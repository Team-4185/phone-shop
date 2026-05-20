package com.challengeteam.shop.constraints.order.validation.annotation.delivery;

import com.challengeteam.shop.constraints.order.validation.validator.OrderDeliveryDetailsValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * correct shipping address for PICKUP delivery:
 * shipping address is null;
 * <p>
 * correct shipping address for POST OFFICE delivery:
 * apartmentNumber is null;
 * houseNumber is null;
 * logisticsCompany is present;
 * logisticPostOffice is present;
 * street is null;
 * city is present;
 * region is present;
 * country is present;
 * zipCode is present;
 * <p>
 * correct shipping address for COURIER delivery:
 * apartmentNumber is present;
 * houseNumber is present;
 * logisticsCompany is present;
 * logisticPostOffice is null;
 * street is present;
 * city is present;
 * region is present;
 * country is present;
 * zipCode is present;
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = OrderDeliveryDetailsValidator.class)
public @interface ValidateCorrectDeliveryDetails {
    String message() default "delivery details are incorrect";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}