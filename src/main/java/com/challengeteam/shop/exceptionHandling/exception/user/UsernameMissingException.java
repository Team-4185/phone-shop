package com.challengeteam.shop.exceptionHandling.exception.user;

public class UsernameMissingException extends RuntimeException {
    public UsernameMissingException(String message) {
        super(message);
    }
}