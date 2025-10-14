package com.challengeteam.shop.exceptionHandling.exception;

public class CriticalSystemException extends RuntimeException {
    public CriticalSystemException() {
    }

    public CriticalSystemException(String message) {
        super(message);
    }

    public CriticalSystemException(String message, Throwable cause) {
        super(message, cause);
    }

    public CriticalSystemException(Throwable cause) {
        super(cause);
    }

    public CriticalSystemException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
