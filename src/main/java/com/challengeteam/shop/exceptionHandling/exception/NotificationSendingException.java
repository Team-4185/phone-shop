package com.challengeteam.shop.exceptionHandling.exception;

public class NotificationSendingException extends RuntimeException {

    public NotificationSendingException(String message) {
        super(message);
    }
}