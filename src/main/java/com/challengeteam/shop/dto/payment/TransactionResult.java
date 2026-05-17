package com.challengeteam.shop.dto.payment;

import com.challengeteam.shop.entity.order.payment.PaymentStatus;

public record TransactionResult(
        PaymentStatus paymentStatus,
        String transactionId,
        String errorMessage
) {
}