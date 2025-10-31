package com.challengeteam.shop.exceptionHandling.exception;

public class FileUtilityException extends Exception {
    public FileUtilityException() {
    }

    public FileUtilityException(String message) {
        super(message);
    }

    public FileUtilityException(String message, Throwable cause) {
        super(message, cause);
    }

    public FileUtilityException(Throwable cause) {
        super(cause);
    }

    public FileUtilityException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
