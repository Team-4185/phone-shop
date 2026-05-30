package com.challengeteam.shop.exceptionHandling.exception.phone;

public class PhoneNotFoundException extends RuntimeException {
    public PhoneNotFoundException(String message) {
        super(message);
    }
}