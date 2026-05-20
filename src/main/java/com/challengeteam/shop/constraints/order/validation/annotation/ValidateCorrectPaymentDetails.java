package com.challengeteam.shop.constraints.order.validation.annotation;

import com.challengeteam.shop.constraints.order.validation.validator.OrderPaymentDetailsValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * correct payment details for CASH_ON_DELIVERY:
 * paymentDetails is null;
 * <p>
 * correct payment details for CARD:
 * paymentDetails is present;
 * cardNumber is present;
 * cardHolderName is present;
 * expirationDate is present;
 * cvv is present;
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = OrderPaymentDetailsValidator.class)
public @interface ValidateCorrectPaymentDetails {
    String message() default "payment details are incorrect";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}