package com.challengeteam.shop.dto.admin.dashboard;

import com.challengeteam.shop.entity.order.OrderStatus;
import com.challengeteam.shop.entity.order.payment.PaymentStatus;
import java.math.BigDecimal;
import java.time.Instant;

/** Recent order row for the dashboard widget. */
public record AdminDashboardRecentOrderResponseDto(
    Long id,
    String customerEmail,
    OrderStatus status,
    PaymentStatus paymentStatus,
    BigDecimal total,
    Instant createdAt) {}
