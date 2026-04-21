package com.challengeteam.shop.exceptionHandling.exception;

public class InvalidPriceRangeException extends RuntimeException {
    public InvalidPriceRangeException() {}

    public InvalidPriceRangeException(String message) {
        super(message);
    }

    public InvalidPriceRangeException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidPriceRangeException(Throwable cause) {
        super(cause);
    }

    public InvalidPriceRangeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
