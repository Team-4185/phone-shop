package com.challengeteam.shop.exceptionHandling.exception.security;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}