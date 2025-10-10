package com.challengeteam.shop.exception;

public class EmailOrPasswordWrongException extends RuntimeException {
    public EmailOrPasswordWrongException() {
    }

    public EmailOrPasswordWrongException(String message) {
        super(message);
    }

    public EmailOrPasswordWrongException(String message, Throwable cause) {
        super(message, cause);
    }

    public EmailOrPasswordWrongException(Throwable cause) {
        super(cause);
    }

    public EmailOrPasswordWrongException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
