package com.challengeteam.shop.dto.phone;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record PhoneUpdateRequestDto(

        @NotBlank(message = "Name must not be empty")
        @Size(min = 3, max = 255, message = "Name must be between 3 and 255 characters")
        String newName,

        @Size(max = 1000, message = "Description must be at most 1000 characters long")
        String newDescription,

        @NotNull(message = "Price must not be null")
        @DecimalMin(value = "0.00", message = "Price must be greater than 0")
        BigDecimal newPrice,

        @NotBlank(message = "Brand must not be empty")
        @Size(min = 3, max = 255, message = "Brand must be between 3 and 255 characters")
        String newBrand,

        @NotNull(message = "Release year must not be null")
        @Min(value = 1970, message = "Release year must be no earlier than 1970")
        @Max(value = 2026, message = "Release year must not be in the future")
        Integer newReleaseYear
) {
}
