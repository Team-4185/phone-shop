package com.challengeteam.shop.constraints.filter.validation.annotation;

import com.challengeteam.shop.constraints.filter.validation.validator.MinPriceNotExceedMaxPriceValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom validation annotation to ensure that minimum price does not exceed maximum price.
 * <p>
 * This constraint is applied at the type level (typically on DTOs or request objects)
 * to validate that the minimum price field is less than or equal to the maximum price field.
 * </p>
 * <p>
 * The validation logic is implemented in {@link MinPriceNotExceedMaxPriceValidator}.
 * If either minPrice or maxPrice is null, the validation passes. Otherwise, it checks
 * that minPrice <= maxPrice.
 * </p>
 *
 * <p><strong>Example usage:</strong></p>
 * <pre>
 * &#64;MinPriceNotExceedMaxPrice
 * public record PhoneFilterRequest(
 *     BigDecimal minPrice,
 *     BigDecimal maxPrice
 * ) { }
 * </pre>
 *
 * @see MinPriceNotExceedMaxPriceValidator
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MinPriceNotExceedMaxPriceValidator.class)
public @interface MinPriceNotExceedMaxPrice {
    String message() default "min price must be less than or equal to max price";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}