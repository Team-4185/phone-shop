package com.challengeteam.shop.service.payment.stripe;

import com.challengeteam.shop.dto.order.request.payment.PaymentDetailsRequestDto;
import com.challengeteam.shop.dto.payment.TransactionResult;
import com.challengeteam.shop.entity.order.payment.PaymentStatus;
import com.challengeteam.shop.exceptionHandling.exception.CriticalSystemException;
import com.challengeteam.shop.exceptionHandling.exception.InvalidAPIRequestException;
import com.challengeteam.shop.properties.StripeProperties;
import com.challengeteam.shop.service.payment.PaymentProvider;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;
import com.stripe.model.Refund;
import com.stripe.net.RequestOptions;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.PaymentMethodCreateParams;
import com.stripe.param.RefundCreateParams;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class StripePaymentProvider implements PaymentProvider {
    public static final String PROVIDER_CODE = "stripe";
    private static final int CURRENCY_FRACTION_DIGITS = 2;

    private final StripeProperties stripeProperties;
    private final StripeClientGateway stripeClientGateway;

    @Override
    public String providerCode() {
        return PROVIDER_CODE;
    }

    @Override
    public TransactionResult pay(PaymentDetailsRequestDto paymentDetails, BigDecimal amount) {
        validateConfiguration();
        try {
            PaymentMethod paymentMethod = stripeClientGateway.createPaymentMethod(
                    buildPaymentMethodParams(paymentDetails), requestOptions());
            PaymentIntent paymentIntent = stripeClientGateway.createPaymentIntent(
                    buildPaymentIntentParams(paymentMethod.getId(), amount), requestOptions());

            PaymentStatus paymentStatus = toPaymentStatus(paymentIntent.getStatus());
            return new TransactionResult(
                    paymentStatus,
                    paymentIntent.getId(),
                    paymentStatus == PaymentStatus.PAID ? null : paymentFailureMessage(paymentIntent));
        } catch (StripeException e) {
            log.warn("Stripe payment failed: {}", e.getMessage());
            return new TransactionResult(PaymentStatus.FAILED, null, e.getMessage());
        }
    }

    @Override
    public TransactionResult refund(String transactionId, BigDecimal amount) {
        validateConfiguration();
        try {
            Refund refund = stripeClientGateway.createRefund(
                    RefundCreateParams.builder()
                            .setPaymentIntent(transactionId)
                            .setAmount(toMinorUnits(amount))
                            .build(),
                    requestOptions());

            PaymentStatus refundStatus = toRefundStatus(refund.getStatus());
            return new TransactionResult(
                    refundStatus,
                    refund.getId(),
                    refundStatus == PaymentStatus.REFUNDED ? null : refundFailureMessage(refund));
        } catch (StripeException e) {
            log.warn("Stripe refund failed for paymentIntent={}: {}", transactionId, e.getMessage());
            return new TransactionResult(PaymentStatus.FAILED, transactionId, e.getMessage());
        }
    }

    private PaymentMethodCreateParams buildPaymentMethodParams(PaymentDetailsRequestDto paymentDetails) {
        return PaymentMethodCreateParams.builder()
                .setType(PaymentMethodCreateParams.Type.CARD)
                .setCard(PaymentMethodCreateParams.CardDetails.builder()
                        .setNumber(paymentDetails.cardNumber())
                        .setExpMonth((long) paymentDetails.cardMonthExpiration())
                        .setExpYear((long) paymentDetails.cardYearExpiration())
                        .setCvc(paymentDetails.cardCvv())
                        .build())
                .setBillingDetails(PaymentMethodCreateParams.BillingDetails.builder()
                        .setName(paymentDetails.cardHoldName())
                        .build())
                .build();
    }

    private PaymentIntentCreateParams buildPaymentIntentParams(String paymentMethodId, BigDecimal amount) {
        return PaymentIntentCreateParams.builder()
                .setAmount(toMinorUnits(amount))
                .setCurrency(normalizedCurrency())
                .setPaymentMethod(paymentMethodId)
                .setConfirm(true)
                .addPaymentMethodType("card")
                .build();
    }

    private Long toMinorUnits(BigDecimal amount) {
        return amount.setScale(CURRENCY_FRACTION_DIGITS, RoundingMode.HALF_UP)
                .movePointRight(CURRENCY_FRACTION_DIGITS)
                .longValueExact();
    }

    private PaymentStatus toPaymentStatus(String stripeStatus) {
        if ("succeeded".equals(stripeStatus)) {
            return PaymentStatus.PAID;
        }
        if ("requires_payment_method".equals(stripeStatus) || "canceled".equals(stripeStatus)) {
            return PaymentStatus.FAILED;
        }
        return PaymentStatus.PENDING;
    }

    private PaymentStatus toRefundStatus(String stripeStatus) {
        if (stripeStatus == null || "succeeded".equals(stripeStatus)) {
            return PaymentStatus.REFUNDED;
        }
        if ("failed".equals(stripeStatus) || "canceled".equals(stripeStatus)) {
            return PaymentStatus.FAILED;
        }
        return PaymentStatus.PENDING;
    }

    private String paymentFailureMessage(PaymentIntent paymentIntent) {
        if (paymentIntent.getLastPaymentError() != null
                && paymentIntent.getLastPaymentError().getMessage() != null
                && !paymentIntent.getLastPaymentError().getMessage().isBlank()) {
            return paymentIntent.getLastPaymentError().getMessage();
        }
        if (paymentIntent.getCancellationReason() != null
                && !paymentIntent.getCancellationReason().isBlank()) {
            return paymentIntent.getCancellationReason();
        }
        return "Stripe payment status: " + paymentIntent.getStatus();
    }

    private String refundFailureMessage(Refund refund) {
        if (refund.getFailureReason() != null && !refund.getFailureReason().isBlank()) {
            return refund.getFailureReason();
        }
        return "Stripe refund status: " + refund.getStatus();
    }

    private RequestOptions requestOptions() {
        return RequestOptions.builder()
                .setApiKey(stripeProperties.getSecretKey())
                .build();
    }

    private String normalizedCurrency() {
        return stripeProperties.getCurrency().toLowerCase(Locale.ROOT);
    }

    private void validateConfiguration() {
        if (stripeProperties.getSecretKey() == null || stripeProperties.getSecretKey().isBlank()) {
            throw new CriticalSystemException("Stripe secret key is not configured");
        }
        if (stripeProperties.getCurrency() == null || stripeProperties.getCurrency().isBlank()) {
            throw new InvalidAPIRequestException("Stripe currency is not configured");
        }
    }
}
