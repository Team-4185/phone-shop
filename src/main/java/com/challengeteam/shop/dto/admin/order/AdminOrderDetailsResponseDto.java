package com.challengeteam.shop.dto.admin.order;

import com.challengeteam.shop.entity.order.DeliveryMethod;
import com.challengeteam.shop.entity.order.OrderStatus;
import com.challengeteam.shop.entity.order.PaymentMethod;
import com.challengeteam.shop.entity.order.PaymentStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/** Full order payload used by the admin order details screen and workflow actions. */
public record AdminOrderDetailsResponseDto(
    Long id,
    Long customerId,
    String customerEmail,
    String customerFirstName,
    String customerLastName,
    String customerPhoneNumber,
    String customerCity,
    OrderStatus status,
    PaymentMethod paymentMethod,
    PaymentStatus paymentStatus,
    DeliveryMethod deliveryMethod,
    BigDecimal total,
    List<String> availableActions,
    List<AdminOrderItemResponseDto> items,
    Instant createdAt,
    Instant updatedAt) {}
