package com.challengeteam.shop.dto.cart;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CartItemAddRequestDto(
        @NotNull(message = "Phone ID must be not null")
        Long phoneId,
        @NotNull(message = "Amount must be not null")
        @Min(value = 1, message = "Amount must be at least {value}")
        @Max(value = 20, message = "Amount must not be greater than {value}")
        Integer amount
) {
}
