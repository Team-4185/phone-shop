package com.challengeteam.shop.constraints.order.validation.validator;

import com.challengeteam.shop.constraints.order.validation.annotation.delivery.ValidateCorrectDeliveryDetails;
import com.challengeteam.shop.constraints.order.validation.annotation.delivery.group.CourierGroupValidation;
import com.challengeteam.shop.constraints.order.validation.annotation.delivery.group.PostOfficeGroupValidation;
import com.challengeteam.shop.dto.order.request.OrderDetailsRequest;
import com.challengeteam.shop.dto.order.request.order.OrderRequestDto;
import com.challengeteam.shop.dto.order.request.shippingAddress.ShippingAddressRequestDto;
import com.challengeteam.shop.entity.order.DeliveryMethod;
import jakarta.validation.*;

import java.util.Set;

/**
 * Validator for order delivery details based on the selected delivery method.
 * <p>
 * This validator ensures that the shipping address provided in an {@link OrderRequestDto}
 * is consistent with the selected {@link DeliveryMethod}:
 * </p>
 * <ul>
 *   <li><b>PICKUP:</b> Shipping address must be null</li>
 *   <li><b>COURIER:</b> Shipping address must be present and validated against {@link CourierGroupValidation}</li>
 *   <li><b>POST_OFFICE:</b> Shipping address must be present and validated against {@link PostOfficeGroupValidation}</li>
 * </ul>
 *
 * @see ValidateCorrectDeliveryDetails
 * @see OrderRequestDto
 * @see DeliveryMethod
 */
public class OrderDeliveryDetailsValidator
        implements ConstraintValidator<ValidateCorrectDeliveryDetails, OrderDetailsRequest> {
    /**
     * The validator instance used for validating shipping address with specific validation groups.
     */
    private Validator validator;

    /**
     * Initializes the validator by creating a default validator factory.
     *
     * @param constraintAnnotation the annotation instance for the constraint declaration
     */
    @Override
    public void initialize(ValidateCorrectDeliveryDetails constraintAnnotation) {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
    }

    /**
     * Validates that the delivery details are correct based on the delivery method.
     * <p>
     * Validation logic:
     * </p>
     * <ul>
     *   <li>If delivery method is PICKUP, shipping address must be null</li>
     *   <li>For COURIER or POST_OFFICE, shipping address must be present</li>
     *   <li>The shipping address is validated against the appropriate validation group based on the delivery method</li>
     *   <li>Any validation violations are added to the context with the property path prefixed with "shippingAddress."</li>
     * </ul>
     *
     * @param value   the {@link OrderRequestDto} to validate
     * @param context the constraint validator context
     * @return {@code true} if the delivery details are valid, {@code false} otherwise
     */
    @Override
    public boolean isValid(OrderDetailsRequest value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        DeliveryMethod method = value.deliveryMethod();
        ShippingAddressRequestDto address = value.shippingAddress();
        if (method == null) {
            return true;
        }
        if (method == DeliveryMethod.PICKUP) {
            return address == null;
        }
        if (address == null) {
            return false;
        }
        Class<?> group = switch (method) {
            case COURIER -> CourierGroupValidation.class;
            case POST_OFFICE -> PostOfficeGroupValidation.class;
            default -> null;
        };
        Set<ConstraintViolation<ShippingAddressRequestDto>> violations =
                validator.validate(address, group);
        if (!violations.isEmpty()) {
            context.disableDefaultConstraintViolation();
            violations.forEach(v ->
                    context.buildConstraintViolationWithTemplate(v.getMessage())
                            .addPropertyNode("shippingAddress." + v.getPropertyPath())
                            .addConstraintViolation()
            );
            return false;
        }
        return true;
    }
}
