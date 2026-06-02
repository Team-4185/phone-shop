package com.challengeteam.shop.web.controller.payment;

import com.challengeteam.shop.dto.payment.PaymentWebhookRequestDto;
import com.challengeteam.shop.dto.payment.PaymentWebhookResponseDto;
import com.challengeteam.shop.service.payment.PaymentWebhookService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments/webhooks")
@RequiredArgsConstructor
@Validated
public class PaymentWebhookController {
    private final PaymentWebhookService paymentWebhookService;

    @Operation(summary = "Receive payment provider webhook")
    @PostMapping("/{provider}")
    public ResponseEntity<PaymentWebhookResponseDto> receiveWebhook(
            @PathVariable String provider,
            @Valid @RequestBody PaymentWebhookRequestDto request) {
        return ResponseEntity.ok(paymentWebhookService.processWebhook(provider, request));
    }
}
