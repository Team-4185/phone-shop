package com.challengeteam.shop.service.payment;

import com.challengeteam.shop.dto.payment.PaymentWebhookResponseDto;

public interface PaymentWebhookService {
    PaymentWebhookResponseDto processWebhook(String provider, String payload, String signatureHeader);
}
