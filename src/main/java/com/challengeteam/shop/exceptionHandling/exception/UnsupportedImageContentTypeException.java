package com.challengeteam.shop.exceptionHandling.exception;

import lombok.Getter;

@Getter
public class UnsupportedImageContentTypeException extends RuntimeException {
    private String contentType;

    public UnsupportedImageContentTypeException(String contentType) {
        super();
        this.contentType = contentType;
    }

    public UnsupportedImageContentTypeException(String contentType, String message) {
        super(message);
        this.contentType = contentType;
    }

    public UnsupportedImageContentTypeException(String contentType, String message, Throwable cause) {
        super(message, cause);
        this.contentType = contentType;
    }

    public UnsupportedImageContentTypeException(String contentType, Throwable cause) {
        super(cause);
        this.contentType = contentType;
    }

    public UnsupportedImageContentTypeException(String contentType, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.contentType = contentType;
    }
}
