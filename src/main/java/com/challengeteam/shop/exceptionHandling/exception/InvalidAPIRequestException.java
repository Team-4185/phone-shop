package com.challengeteam.shop.exceptionHandling.exception;

public class InvalidAPIRequestException extends RuntimeException {

    public InvalidAPIRequestException() {}

    public InvalidAPIRequestException(String message) {
        super(message);
    }

    public InvalidAPIRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidAPIRequestException(Throwable cause) {
        super(cause);
    }

    public InvalidAPIRequestException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
