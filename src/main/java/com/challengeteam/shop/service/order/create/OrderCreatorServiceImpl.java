package com.challengeteam.shop.service.order.create;

import com.challengeteam.shop.constants.notification.type.Notification_type;
import com.challengeteam.shop.dto.email.Notification;
import com.challengeteam.shop.dto.order.OrderResponseDto;
import com.challengeteam.shop.dto.order.request.item.OrderItemRequestDto;
import com.challengeteam.shop.dto.order.request.order.OrderRequestDto;
import com.challengeteam.shop.dto.order.request.shippingAddress.ShippingAddressRequestDto;
import com.challengeteam.shop.dto.payment.TransactionResult;
import com.challengeteam.shop.entity.order.Order;
import com.challengeteam.shop.entity.order.OrderItem;
import com.challengeteam.shop.entity.order.OrderStatus;
import com.challengeteam.shop.entity.order.payment.PaymentDetails;
import com.challengeteam.shop.entity.order.payment.PaymentStatus;
import com.challengeteam.shop.entity.order.shipping.ShippingAddress;
import com.challengeteam.shop.entity.phone.Phone;
import com.challengeteam.shop.entity.user.User;
import com.challengeteam.shop.exceptionHandling.exception.order.PaymentFailedException;
import com.challengeteam.shop.exceptionHandling.exception.phone.PhoneNotFoundException;
import com.challengeteam.shop.mapper.order.OrderMapper;
import com.challengeteam.shop.mapper.order.ShippingAddressOrderMapper;
import com.challengeteam.shop.persistence.repository.OrderRepository;
import com.challengeteam.shop.persistence.repository.PhoneRepository;
import com.challengeteam.shop.service.mock.PaymentMockService;
import com.challengeteam.shop.service.notification.NotificationSenderService;
import com.challengeteam.shop.utility.AuthenticationUserExtractorHelper;
import com.challengeteam.shop.utility.notification.email.OrderConfirmationEmailBuilder;
import com.challengeteam.shop.utility.order.InputNormalizer;
import com.challengeteam.shop.utility.order.OrderUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderCreatorServiceImpl implements OrderCreatorService {

    private final PhoneRepository phoneRepository;
    private final OrderRepository orderRepository;
    private final AuthenticationUserExtractorHelper authenticationUserExtractorHelper;
    private final NotificationSenderService notificationSenderService;
    private final OrderMapper orderMapper;
    private final PaymentMockService paymentMockService;
    private final ShippingAddressOrderMapper shippingAddressOrderMapper;
    private final OrderConfirmationEmailBuilder orderConfirmationEmailBuilder;

    /**
     * Creates a new order based on the provided request and authentication details.
     * This method performs stock validation, processes payment, updates phone stock,
     * saves the order, and sends a confirmation notification.
     *
     * @param request        the {@code OrderRequestDto} containing order details such as items and quantities
     * @param authentication the {@code Authentication} object providing the details of the authenticated user
     * @return an {@code OrderResponseDto} containing details of the created order
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    @Override
    public OrderResponseDto create(OrderRequestDto request, Authentication authentication) {
        Map<Long, Phone> phoneMap = resolveAndValidatePhones(request.items());
        OrderUtils.checkIfStockAvailable(phoneMap, request.items());
        OrderUtils.checkIfColorAndStorageAvailable(phoneMap, request.items());

        Order order = buildOrder(request, authentication, phoneMap);
        PaymentDetails paymentDetails = processPayment(request, order.getTotal());

        order.setPaymentDetails(paymentDetails);

        updatePhoneStock(phoneMap, request.items());
        phoneRepository.saveAll(phoneMap.values());
        log.debug("Saved {} phones after stock update", phoneMap.size());

        orderRepository.save(order);
        log.debug("Saved order: {}", order.getId());

        sendConfirmationNotification(order);

        return orderMapper.toDto(order);
    }


    private Map<Long, Phone> resolveAndValidatePhones(List<OrderItemRequestDto> items) {
        Set<Long> uniquePhoneIds = items.stream()
                .map(OrderItemRequestDto::phoneId)
                .collect(toSet());

        List<Phone> foundPhones = phoneRepository.findAllById(uniquePhoneIds);

        if (foundPhones.size() != uniquePhoneIds.size()) {
            Set<Long> foundIds = foundPhones.stream()
                    .map(Phone::getId)
                    .collect(toSet());
            Set<Long> missingIds = uniquePhoneIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .collect(toSet());
            log.error("Phones not found for IDs: {}", missingIds);
            throw new PhoneNotFoundException("Phones not found: " + missingIds);
        }

        return foundPhones.stream()
                .collect(toMap(Phone::getId, Function.identity()));
    }

    private Order buildOrder(OrderRequestDto request,
                             Authentication authentication,
                             Map<Long, Phone> phoneMap) {
        Optional<User> extractedUser = authenticationUserExtractorHelper
                .extractUserFromSecurityContextHolder(authentication);

        List<OrderItem> orderItems = new ArrayList<>(request.items().size());
        BigDecimal totalPrice = BigDecimal.ZERO;

        for (OrderItemRequestDto item : request.items()) {
            Phone phone = phoneMap.get(item.phoneId());
            BigDecimal itemTotal = phone.getPrice()
                    .multiply(BigDecimal.valueOf(item.quantity()));
            orderItems.add(OrderItem.builder()
                    .phone(phone)
                    .productName(phone.getName())
                    .sku(phone.getSku())
                    .selectedColor(item.color())
                    .selectedStorage(item.storage())
                    .unitPrice(phone.getPrice())
                    .quantity(item.quantity())
                    .totalPrice(itemTotal)
                    .build());
            totalPrice = totalPrice.add(itemTotal);
        }

        Order order = Order.builder()
                .user(extractedUser.orElse(null))
                .customerEmail(InputNormalizer.toEmail(request.customerEmail()))
                .customerFirstName(InputNormalizer.toTitleCase(request.customerFirstName()))
                .customerLastName(InputNormalizer.toTitleCase(request.customerLastName()))
                .customerPhoneNumber(request.customerPhoneNumber())
                .status(OrderStatus.NEW)
                .paymentMethod(request.paymentMethod())
                .deliveryMethod(request.deliveryMethod())
                .shippingAddress(request.shippingAddress() != null
                        ? normalizeShippingAddress(request.shippingAddress())
                        : null)
                .total(totalPrice)
                .build();

        orderItems.forEach(item -> item.setOrder(order));
        order.setItems(orderItems);

        return order;
    }

    private PaymentDetails processPayment(OrderRequestDto request, BigDecimal total) {
        return switch (request.paymentMethod()) {
            case CARD -> {
                log.debug("Processing CARD payment, amount: {}", total);
                TransactionResult result = paymentMockService.pay(
                        request.paymentDetails(), total);
                if (result.paymentStatus() == PaymentStatus.FAILED) {
                    log.error("Payment failed: {}", result.errorMessage());
                    throw new PaymentFailedException(result.errorMessage());
                }
                yield new PaymentDetails(result.paymentStatus(), result.transactionId());
            }
            case CASH_ON_DELIVERY -> {
                log.debug("Payment method is CASH_ON_DELIVERY, skipping payment processing");
                yield new PaymentDetails(PaymentStatus.PENDING, null);
            }
        };
    }

    private void updatePhoneStock(Map<Long, Phone> phoneMap, List<OrderItemRequestDto> items) {
        items.forEach(item ->
                OrderUtils.updatePhoneStock(phoneMap.get(item.phoneId()), item.quantity()));
        log.debug("Updated stock for {} phones", phoneMap.size());
    }

    private void sendConfirmationNotification(Order order) {
        Notification notification = orderConfirmationEmailBuilder
                .buildOrderConfirmationNotification(order);
        log.debug("Sending order confirmation notification to: {}", notification.to());
        notificationSenderService.sendNotification(notification, Notification_type.EMAIL);
    }

    private ShippingAddress normalizeShippingAddress(ShippingAddressRequestDto dto) {
        ShippingAddress address = shippingAddressOrderMapper.toShippingAddress(dto);
        address.setCity(InputNormalizer.toTitleCase(dto.city()));
        address.setStreet(InputNormalizer.toTitleCase(dto.street()));
        address.setRegion(InputNormalizer.toTitleCase(dto.region()));
        address.setCountry(InputNormalizer.toTitleCase(dto.country()));
        return address;
    }
}