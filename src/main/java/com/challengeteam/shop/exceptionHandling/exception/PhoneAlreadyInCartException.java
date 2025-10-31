package com.challengeteam.shop.exceptionHandling.exception;

public class PhoneAlreadyInCartException extends RuntimeException {
    public PhoneAlreadyInCartException() {
    }

    public PhoneAlreadyInCartException(String message) {
        super(message);
    }

    public PhoneAlreadyInCartException(String message, Throwable cause) {
        super(message, cause);
    }

    public PhoneAlreadyInCartException(Throwable cause) {
        super(cause);
    }

    public PhoneAlreadyInCartException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
