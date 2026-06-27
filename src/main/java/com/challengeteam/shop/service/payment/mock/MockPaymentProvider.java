package com.challengeteam.shop.service.payment.mock;

import com.challengeteam.shop.dto.order.request.payment.PaymentDetailsRequestDto;
import com.challengeteam.shop.dto.payment.TransactionResult;
import com.challengeteam.shop.entity.order.payment.PaymentStatus;
import com.challengeteam.shop.service.mock.PaymentMockService;
import com.challengeteam.shop.service.payment.PaymentProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
@RequiredArgsConstructor
public class MockPaymentProvider implements PaymentProvider {
    public static final String PROVIDER_CODE = "mock";
    private final PaymentMockService paymentMockService;

    @Override
    public String providerCode() {
        return PROVIDER_CODE;
    }

    @Override
    public TransactionResult pay(PaymentDetailsRequestDto paymentDetails, BigDecimal amount) {
        return paymentMockService.pay(paymentDetails, amount);
    }

    @Override
    public TransactionResult refund(String transactionId, BigDecimal amount) {
        return new TransactionResult(PaymentStatus.REFUNDED, transactionId, null);
    }
}
