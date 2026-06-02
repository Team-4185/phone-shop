package com.challengeteam.shop.exceptionHandling.exception.user;

public class InvalidUserCredentialsException extends RuntimeException {
    public InvalidUserCredentialsException(String message) {
        super(message);
    }
}