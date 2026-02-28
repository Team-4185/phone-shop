package com.challengeteam.shop.web.handler;

import com.challengeteam.shop.dto.webhook.StripeResponse;
import com.challengeteam.shop.entity.order.Order;
import com.challengeteam.shop.exceptionHandling.exception.CriticalSystemException;
import com.challengeteam.shop.service.OrderService;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Map;
import java.util.Optional;

import static com.challengeteam.shop.web.handler.StripeWebhookHandlerImpl.CHECKOUT_SESSION_EXPIRED;
import static com.challengeteam.shop.web.handler.StripeWebhookHandlerImpl.PAYMENT_INTENT_SUCCEEDED;
import static com.challengeteam.shop.web.handler.StripeWebhookHandlerImplTest.TestResources.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class StripeWebhookHandlerImplTest {
    @Mock private OrderService orderService;
    @InjectMocks private StripeWebhookHandlerImpl stripeWebhookHandler;

    @Nested
    class HandleEventTest {

        @Test
        void whenEventTypeIsPaymentIntentSucceeded_thenProcessSuccessfulPayment() {
            // mockito
            Mockito.when(orderService.getById(ORDER_ID))
                    .thenReturn(Optional.of(buildNotProcessedOrder()));

            // when
            StripeResponse result = stripeWebhookHandler.handleEvent(PAYMENT_INTENT_SUCCEEDED, buildPaymentIntentWhenOrderNotProcessed());

            // then
            assertThat(result.status()).isEqualTo(HttpStatus.OK);
            Mockito.verify(orderService).makeOrderPaid(ORDER_ID, PAYMENT_INTENT_ID);
            Mockito.verify(orderService).setProcessedByWebhook(ORDER_ID, true);
        }

        @Test
        void whenEventTypeIsPaymentIntentSucceededButOrderAlreadyProcessed_thenDoNothing() {
            // mockito
            Mockito.when(orderService.getById(ORDER_ID))
                    .thenReturn(Optional.of(buildProcessedOrder()));

            // when
            StripeResponse result = stripeWebhookHandler.handleEvent(PAYMENT_INTENT_SUCCEEDED, buildSPaymentIntent());

            // then
            assertThat(result.status()).isEqualTo(HttpStatus.OK);
            Mockito.verifyNoMoreInteractions(orderService);
        }
        
        @Test
        void whenEventTypeIsPaymentIntentSucceededButNotFoundOrder_thenThrowException() {
            // mockito
            Mockito.when(orderService.getById(ORDER_ID))
                    .thenReturn(Optional.empty());

            // when + then
            assertThatThrownBy(() -> stripeWebhookHandler.handleEvent(PAYMENT_INTENT_SUCCEEDED, buildSPaymentIntent()))
                    .isInstanceOf(CriticalSystemException.class);
        }

        @Test
        void whenEventTypeIsCheckoutSessionExpired_thenProcessFailedPayment() {
            // mockito
            Mockito.when(orderService.getById(ORDER_ID))
                    .thenReturn(Optional.of(buildNotProcessedOrder()));

            // when
            StripeResponse result = stripeWebhookHandler.handleEvent(CHECKOUT_SESSION_EXPIRED, buildSession());

            // then
            assertThat(result.status()).isEqualTo(HttpStatus.OK);
            Mockito.verify(orderService).makeOrderFailed(ORDER_ID);
            Mockito.verify(orderService).setProcessedByWebhook(ORDER_ID, true);
        }

        @Test
        void whenEventTypeIsCheckoutSessionExpiredButOrderAlreadyProcessed_thenDoNothing() {
            // mockito
            Mockito.when(orderService.getById(ORDER_ID))
                    .thenReturn(Optional.of(buildProcessedOrder()));

            // when
            StripeResponse result = stripeWebhookHandler.handleEvent(CHECKOUT_SESSION_EXPIRED, buildSession());

            // then
            assertThat(result.status()).isEqualTo(HttpStatus.OK);
            Mockito.verifyNoMoreInteractions(orderService);
        }

        @Test
        void whenEventTypeIsCheckoutSessionExpiredButNotFoundOrder_thenThrowException() {
            // mockito
            Mockito.when(orderService.getById(ORDER_ID))
                    .thenReturn(Optional.empty());

            // when + then
            assertThatThrownBy(() -> stripeWebhookHandler.handleEvent(CHECKOUT_SESSION_EXPIRED, buildSession()))
                    .isInstanceOf(CriticalSystemException.class);
        }

        @Test
        void whenUnexpectedEventType_thenReturnBadRequest() {
            // when
            StripeResponse result = stripeWebhookHandler.handleEvent(UNEXPECTED_EVENT_TYPE, buildStripeObject());
            
            // then
            assertThat(result.status()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    static class TestResources {
        public final static String UNEXPECTED_EVENT_TYPE = "unexpected.event.type";
        public final static long ORDER_ID = 1L;
        public final static String PAYMENT_INTENT_ID = "777";
        public final static Map<String, String> PAYMENT_INTENT_METADATA = Map.of("orderId", Long.toString(ORDER_ID));
        public final static Map<String, String> CHECKOUT_SESSION_METADATA = Map.of("orderId", Long.toString(ORDER_ID));

        public static PaymentIntent buildPaymentIntentWhenOrderNotProcessed() {
            PaymentIntent intent = Mockito.mock(PaymentIntent.class);
            Mockito.when(intent.getId())
                    .thenReturn(PAYMENT_INTENT_ID);
            Mockito.when(intent.getMetadata())
                    .thenReturn(PAYMENT_INTENT_METADATA);

            return intent;
        }
        
        public static PaymentIntent buildSPaymentIntent() {
            PaymentIntent intent = Mockito.mock(PaymentIntent.class);
            Mockito.when(intent.getMetadata())
                    .thenReturn(PAYMENT_INTENT_METADATA);

            return intent;
        }

        public static Order buildNotProcessedOrder() {
            return Order.builder()
                    .processedByWebhook(false)
                    .build();
        }

        public static Order buildProcessedOrder() {
            return Order.builder()
                    .processedByWebhook(true)
                    .build();
        }
        public static Session buildsdSession() {
            Session session = Mockito.mock(Session.class);
            Mockito.when(session.getMetadata())
                    .thenReturn(CHECKOUT_SESSION_METADATA);

            return session;
        }

        public static Session buildSession() {
            Session session = Mockito.mock(Session.class);
            Mockito.when(session.getMetadata())
                    .thenReturn(CHECKOUT_SESSION_METADATA);

            return session;
        }
        
        public static StripeObject buildStripeObject() {
            return Mockito.mock(StripeObject.class);
        }
    }
}