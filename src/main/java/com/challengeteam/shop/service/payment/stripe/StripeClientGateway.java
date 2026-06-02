package com.challengeteam.shop.service.payment.stripe;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;
import com.stripe.model.Refund;
import com.stripe.net.RequestOptions;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.PaymentMethodCreateParams;
import com.stripe.param.RefundCreateParams;

public interface StripeClientGateway {
    PaymentMethod createPaymentMethod(
            PaymentMethodCreateParams params, RequestOptions requestOptions) throws StripeException;

    PaymentIntent createPaymentIntent(
            PaymentIntentCreateParams params, RequestOptions requestOptions) throws StripeException;

    Refund createRefund(RefundCreateParams params, RequestOptions requestOptions) throws StripeException;

    Event constructWebhookEvent(
            String payload, String signatureHeader, String webhookSecret) throws SignatureVerificationException;
}
