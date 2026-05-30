package com.challengeteam.shop.dto.admin.customer;

import com.challengeteam.shop.entity.order.OrderStatus;
import com.challengeteam.shop.entity.order.payment.PaymentStatus;
import java.math.BigDecimal;
import java.time.Instant;

/** Compact order history item inside admin customer details. */
public record AdminCustomerOrderSummaryResponseDto(
    Long id, OrderStatus status, PaymentStatus paymentStatus, BigDecimal total, Instant createdAt) {}
