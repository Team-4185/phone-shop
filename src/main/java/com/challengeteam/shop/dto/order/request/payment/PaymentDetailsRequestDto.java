package com.challengeteam.shop.dto.order.request.payment;

import com.challengeteam.shop.entity.order.payment.PaymentDetails;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.io.Serializable;

/**
 * DTO for {@link PaymentDetails}
 */
public record PaymentDetailsRequestDto(
        @NotBlank(message = "Cardholder name must be present")
        @Pattern(regexp = "^[a-zA-Z ]{2,26}$",
                message = "Cardholder name must contain 2-26 Latin letters and spaces")
        String cardHoldName,
        @NotBlank(message = "Card number must be present")
        @Pattern(regexp = "^[0-9]{13,19}$",
                message = "Card number must be between 13 and 19 digits")
        String cardNumber,
        @Min(value = 1, message = "Month must be between 01 and 12")
        @Max(value = 12, message = "Month must be between 01 and 12")
        int cardMonthExpiration,
        @Min(value = 2024, message = "Year must be current or future")
        @Max(value = 2099, message = "Invalid expiration year")
        int cardYearExpiration,
        @NotBlank(message = "CVV must be present")
        @Pattern(regexp = "^[0-9]{3,4}$",
                message = "CVV must be 3 or 4 digits")
        String cardCvv
) implements Serializable {
}