package com.challengeteam.shop.service.payment;

import com.challengeteam.shop.dto.order.request.payment.PaymentDetailsRequestDto;
import com.challengeteam.shop.dto.payment.TransactionResult;

import java.math.BigDecimal;

public interface PaymentProvider {
    String providerCode();

    TransactionResult pay(PaymentDetailsRequestDto paymentDetails, BigDecimal amount);

    TransactionResult refund(String transactionId, BigDecimal amount);
}
