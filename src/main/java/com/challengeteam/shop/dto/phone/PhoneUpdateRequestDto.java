package com.challengeteam.shop.dto.phone;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record PhoneUpdateRequestDto(

        @Size(min = 3, max = 255, message = "Name must be between {min} and {max} characters")
        String newName,

        @Size(max = 1000, message = "Description must be at most {max} characters long")
        String newDescription,

        @DecimalMin(value = "0.00", message = "Price must be greater than {value}")
        BigDecimal newPrice,

        @Size(min = 3, max = 255, message = "Brand must be between {min} and {max} characters")
        String newBrand,

        @Min(value = 1970, message = "Release year must be no earlier than {value}")
        @Max(value = 2026, message = "Release year must be no later than {value}")
        Integer newReleaseYear
) {
}
