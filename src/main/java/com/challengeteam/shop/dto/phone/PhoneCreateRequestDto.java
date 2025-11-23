package com.challengeteam.shop.dto.phone;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record PhoneCreateRequestDto(

        @NotBlank(message = "Name must not be empty")
        @Size(min = 3, max = 255, message = "Name must be between {min} and {max} characters")
        String name,

        @Size(max = 1000, message = "Description must be at most {max} characters long")
        String description,

        @NotNull(message = "Price must not be null")
        @DecimalMin(value = "0.00", message = "Price must be greater than {value}")
        BigDecimal price,

        @NotBlank(message = "Brand must not be empty")
        @Size(min = 3, max = 255, message = "Brand must be between {min} and {max} characters")
        String brand,

        @NotNull(message = "Release year must not be null")
        @Min(value = 1970, message = "Release year must be no earlier than {value}")
        @Max(value = 2026, message = "Release year must be no later than {value}")
        Integer releaseYear
) {
}
