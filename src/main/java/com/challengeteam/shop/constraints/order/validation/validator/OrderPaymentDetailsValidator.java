package com.challengeteam.shop.constraints.order.validation.validator;

import com.challengeteam.shop.constraints.order.validation.annotation.ValidateCorrectPaymentDetails;
import com.challengeteam.shop.dto.order.request.order.OrderRequestDto;
import com.challengeteam.shop.dto.order.request.payment.PaymentDetailsRequestDto;
import com.challengeteam.shop.entity.order.payment.PaymentMethod;
import jakarta.validation.*;

import java.util.Set;

/**
 * Validator for order payment details based on the selected payment method.
 * <p>
 * This validator ensures that the payment details provided in an {@link OrderRequestDto}
 * are consistent with the selected {@link PaymentMethod}:
 * </p>
 * <ul>
 *   <li><b>CASH_ON_DELIVERY:</b> Payment details must be null</li>
 *   <li><b>CARD:</b> Payment details must be present and validated according to {@link PaymentDetailsRequestDto} constraints</li>
 * </ul>
 *
 * @see ValidateCorrectPaymentDetails
 * @see OrderRequestDto
 * @see PaymentMethod
 */
public class OrderPaymentDetailsValidator
        implements ConstraintValidator<ValidateCorrectPaymentDetails, OrderRequestDto> {

    /**
     * The validator instance used for validating payment details.
     */
    private Validator validator;

    /**
     * Initializes the validator by creating a default validator factory.
     *
     * @param constraintAnnotation the annotation instance for the constraint declaration
     */
    @Override
    public void initialize(ValidateCorrectPaymentDetails constraintAnnotation) {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
    }

    /**
     * Validates that the payment details are correct based on the payment method.
     * <p>
     * Validation logic:
     * </p>
     * <ul>
     *   <li>If payment method is CASH_ON_DELIVERY, payment details must be null</li>
     *   <li>If payment method is CARD, payment details must be present and valid</li>
     *   <li>The payment details are validated against all constraints defined in {@link PaymentDetailsRequestDto}</li>
     *   <li>Any validation violations are added to the context with the property path prefixed with "paymentDetails."</li>
     * </ul>
     *
     * @param value   the {@link OrderRequestDto} to validate
     * @param context the constraint validator context
     * @return {@code true} if the payment details are valid, {@code false} otherwise
     */
    @Override
    public boolean isValid(OrderRequestDto value, ConstraintValidatorContext context) {
        if (value.paymentMethod() == PaymentMethod.CASH_ON_DELIVERY) {
            return value.paymentDetails() == null;
        }
        if (value.paymentMethod() == PaymentMethod.CARD) {
            if (value.paymentDetails() == null) {
                return false;
            }
            Set<ConstraintViolation<PaymentDetailsRequestDto>> violations =
                    validator.validate(value.paymentDetails());
            if (!violations.isEmpty()) {
                context.disableDefaultConstraintViolation();
                violations.forEach(v ->
                        context.buildConstraintViolationWithTemplate(v.getMessage())
                                .addPropertyNode("paymentDetails." + v.getPropertyPath())
                                .addConstraintViolation()
                );
                return false;
            }
        }
        return true;
    }
}