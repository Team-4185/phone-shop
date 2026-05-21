package com.challengeteam.shop.exceptionHandling.exception.security;

public class AccessDeniedException extends RuntimeException {
    public AccessDeniedException(String message) {
        super(message);
    }
}
