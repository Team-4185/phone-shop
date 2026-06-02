package com.challengeteam.shop.service.payment.stripe;

import com.challengeteam.shop.dto.order.request.payment.PaymentDetailsRequestDto;
import com.challengeteam.shop.dto.payment.TransactionResult;
import com.challengeteam.shop.entity.order.payment.PaymentStatus;
import com.challengeteam.shop.exceptionHandling.exception.CriticalSystemException;
import com.challengeteam.shop.properties.StripeProperties;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;
import com.stripe.model.Refund;
import com.stripe.net.RequestOptions;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.PaymentMethodCreateParams;
import com.stripe.param.RefundCreateParams;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StripePaymentProviderTest {
    @Mock
    private StripeClientGateway stripeClientGateway;

    @Test
    void shouldCreateConfirmedPaymentIntentAndReturnPaidResult() throws Exception {
        StripePaymentProvider stripePaymentProvider = stripePaymentProvider();
        PaymentMethod paymentMethod = new PaymentMethod();
        paymentMethod.setId("pm_test");
        PaymentIntent paymentIntent = new PaymentIntent();
        paymentIntent.setId("pi_test");
        paymentIntent.setStatus("succeeded");

        when(stripeClientGateway.createPaymentMethod(any(PaymentMethodCreateParams.class), any(RequestOptions.class)))
                .thenReturn(paymentMethod);
        when(stripeClientGateway.createPaymentIntent(any(PaymentIntentCreateParams.class), any(RequestOptions.class)))
                .thenReturn(paymentIntent);

        TransactionResult result = stripePaymentProvider.pay(paymentDetails(), BigDecimal.valueOf(123.45));

        assertThat(result.paymentStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(result.transactionId()).isEqualTo("pi_test");

        ArgumentCaptor<PaymentIntentCreateParams> paramsCaptor =
                ArgumentCaptor.forClass(PaymentIntentCreateParams.class);
        verify(stripeClientGateway).createPaymentIntent(paramsCaptor.capture(), any(RequestOptions.class));
        assertThat(paramsCaptor.getValue().getAmount()).isEqualTo(12345L);
        assertThat(paramsCaptor.getValue().getCurrency()).isEqualTo("usd");
        assertThat(paramsCaptor.getValue().getPaymentMethod()).isEqualTo("pm_test");
        assertThat(paramsCaptor.getValue().getConfirm()).isTrue();
    }

    @Test
    void shouldReturnPendingWhenStripePaymentRequiresAdditionalAction() throws Exception {
        StripePaymentProvider stripePaymentProvider = stripePaymentProvider();
        PaymentMethod paymentMethod = new PaymentMethod();
        paymentMethod.setId("pm_test");
        PaymentIntent paymentIntent = new PaymentIntent();
        paymentIntent.setId("pi_test");
        paymentIntent.setStatus("requires_action");

        when(stripeClientGateway.createPaymentMethod(any(PaymentMethodCreateParams.class), any(RequestOptions.class)))
                .thenReturn(paymentMethod);
        when(stripeClientGateway.createPaymentIntent(any(PaymentIntentCreateParams.class), any(RequestOptions.class)))
                .thenReturn(paymentIntent);

        TransactionResult result = stripePaymentProvider.pay(paymentDetails(), BigDecimal.TEN);

        assertThat(result.paymentStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(result.transactionId()).isEqualTo("pi_test");
        assertThat(result.errorMessage()).isEqualTo("Stripe payment status: requires_action");
    }

    @Test
    void shouldCreateRefundForPaymentIntent() throws Exception {
        StripePaymentProvider stripePaymentProvider = stripePaymentProvider();
        Refund refund = new Refund();
        refund.setId("re_test");
        refund.setStatus("succeeded");

        when(stripeClientGateway.createRefund(any(RefundCreateParams.class), any(RequestOptions.class)))
                .thenReturn(refund);

        TransactionResult result = stripePaymentProvider.refund("pi_test", BigDecimal.TEN);

        assertThat(result.paymentStatus()).isEqualTo(PaymentStatus.REFUNDED);
        assertThat(result.transactionId()).isEqualTo("re_test");

        ArgumentCaptor<RefundCreateParams> paramsCaptor = ArgumentCaptor.forClass(RefundCreateParams.class);
        verify(stripeClientGateway).createRefund(paramsCaptor.capture(), any(RequestOptions.class));
        assertThat(paramsCaptor.getValue().getPaymentIntent()).isEqualTo("pi_test");
        assertThat(paramsCaptor.getValue().getAmount()).isEqualTo(1000L);
    }

    @Test
    void shouldReturnFailedWhenStripeRefundFails() throws Exception {
        StripePaymentProvider stripePaymentProvider = stripePaymentProvider();
        Refund refund = new Refund();
        refund.setId("re_test");
        refund.setStatus("failed");
        refund.setFailureReason("lost_or_stolen_card");

        when(stripeClientGateway.createRefund(any(RefundCreateParams.class), any(RequestOptions.class)))
                .thenReturn(refund);

        TransactionResult result = stripePaymentProvider.refund("pi_test", BigDecimal.TEN);

        assertThat(result.paymentStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(result.transactionId()).isEqualTo("re_test");
        assertThat(result.errorMessage()).isEqualTo("lost_or_stolen_card");
    }

    @Test
    void shouldFailFastWhenSecretKeyIsMissing() {
        StripeProperties stripeProperties = new StripeProperties();
        StripePaymentProvider stripePaymentProvider =
                new StripePaymentProvider(stripeProperties, stripeClientGateway);

        assertThatThrownBy(() -> stripePaymentProvider.pay(paymentDetails(), BigDecimal.TEN))
                .isInstanceOf(CriticalSystemException.class)
                .hasMessage("Stripe secret key is not configured");
    }

    private StripePaymentProvider stripePaymentProvider() {
        StripeProperties stripeProperties = new StripeProperties();
        stripeProperties.setSecretKey("sk_test");
        stripeProperties.setCurrency("USD");
        return new StripePaymentProvider(stripeProperties, stripeClientGateway);
    }

    private PaymentDetailsRequestDto paymentDetails() {
        return new PaymentDetailsRequestDto("John Doe", "4242424242424242", 12, 2030, "123");
    }
}
