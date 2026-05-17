package com.challengeteam.shop.exceptionHandling.exception;

public class PhoneNotFoundException extends RuntimeException {
    public PhoneNotFoundException(String message) {
        super(message);
    }
}