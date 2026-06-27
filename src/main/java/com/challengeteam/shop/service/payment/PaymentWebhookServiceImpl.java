package com.challengeteam.shop.service.payment;

import com.challengeteam.shop.dto.payment.PaymentWebhookRequestDto;
import com.challengeteam.shop.dto.payment.PaymentWebhookResponseDto;
import com.challengeteam.shop.entity.order.Order;
import com.challengeteam.shop.entity.order.payment.PaymentStatus;
import com.challengeteam.shop.entity.payment.ProcessedPaymentEvent;
import com.challengeteam.shop.exceptionHandling.exception.order.OrderNotFoundException;
import com.challengeteam.shop.exceptionHandling.exception.payment.PaymentWebhookException;
import com.challengeteam.shop.persistence.repository.OrderRepository;
import com.challengeteam.shop.persistence.repository.ProcessedPaymentEventRepository;
import com.challengeteam.shop.properties.StripeProperties;
import com.challengeteam.shop.service.payment.stripe.StripeClientGateway;
import com.challengeteam.shop.service.payment.stripe.StripePaymentProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Charge;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentWebhookServiceImpl implements PaymentWebhookService {
    private final OrderRepository orderRepository;
    private final ProcessedPaymentEventRepository processedPaymentEventRepository;
    private final PaymentProviderResolver paymentProviderResolver;
    private final ObjectMapper objectMapper;
    private final StripeClientGateway stripeClientGateway;
    private final StripeProperties stripeProperties;

    @Override
    @Transactional
    public PaymentWebhookResponseDto processWebhook(String provider, String payload, String signatureHeader) {
        paymentProviderResolver.getProvider(provider);

        Optional<PaymentWebhookRequestDto> parsedRequest = StripePaymentProvider.PROVIDER_CODE.equals(provider)
                ? parseStripeWebhook(payload, signatureHeader)
                : Optional.of(parseJsonWebhook(payload));
        if (parsedRequest.isEmpty()) {
            return new PaymentWebhookResponseDto(false, null, null);
        }

        PaymentWebhookRequestDto request = parsedRequest.get();
        validateWebhookRequest(request);

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

    private PaymentWebhookRequestDto parseJsonWebhook(String payload) {
        try {
            return objectMapper.readValue(payload, PaymentWebhookRequestDto.class);
        } catch (JsonProcessingException e) {
            throw new PaymentWebhookException("Invalid payment webhook payload", e);
        }
    }

    private Optional<PaymentWebhookRequestDto> parseStripeWebhook(String payload, String signatureHeader) {
        if (signatureHeader == null || signatureHeader.isBlank()) {
            throw new PaymentWebhookException("Missing Stripe-Signature header");
        }
        if (stripeProperties.getWebhookSecret() == null || stripeProperties.getWebhookSecret().isBlank()) {
            throw new PaymentWebhookException("Stripe webhook secret is not configured");
        }

        Event event;
        try {
            event = stripeClientGateway.constructWebhookEvent(
                    payload, signatureHeader, stripeProperties.getWebhookSecret());
        } catch (SignatureVerificationException e) {
            throw new PaymentWebhookException("Invalid Stripe webhook signature", e);
        }

        return toWebhookRequest(event);
    }

    private Optional<PaymentWebhookRequestDto> toWebhookRequest(Event event) {
        return switch (event.getType()) {
            case "payment_intent.succeeded" ->
                    Optional.of(paymentIntentWebhook(event.getId(), stripeObject(event), PaymentStatus.PAID));
            case "payment_intent.payment_failed", "payment_intent.canceled" ->
                    Optional.of(paymentIntentWebhook(event.getId(), stripeObject(event), PaymentStatus.FAILED));
            case "charge.refunded" -> Optional.of(chargeWebhook(event.getId(), stripeObject(event), PaymentStatus.REFUNDED));
            default -> {
                log.debug("Ignoring unsupported Stripe webhook type={}", event.getType());
                yield Optional.empty();
            }
        };
    }

    private StripeObject stripeObject(Event event) {
        return event.getDataObjectDeserializer()
                .getObject()
                .orElseThrow(() -> new PaymentWebhookException("Unsupported Stripe webhook payload"));
    }

    private PaymentWebhookRequestDto paymentIntentWebhook(
            String eventId, StripeObject stripeObject, PaymentStatus paymentStatus) {
        if (!(stripeObject instanceof PaymentIntent paymentIntent)) {
            throw new PaymentWebhookException("Stripe event does not contain PaymentIntent data");
        }

        return new PaymentWebhookRequestDto(eventId, paymentIntent.getId(), paymentStatus, null);
    }

    private PaymentWebhookRequestDto chargeWebhook(
            String eventId, StripeObject stripeObject, PaymentStatus paymentStatus) {
        if (!(stripeObject instanceof Charge charge)) {
            throw new PaymentWebhookException("Stripe event does not contain Charge data");
        }
        if (charge.getPaymentIntent() == null || charge.getPaymentIntent().isBlank()) {
            throw new PaymentWebhookException("Stripe charge has no payment intent id");
        }

        return new PaymentWebhookRequestDto(eventId, charge.getPaymentIntent(), paymentStatus, null);
    }

    private void validateWebhookRequest(PaymentWebhookRequestDto request) {
        if (request.eventId() == null || request.eventId().isBlank()) {
            throw new PaymentWebhookException("Payment webhook eventId is required");
        }
        if (request.transactionId() == null || request.transactionId().isBlank()) {
            throw new PaymentWebhookException("Payment webhook transactionId is required");
        }
        if (request.paymentStatus() == null) {
            throw new PaymentWebhookException("Payment webhook paymentStatus is required");
        }
    }
}
