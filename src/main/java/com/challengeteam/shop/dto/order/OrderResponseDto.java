package com.challengeteam.shop.dto.order;

import com.challengeteam.shop.entity.order.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponseDto(
        Long id,
        Instant createdAt,
        OrderStatus status,
        BigDecimal totalPrice,
        String destination,
        String paymentPaid,
        String paymentMethod,
        String paymentCheckoutUrl,
        String recipientEmail,
        String recipientPhone,
        String recipientFirstname,
        String recipientLastname,
        List<OrderItemResponseDto> orderItems
) {
}
