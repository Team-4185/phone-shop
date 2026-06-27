package com.challengeteam.shop.service.order.customer;

import com.challengeteam.shop.constants.notification.type.Notification_type;
import com.challengeteam.shop.dto.email.Notification;
import com.challengeteam.shop.dto.order.OrderResponseDto;
import com.challengeteam.shop.dto.order.request.checkout.CartItemSelectionRequestDto;
import com.challengeteam.shop.dto.order.request.checkout.CheckoutRequestDto;
import com.challengeteam.shop.dto.order.request.item.OrderItemRequestDto;
import com.challengeteam.shop.dto.order.request.order.OrderRequestDto;
import com.challengeteam.shop.entity.cart.Cart;
import com.challengeteam.shop.entity.cart.CartItem;
import com.challengeteam.shop.entity.order.Order;
import com.challengeteam.shop.entity.order.OrderStatus;
import com.challengeteam.shop.entity.user.User;
import com.challengeteam.shop.exceptionHandling.exception.InvalidAPIRequestException;
import com.challengeteam.shop.exceptionHandling.exception.ResourceNotFoundException;
import com.challengeteam.shop.exceptionHandling.exception.order.InvalidOrderStatusTransitionException;
import com.challengeteam.shop.exceptionHandling.exception.order.OrderNotFoundException;
import com.challengeteam.shop.exceptionHandling.exception.security.UnauthorizedException;
import com.challengeteam.shop.mapper.order.OrderMapper;
import com.challengeteam.shop.persistence.repository.OrderRepository;
import com.challengeteam.shop.service.CartService;
import com.challengeteam.shop.service.UserCartService;
import com.challengeteam.shop.service.notification.NotificationSenderService;
import com.challengeteam.shop.service.order.create.OrderCreatorService;
import com.challengeteam.shop.utility.AuthenticationUserExtractorHelper;
import com.challengeteam.shop.utility.notification.email.OrderStatusEmailBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerOrderServiceImpl implements CustomerOrderService {

    private static final Set<OrderStatus> CUSTOMER_CANCELLABLE_STATUSES =
            Set.of(OrderStatus.NEW, OrderStatus.CONFIRMED, OrderStatus.PROCESSING);

    private final OrderCreatorService orderCreatorService;
    private final UserCartService userCartService;
    private final CartService cartService;
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final AuthenticationUserExtractorHelper authenticationUserExtractorHelper;
    private final NotificationSenderService notificationSenderService;
    private final OrderStatusEmailBuilder orderStatusEmailBuilder;

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public OrderResponseDto checkoutFromCart(CheckoutRequestDto request, Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        Cart cart = userCartService.getUserCart(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cart for user with id " + user.getId() + " not found"));

        if (cart.getCartItems().isEmpty()) {
            throw new InvalidAPIRequestException("Cannot checkout an empty cart");
        }

        OrderRequestDto orderRequest = toOrderRequest(request, cart);
        OrderResponseDto response = orderCreatorService.create(orderRequest, authentication);
        cartService.clearCart(cart);
        log.info("Checked out cart id={} for user id={} into order id={}",
                cart.getId(), user.getId(), response.id());
        return response;
    }

    @Override
    @Transactional
    public OrderResponseDto cancel(long orderId, Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));

        if (order.getUser() == null || !order.getUser().getId().equals(user.getId())) {
            throw new OrderNotFoundException("Order not found: " + orderId);
        }

        OrderStatus previousStatus = order.getStatus();
        if (!CUSTOMER_CANCELLABLE_STATUSES.contains(previousStatus)) {
            throw new InvalidOrderStatusTransitionException(
                    "Order status %s cannot be cancelled by customer".formatted(previousStatus));
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order savedOrder = orderRepository.save(order);
        sendStatusChangedNotification(savedOrder, previousStatus, OrderStatus.CANCELLED);
        log.info("Customer cancelled order id={} userId={} previousStatus={}",
                orderId, user.getId(), previousStatus);
        return orderMapper.toDto(savedOrder);
    }

    private OrderRequestDto toOrderRequest(CheckoutRequestDto request, Cart cart) {
        resolveSelections(request, cart);
        List<OrderItemRequestDto> orderItems = cart.getCartItems().stream()
                .map(this::toOrderItemRequest)
                .toList();

        return new OrderRequestDto(
                request.customerEmail(),
                request.customerFirstName(),
                request.customerLastName(),
                request.customerPhoneNumber(),
                request.paymentMethod(),
                request.paymentDetails(),
                request.deliveryMethod(),
                request.shippingAddress(),
                orderItems
        );
    }

    private Map<Long, CartItemSelectionRequestDto> resolveSelections(CheckoutRequestDto request, Cart cart) {
        Map<Long, CartItemSelectionRequestDto> selectionsByVariantId = request.itemSelections().stream()
                .collect(toMap(
                        selection -> resolveSelectionVariantId(selection, cart),
                        Function.identity(),
                        (left, right) -> {
                            throw new InvalidAPIRequestException(
                                    "Duplicate cart item selection");
                        }
                ));

        Set<Long> cartVariantIds = cart.getCartItems().stream()
                .map(item -> item.getVariant().getId())
                .collect(toSet());
        Set<Long> selectionVariantIds = selectionsByVariantId.keySet();

        if (!selectionVariantIds.equals(cartVariantIds)) {
            throw new InvalidAPIRequestException(
                    "Cart item selections must match current cart items");
        }

        return selectionsByVariantId;
    }

    private OrderItemRequestDto toOrderItemRequest(CartItem item) {
        return new OrderItemRequestDto(
                item.getPhone().getId(),
                item.getVariant().getId(),
                item.getAmount(),
                item.getVariant().getColor(),
                item.getVariant().getStorageCapacity()
        );
    }

    private Long resolveSelectionVariantId(CartItemSelectionRequestDto selection, Cart cart) {
        return java.util.Optional.ofNullable(selection.variantId())
                .orElseGet(() -> cart.getCartItems().stream()
                        .filter(item -> item.getPhone().getId().equals(selection.phoneId()))
                        .map(item -> item.getVariant().getId())
                        .findFirst()
                        .orElseThrow(() -> new InvalidAPIRequestException(
                                "Cart item selection does not match current cart items")));
    }

    private User getAuthenticatedUser(Authentication authentication) {
        return authenticationUserExtractorHelper.extractUserFromSecurityContextHolder(authentication)
                .orElseThrow(() -> new UnauthorizedException("User not authenticated"));
    }

    private void sendStatusChangedNotification(Order order,
                                               OrderStatus previousStatus,
                                               OrderStatus currentStatus) {
        Notification notification = orderStatusEmailBuilder
                .buildOrderStatusChangedNotification(order, previousStatus, currentStatus);
        notificationSenderService.sendNotification(notification, Notification_type.EMAIL);
    }
}
