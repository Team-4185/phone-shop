package com.challengeteam.shop.dto.order.response.paymentDetails;

import com.challengeteam.shop.entity.order.payment.PaymentStatus;

import java.io.Serializable;

/**
 * DTO for {@link com.challengeteam.shop.entity.order.payment.PaymentDetails}
 */
public record PaymentDetailsResponseDto(
        PaymentStatus paymentStatus,
        String transactionId) implements Serializable {
}