package com.challengeteam.shop.dto.payment;

import com.challengeteam.shop.entity.order.payment.PaymentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

public record PaymentWebhookRequestDto(
        @NotBlank String eventId,
        @NotBlank String transactionId,
        @NotNull PaymentStatus paymentStatus,
        String errorMessage) implements Serializable {
}
