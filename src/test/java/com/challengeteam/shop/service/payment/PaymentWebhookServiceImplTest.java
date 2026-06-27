package com.challengeteam.shop.service.payment;

import com.challengeteam.shop.dto.payment.PaymentWebhookRequestDto;
import com.challengeteam.shop.dto.payment.PaymentWebhookResponseDto;
import com.challengeteam.shop.entity.order.Order;
import com.challengeteam.shop.entity.order.payment.PaymentDetails;
import com.challengeteam.shop.entity.order.payment.PaymentStatus;
import com.challengeteam.shop.entity.payment.ProcessedPaymentEvent;
import com.challengeteam.shop.exceptionHandling.exception.payment.PaymentWebhookException;
import com.challengeteam.shop.persistence.repository.OrderRepository;
import com.challengeteam.shop.persistence.repository.ProcessedPaymentEventRepository;
import com.challengeteam.shop.properties.StripeProperties;
import com.challengeteam.shop.service.payment.stripe.StripeClientGateway;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.stripe.Stripe;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentWebhookServiceImplTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ProcessedPaymentEventRepository processedPaymentEventRepository;
    @Mock
    private PaymentProviderResolver paymentProviderResolver;
    @Mock
    private PaymentProvider paymentProvider;
    @Mock
    private StripeClientGateway stripeClientGateway;

    @Test
    void shouldUpdatePaymentStatusAndStoreProcessedEvent() {
        PaymentWebhookServiceImpl paymentWebhookService = paymentWebhookService();
        Order order = Order.builder()
                .id(10L)
                .paymentDetails(new PaymentDetails(PaymentStatus.PENDING, "tx-1"))
                .build();
        String payload = """
                {"eventId":"evt-1","transactionId":"tx-1","paymentStatus":"PAID","errorMessage":null}
                """;

        when(paymentProviderResolver.getProvider("mock")).thenReturn(paymentProvider);
        when(orderRepository.findByPaymentDetailsTransactionId("tx-1")).thenReturn(Optional.of(order));
        when(processedPaymentEventRepository.existsByExternalEventId("evt-1")).thenReturn(false);

        PaymentWebhookResponseDto response = paymentWebhookService.processWebhook("mock", payload, null);

        assertThat(response.processed()).isTrue();
        assertThat(response.orderId()).isEqualTo(10L);
        assertThat(order.getPaymentDetails().getPaymentStatus()).isEqualTo(PaymentStatus.PAID);

        ArgumentCaptor<ProcessedPaymentEvent> eventCaptor =
                ArgumentCaptor.forClass(ProcessedPaymentEvent.class);
        verify(processedPaymentEventRepository).save(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getExternalEventId()).isEqualTo("evt-1");
        assertThat(eventCaptor.getValue().getExternalTransactionId()).isEqualTo("tx-1");
    }

    @Test
    void shouldIgnoreDuplicateWebhookEvent() {
        PaymentWebhookServiceImpl paymentWebhookService = paymentWebhookService();
        Order order = Order.builder()
                .id(10L)
                .paymentDetails(new PaymentDetails(PaymentStatus.PAID, "tx-1"))
                .build();
        String payload = """
                {"eventId":"evt-1","transactionId":"tx-1","paymentStatus":"FAILED","errorMessage":null}
                """;

        when(paymentProviderResolver.getProvider("mock")).thenReturn(paymentProvider);
        when(orderRepository.findByPaymentDetailsTransactionId("tx-1")).thenReturn(Optional.of(order));
        when(processedPaymentEventRepository.existsByExternalEventId("evt-1")).thenReturn(true);

        PaymentWebhookResponseDto response = paymentWebhookService.processWebhook("mock", payload, null);

        assertThat(response.processed()).isFalse();
        assertThat(response.paymentStatus()).isEqualTo(PaymentStatus.PAID);
        verify(processedPaymentEventRepository, never()).save(any());
    }

    @Test
    void shouldRejectStripeWebhookWithoutSignature() {
        PaymentWebhookServiceImpl paymentWebhookService = paymentWebhookService();

        when(paymentProviderResolver.getProvider("stripe")).thenReturn(paymentProvider);

        assertThatThrownBy(() -> paymentWebhookService.processWebhook("stripe", "{}", null))
                .isInstanceOf(PaymentWebhookException.class)
                .hasMessage("Missing Stripe-Signature header");
    }

    @Test
    void shouldIgnoreUnsupportedStripeWebhookEvent() throws Exception {
        PaymentWebhookServiceImpl paymentWebhookService = paymentWebhookService();
        Event event = new Event();
        event.setId("evt-ignored");
        event.setType("customer.created");

        when(paymentProviderResolver.getProvider("stripe")).thenReturn(paymentProvider);
        when(stripeClientGateway.constructWebhookEvent("{}", "signature", "whsec_test")).thenReturn(event);

        PaymentWebhookResponseDto response = paymentWebhookService.processWebhook("stripe", "{}", "signature");

        assertThat(response.processed()).isFalse();
        assertThat(response.orderId()).isNull();
        assertThat(response.paymentStatus()).isNull();
        verifyNoInteractions(orderRepository, processedPaymentEventRepository);
    }

    @Test
    void shouldUpdatePaymentStatusFromStripePaymentIntentWebhook() throws Exception {
        PaymentWebhookServiceImpl paymentWebhookService = paymentWebhookService();
        PaymentIntent paymentIntent = new PaymentIntent();
        paymentIntent.setId("pi_test");
        Event event = new Event();
        event.setId("evt-stripe");
        event.setApiVersion(Stripe.API_VERSION);
        event.setType("payment_intent.succeeded");
        event.setData(stripeEventData(paymentIntent));
        Order order = Order.builder()
                .id(20L)
                .paymentDetails(new PaymentDetails(PaymentStatus.PENDING, "pi_test"))
                .build();

        when(paymentProviderResolver.getProvider("stripe")).thenReturn(paymentProvider);
        when(stripeClientGateway.constructWebhookEvent("{}", "signature", "whsec_test")).thenReturn(event);
        when(orderRepository.findByPaymentDetailsTransactionId("pi_test")).thenReturn(Optional.of(order));
        when(processedPaymentEventRepository.existsByExternalEventId("evt-stripe")).thenReturn(false);

        PaymentWebhookResponseDto response = paymentWebhookService.processWebhook("stripe", "{}", "signature");

        assertThat(response.processed()).isTrue();
        assertThat(response.orderId()).isEqualTo(20L);
        assertThat(order.getPaymentDetails().getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
    }

    private Event.Data stripeEventData(PaymentIntent paymentIntent) {
        JsonObject object = new JsonObject();
        object.addProperty("object", "payment_intent");
        object.addProperty("id", paymentIntent.getId());
        Event.Data data = new Event.Data();
        data.setObject(object);
        return data;
    }

    private PaymentWebhookServiceImpl paymentWebhookService() {
        StripeProperties stripeProperties = new StripeProperties();
        stripeProperties.setWebhookSecret("whsec_test");
        return new PaymentWebhookServiceImpl(
                orderRepository,
                processedPaymentEventRepository,
                paymentProviderResolver,
                new ObjectMapper(),
                stripeClientGateway,
                stripeProperties);
    }
}
