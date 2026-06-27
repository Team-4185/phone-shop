package com.challengeteam.shop.service.order.create;

import com.challengeteam.shop.constants.notification.type.Notification_type;
import com.challengeteam.shop.dto.delivery.DeliveryQuote;
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
import com.challengeteam.shop.entity.phone.ProductVariant;
import com.challengeteam.shop.entity.user.User;
import com.challengeteam.shop.exceptionHandling.exception.order.PaymentFailedException;
import com.challengeteam.shop.exceptionHandling.exception.phone.PhoneNotFoundException;
import com.challengeteam.shop.mapper.order.OrderMapper;
import com.challengeteam.shop.mapper.order.ShippingAddressOrderMapper;
import com.challengeteam.shop.persistence.repository.OrderRepository;
import com.challengeteam.shop.persistence.repository.PhoneRepository;
import com.challengeteam.shop.persistence.repository.ProductVariantRepository;
import com.challengeteam.shop.service.delivery.DeliveryProvider;
import com.challengeteam.shop.service.delivery.DeliveryProviderResolver;
import com.challengeteam.shop.service.notification.NotificationSenderService;
import com.challengeteam.shop.service.payment.PaymentProvider;
import com.challengeteam.shop.service.payment.PaymentProviderResolver;
import com.challengeteam.shop.utility.AuthenticationUserExtractorHelper;
import com.challengeteam.shop.utility.InputNormalizer;
import com.challengeteam.shop.utility.notification.email.OrderConfirmationEmailBuilder;
import com.challengeteam.shop.utility.ProductStatusResolver;
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
    private final ProductVariantRepository productVariantRepository;
    private final OrderRepository orderRepository;
    private final AuthenticationUserExtractorHelper authenticationUserExtractorHelper;
    private final NotificationSenderService notificationSenderService;
    private final OrderMapper orderMapper;
    private final PaymentProviderResolver paymentProviderResolver;
    private final DeliveryProviderResolver deliveryProviderResolver;
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
        Map<Long, ProductVariant> variantMap = resolveAndValidateVariants(request.items());
        checkVariantStockAvailable(variantMap, request.items());

        BigDecimal itemsTotal = calculateItemsTotal(request, variantMap);
        DeliveryProvider deliveryProvider = deliveryProviderResolver.getDefaultProvider();
        DeliveryQuote deliveryQuote = deliveryProvider.quote(
                request.deliveryMethod(), request.shippingAddress(), itemsTotal);

        Order order = buildOrder(request, authentication, variantMap, itemsTotal, deliveryQuote);
        PaymentDetails paymentDetails = processPayment(request, order.getTotal());

        order.setPaymentDetails(paymentDetails);

        updateVariantStock(variantMap, request.items());
        productVariantRepository.saveAll(variantMap.values().stream().toList());
        phoneRepository.saveAll(variantMap.values().stream().map(ProductVariant::getPhone).distinct().toList());
        log.debug("Saved {} variants after stock update", variantMap.size());

        orderRepository.save(order);
        log.debug("Saved order: {}", order.getId());

        sendConfirmationNotification(order);

        return orderMapper.toDto(order);
    }


    private Map<Long, ProductVariant> resolveAndValidateVariants(List<OrderItemRequestDto> items) {
        Set<Long> uniqueVariantIds = items.stream()
                .map(this::resolveVariantId)
                .collect(toSet());

        List<ProductVariant> foundVariants = productVariantRepository.findAllByIdWithPhone(uniqueVariantIds);

        if (foundVariants.size() != uniqueVariantIds.size()) {
            Set<Long> foundIds = foundVariants.stream()
                    .map(ProductVariant::getId)
                    .collect(toSet());
            Set<Long> missingIds = uniqueVariantIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .collect(toSet());
            log.error("Product variants not found for IDs: {}", missingIds);
            throw new PhoneNotFoundException("Product variants not found: " + missingIds);
        }

        return foundVariants.stream()
                .collect(toMap(ProductVariant::getId, Function.identity()));
    }

    private Order buildOrder(OrderRequestDto request,
                             Authentication authentication,
                             Map<Long, ProductVariant> variantMap,
                             BigDecimal itemsTotal,
                             DeliveryQuote deliveryQuote) {
        Optional<User> extractedUser = authenticationUserExtractorHelper
                .extractUserFromSecurityContextHolder(authentication);

        List<OrderItem> orderItems = new ArrayList<>(request.items().size());

        for (OrderItemRequestDto item : request.items()) {
            ProductVariant variant = variantMap.get(resolveVariantId(item));
            Phone phone = variant.getPhone();
            BigDecimal itemTotal = variant.getPrice()
                    .multiply(BigDecimal.valueOf(item.quantity()));
            orderItems.add(OrderItem.builder()
                    .phone(phone)
                    .variant(variant)
                    .productName(phone.getName())
                    .sku(variant.getSku())
                    .selectedColor(variant.getColor())
                    .selectedStorage(variant.getStorageCapacity())
                    .unitPrice(variant.getPrice())
                    .quantity(item.quantity())
                    .totalPrice(itemTotal)
                    .build());
        }

        PaymentProvider paymentProvider = paymentProviderResolver.getDefaultProvider();
        Order order = Order.builder()
                .user(extractedUser.orElse(null))
                .customerEmail(InputNormalizer.toEmail(request.customerEmail()))
                .customerFirstName(InputNormalizer.toTitleCase(request.customerFirstName()))
                .customerLastName(InputNormalizer.toTitleCase(request.customerLastName()))
                .customerPhoneNumber(request.customerPhoneNumber())
                .status(OrderStatus.NEW)
                .paymentMethod(request.paymentMethod())
                .deliveryMethod(request.deliveryMethod())
                .paymentProvider(paymentProvider.providerCode())
                .deliveryProvider(deliveryQuote.provider())
                .pickupPointId(resolvePickupPointId(request))
                .estimatedDeliveryDate(deliveryQuote.estimatedDeliveryDate())
                .deliveryPrice(deliveryQuote.price())
                .shippingAddress(request.shippingAddress() != null
                        ? normalizeShippingAddress(request.shippingAddress())
                        : null)
                .total(itemsTotal.add(deliveryQuote.price()))
                .build();

        orderItems.forEach(item -> item.setOrder(order));
        order.setItems(orderItems);

        return order;
    }

    private PaymentDetails processPayment(OrderRequestDto request, BigDecimal total) {
        return switch (request.paymentMethod()) {
            case CARD -> {
                log.debug("Processing CARD payment, amount: {}", total);
                TransactionResult result = paymentProviderResolver.getDefaultProvider().pay(
                        request.paymentDetails(), total);
                if (result.paymentStatus() != PaymentStatus.PAID) {
                    String message = result.errorMessage() == null || result.errorMessage().isBlank()
                            ? "Payment was not completed"
                            : result.errorMessage();
                    log.error("Payment was not completed, status={}, error={}", result.paymentStatus(), message);
                    throw new PaymentFailedException(message);
                }
                yield new PaymentDetails(result.paymentStatus(), result.transactionId());
            }
            case CASH_ON_DELIVERY -> {
                log.debug("Payment method is CASH_ON_DELIVERY, skipping payment processing");
                yield new PaymentDetails(PaymentStatus.PENDING, null);
            }
        };
    }

    private void updateVariantStock(Map<Long, ProductVariant> variantMap, List<OrderItemRequestDto> items) {
        items.forEach(item -> updateVariantStock(variantMap.get(resolveVariantId(item)), item.quantity()));
        variantMap.values().stream()
                .map(ProductVariant::getPhone)
                .distinct()
                .forEach(this::syncPhoneStockFromVariants);
        log.debug("Updated stock for {} variants", variantMap.size());
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

    private BigDecimal calculateItemsTotal(OrderRequestDto request, Map<Long, ProductVariant> variantMap) {
        return request.items().stream()
                .map(item -> variantMap.get(resolveVariantId(item)).getPrice()
                        .multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Long resolveVariantId(OrderItemRequestDto item) {
        return Optional.ofNullable(item.variantId())
                .orElseGet(() -> productVariantRepository
                        .findByPhoneIdAndColorAndStorageCapacity(item.phoneId(), item.color(), item.storage())
                        .map(ProductVariant::getId)
                        .orElseThrow(() -> new PhoneNotFoundException(
                                "Product variant not found for phone id %s, color %s and storage %s"
                                        .formatted(item.phoneId(), item.color(), item.storage()))));
    }

    private void checkVariantStockAvailable(
            Map<Long, ProductVariant> variantMap, List<OrderItemRequestDto> items) {
        List<String> unavailableVariants = items.stream()
                .filter(item -> variantMap.get(resolveVariantId(item)).getStock() < item.quantity())
                .map(item -> variantMap.get(resolveVariantId(item)).getSku())
                .toList();

        if (!unavailableVariants.isEmpty()) {
            log.error("Not enough stock for variants: {}", unavailableVariants);
            throw new com.challengeteam.shop.exceptionHandling.exception.order.OrderCreationException(
                    "Not enough stock for variants: " + unavailableVariants);
        }
    }

    private void updateVariantStock(ProductVariant variant, int orderedQuantity) {
        int remainingStock = variant.getStock() - orderedQuantity;
        variant.setStock(remainingStock);
        variant.setStatus(ProductStatusResolver.resolve(remainingStock));
        log.info("Updated variant stock for variant: {} with remaining stock: {}",
                variant.getId(), remainingStock);
    }

    private void syncPhoneStockFromVariants(Phone phone) {
        List<ProductVariant> variants = productVariantRepository.findAllByPhoneIdOrderByPriceAscIdAsc(phone.getId());
        int stock = variants.stream().mapToInt(ProductVariant::getStock).sum();
        phone.setStock(stock);
        phone.setStatus(ProductStatusResolver.resolve(stock));
        variants.stream().map(ProductVariant::getPrice).min(BigDecimal::compareTo).ifPresent(phone::setPrice);
    }

    private String resolvePickupPointId(OrderRequestDto request) {
        if (request.deliveryMethod() == com.challengeteam.shop.entity.order.DeliveryMethod.POST_OFFICE
                && request.shippingAddress() != null) {
            return request.shippingAddress().logisticPostOffice();
        }
        return null;
    }
}
