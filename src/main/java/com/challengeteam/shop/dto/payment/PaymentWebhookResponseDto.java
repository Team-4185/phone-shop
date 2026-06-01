package com.challengeteam.shop.dto.payment;

import com.challengeteam.shop.entity.order.payment.PaymentStatus;

import java.io.Serializable;

public record PaymentWebhookResponseDto(
        boolean processed,
        Long orderId,
        PaymentStatus paymentStatus) implements Serializable {
}
