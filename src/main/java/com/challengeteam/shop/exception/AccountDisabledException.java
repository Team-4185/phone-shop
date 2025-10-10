package com.challengeteam.shop.exception;

public class AccountDisabledException extends RuntimeException {
    public AccountDisabledException() {
    }

    public AccountDisabledException(String message) {
        super(message);
    }

    public AccountDisabledException(String message, Throwable cause) {
        super(message, cause);
    }

    public AccountDisabledException(Throwable cause) {
        super(cause);
    }

    public AccountDisabledException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
