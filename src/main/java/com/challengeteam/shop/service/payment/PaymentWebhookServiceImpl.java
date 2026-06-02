package com.challengeteam.shop.service.payment;

import com.challengeteam.shop.dto.payment.PaymentWebhookRequestDto;
import com.challengeteam.shop.dto.payment.PaymentWebhookResponseDto;
import com.challengeteam.shop.entity.order.Order;
import com.challengeteam.shop.entity.payment.ProcessedPaymentEvent;
import com.challengeteam.shop.exceptionHandling.exception.order.OrderNotFoundException;
import com.challengeteam.shop.persistence.repository.OrderRepository;
import com.challengeteam.shop.persistence.repository.ProcessedPaymentEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentWebhookServiceImpl implements PaymentWebhookService {
    private final OrderRepository orderRepository;
    private final ProcessedPaymentEventRepository processedPaymentEventRepository;
    private final PaymentProviderResolver paymentProviderResolver;

    @Override
    @Transactional
    public PaymentWebhookResponseDto processWebhook(String provider, PaymentWebhookRequestDto request) {
        paymentProviderResolver.getProvider(provider);

        Order order = orderRepository.findByPaymentDetailsTransactionId(request.transactionId())
                .orElseThrow(() -> new OrderNotFoundException(
                        "Order not found for payment transaction: " + request.transactionId()));

        if (processedPaymentEventRepository.existsByExternalEventId(request.eventId())) {
            return new PaymentWebhookResponseDto(false, order.getId(), order.getPaymentDetails().getPaymentStatus());
        }

        order.getPaymentDetails().setPaymentStatus(request.paymentStatus());
        processedPaymentEventRepository.save(ProcessedPaymentEvent.builder()
                .provider(provider)
                .externalEventId(request.eventId())
                .externalTransactionId(request.transactionId())
                .build());

        return new PaymentWebhookResponseDto(true, order.getId(), request.paymentStatus());
    }
}
