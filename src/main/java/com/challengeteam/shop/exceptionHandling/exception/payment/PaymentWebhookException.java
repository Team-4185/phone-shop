package com.challengeteam.shop.exceptionHandling.exception.payment;

public class PaymentWebhookException extends RuntimeException {
    public PaymentWebhookException(String message) {
        super(message);
    }

    public PaymentWebhookException(String message, Throwable cause) {
        super(message, cause);
    }
}
