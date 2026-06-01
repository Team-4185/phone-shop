package com.challengeteam.shop.dto.payment;

import com.challengeteam.shop.entity.order.payment.PaymentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

public record PaymentWebhookRequestDto(
        @NotBlank(message = "eventId is required")
        String eventId,
        
        @NotBlank(message = "transactionId is required")
        String transactionId,
        
        @NotNull(message = "paymentStatus is required")
        PaymentStatus paymentStatus,
        
        String errorMessage
) implements Serializable {
}
