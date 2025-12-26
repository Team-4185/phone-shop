package com.challengeteam.shop.exceptionHandling.exception;

public class InvalidCartItemAmountException extends RuntimeException {

    public InvalidCartItemAmountException() {}

    public InvalidCartItemAmountException(String message) {
        super(message);
    }

    public InvalidCartItemAmountException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidCartItemAmountException(Throwable cause) {
        super(cause);
    }

    public InvalidCartItemAmountException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
