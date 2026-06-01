package com.challengeteam.shop.service.payment;

import com.challengeteam.shop.dto.payment.PaymentWebhookRequestDto;
import com.challengeteam.shop.dto.payment.PaymentWebhookResponseDto;

public interface PaymentWebhookService {
    PaymentWebhookResponseDto processWebhook(String provider, PaymentWebhookRequestDto request);
}
