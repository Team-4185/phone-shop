package com.challengeteam.shop.exceptionHandling.exception.order;

public class OrderCreationException extends RuntimeException {
    public OrderCreationException(String message) {
        super(message);
    }
}