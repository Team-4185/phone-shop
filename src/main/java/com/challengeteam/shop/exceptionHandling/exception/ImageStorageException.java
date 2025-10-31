package com.challengeteam.shop.exceptionHandling.exception;

public class ImageStorageException extends Exception {
    public ImageStorageException() {
    }

    public ImageStorageException(String message) {
        super(message);
    }

    public ImageStorageException(String message, Throwable cause) {
        super(message, cause);
    }

    public ImageStorageException(Throwable cause) {
        super(cause);
    }

    public ImageStorageException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
