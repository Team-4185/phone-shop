package com.challengeteam.shop.exceptionHandling.exception.order;

public class PaymentFailedException extends RuntimeException {
    public PaymentFailedException(String message) {
        super(message);
    }
}