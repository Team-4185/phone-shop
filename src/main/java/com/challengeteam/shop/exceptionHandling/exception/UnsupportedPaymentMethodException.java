package com.challengeteam.shop.exceptionHandling.exception;

import com.challengeteam.shop.entity.order.PaymentMethod;
import lombok.Getter;

@Getter
public class UnsupportedPaymentMethodException extends RuntimeException {
    private PaymentMethod paymentMethod;

    public UnsupportedPaymentMethodException(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public UnsupportedPaymentMethodException(PaymentMethod paymentMethod, String message) {
        super(message);
        this.paymentMethod = paymentMethod;
    }

    public UnsupportedPaymentMethodException(PaymentMethod paymentMethod, String message, Throwable cause) {
        super(message, cause);
        this.paymentMethod = paymentMethod;
    }

    public UnsupportedPaymentMethodException(PaymentMethod paymentMethod, Throwable cause) {
        super(cause);
        this.paymentMethod = paymentMethod;
    }

    public UnsupportedPaymentMethodException(PaymentMethod paymentMethod, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.paymentMethod = paymentMethod;
    }
}
