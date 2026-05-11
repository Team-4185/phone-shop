package com.challengeteam.shop.dto.admin.order;

import com.challengeteam.shop.entity.order.OrderStatus;
import com.challengeteam.shop.entity.order.PaymentMethod;
import com.challengeteam.shop.entity.order.DeliveryMethod;
import java.math.BigDecimal;
import java.time.Instant;

/** Compact order row for the admin Order Management table. */
public record AdminOrderListItemResponseDto(
    Long id,
    Long customerId,
    String customerEmail,
    OrderStatus status,
    PaymentMethod paymentMethod,
    DeliveryMethod deliveryMethod,
    BigDecimal total,
    Integer itemsCount,
    Instant createdAt,
    Instant updatedAt) {}
