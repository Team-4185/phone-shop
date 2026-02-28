package com.challengeteam.shop.exceptionHandling.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class StripeWebhookException extends Exception {
    private HttpStatus status;

    public StripeWebhookException(HttpStatus status) {
        this.status = status;
    }

    public StripeWebhookException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }
}
