package com.challengeteam.shop.dto.order.request.order;

import com.challengeteam.shop.constraints.order.validation.annotation.ValidateCorrectPaymentDetails;
import com.challengeteam.shop.constraints.order.validation.annotation.delivery.ValidateCorrectDeliveryDetails;
import com.challengeteam.shop.constraints.userData.InputUserValidationRules;
import com.challengeteam.shop.dto.order.request.item.OrderItemRequestDto;
import com.challengeteam.shop.dto.order.request.payment.PaymentDetailsRequestDto;
import com.challengeteam.shop.dto.order.request.shippingAddress.ShippingAddressRequestDto;
import com.challengeteam.shop.entity.order.DeliveryMethod;
import com.challengeteam.shop.entity.order.payment.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.util.List;

/**
 * DTO for {@link com.challengeteam.shop.entity.order.Order}
 */
@ValidateCorrectPaymentDetails
@ValidateCorrectDeliveryDetails
public record OrderRequestDto(
        @NotNull(message = "Customer email is required")
        @Email(regexp = InputUserValidationRules.EMAIL_PATTERN_CONSTRAINT,
                message = "Email format is invalid")
        @Size(min = 1, max = 100, message = "Email length must be between {min} and {max}")
        String customerEmail,
        @NotNull(message = "Customer address is required")
        @Pattern(regexp = InputUserValidationRules.NAME_PATTERN_CONSTRAINT,
                message = "Please enter a valid name. Use only letters (Latin or Cyrillic), hyphens, spaces, or apostrophes. " +
                        "The name must start with a letter.")
        String customerFirstName,
        @NotNull(message = "Customer last name is required")
        @Pattern(regexp = InputUserValidationRules.NAME_PATTERN_CONSTRAINT,
                message = "Please enter a valid last name. Use only letters (Latin or Cyrillic), hyphens, spaces, or apostrophes. " +
                        "The last name must start with a letter.")
        String customerLastName,
        @NotNull(message = "Customer phone number is required")
        @Pattern(regexp = InputUserValidationRules.PHONE_NUMBER_PATTERN_CONSTRAINT,
                message = "Phone number must start with '+' followed by 7 to 15 digits")
        String customerPhoneNumber,
        @NotNull(message = "Customer payment method is required")
        PaymentMethod paymentMethod,
        PaymentDetailsRequestDto paymentDetails,
        @NotNull(message = "Customer delivery method is required")
        DeliveryMethod deliveryMethod,
        ShippingAddressRequestDto shippingAddress,
        @NotNull
        @Size(min = 1)
        @Valid
        List<OrderItemRequestDto> items) implements Serializable {
}