package com.challengeteam.shop.dto.admin.order;

import com.challengeteam.shop.entity.order.DeliveryMethod;
import com.challengeteam.shop.entity.order.OrderStatus;
import com.challengeteam.shop.entity.order.payment.PaymentMethod;
import com.challengeteam.shop.entity.order.payment.PaymentStatus;
import java.math.BigDecimal;
import java.time.Instant;

/** Compact order row for the admin Order Management table. */
public record AdminOrderListItemResponseDto(
    Long id,
    Long customerId,
    String customerEmail,
    OrderStatus status,
    PaymentMethod paymentMethod,
    PaymentStatus paymentStatus,
    DeliveryMethod deliveryMethod,
    BigDecimal total,
    Integer itemsCount,
    Instant createdAt,
    Instant updatedAt) {}
