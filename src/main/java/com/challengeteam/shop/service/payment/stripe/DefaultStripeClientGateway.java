package com.challengeteam.shop.service.payment.stripe;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;
import com.stripe.model.Refund;
import com.stripe.net.RequestOptions;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.PaymentMethodCreateParams;
import com.stripe.param.RefundCreateParams;
import org.springframework.stereotype.Component;

@Component
public class DefaultStripeClientGateway implements StripeClientGateway {
    @Override
    public PaymentMethod createPaymentMethod(
            PaymentMethodCreateParams params, RequestOptions requestOptions) throws StripeException {
        return PaymentMethod.create(params, requestOptions);
    }

    @Override
    public PaymentIntent createPaymentIntent(
            PaymentIntentCreateParams params, RequestOptions requestOptions) throws StripeException {
        return PaymentIntent.create(params, requestOptions);
    }

    @Override
    public Refund createRefund(RefundCreateParams params, RequestOptions requestOptions) throws StripeException {
        return Refund.create(params, requestOptions);
    }

    @Override
    public Event constructWebhookEvent(
            String payload, String signatureHeader, String webhookSecret) throws SignatureVerificationException {
        return Webhook.constructEvent(payload, signatureHeader, webhookSecret);
    }
}
