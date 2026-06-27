package com.challengeteam.shop.dto.cart;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CartItemRemoveRequestDto(
        @Min(value = 1, message = "Phone ID must be at least {value}")
        Long phoneId,
        @Min(value = 1, message = "Variant ID must be at least {value}")
        Long variantId,
        @NotNull(message = "Amount must be not null")
        @Min(value = 1, message = "Amount must be at least {value}")
        @Max(value = 20, message = "Amount must not be greater than {value}")
        Integer amount
) {
    public CartItemRemoveRequestDto(Long phoneId, Integer amount) {
        this(phoneId, null, amount);
    }

    @AssertTrue(message = "Phone ID or variant ID must be provided")
    public boolean isPhoneOrVariantProvided() {
        return phoneId != null || variantId != null;
    }
}
