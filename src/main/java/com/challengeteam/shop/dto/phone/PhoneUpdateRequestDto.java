package com.challengeteam.shop.dto.phone;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record PhoneUpdateRequestDto(

        @Size(min = 3, max = 255, message = "Name must be between {min} and {max} characters")
        String name,

        @Size(max = 1000, message = "Description must be at most {max} characters long")
        String description,

        @DecimalMin(value = "0.00", message = "Price must be greater than {value}")
        BigDecimal price,

        @Size(min = 3, max = 255, message = "Brand must be between {min} and {max} characters")
        String brand,

        @Min(value = 1970, message = "Release year must be no earlier than {value}")
        @Max(value = 2026, message = "Release year must be no later than {value}")
        Integer releaseYear,

        @Pattern(regexp = "^[A-Za-z0-9\\s\\-]+$", message = "CPU must contain only letters, numbers, spaces and hyphens")
        @Size(max = 50, message = "CPU must be at most {max} characters long")
        String cpu,

        @Min(value = 1, message = "Number of cores must be at least {value}")
        @Max(value = 32, message = "Number of cores must be at most {value}")
        Integer coresNumber,

        @Pattern(regexp = "^\\d+(\\.\\d+)?\"$", message = "Screen size must be in format: number followed by \" (e.g., 6.7\")")
        String screenSize,

        @Pattern(regexp = "^\\d+\\s*MP$", message = "Front camera must be in format: number followed by MP (e.g., 12 MP)")
        String frontCamera,

        @Pattern(regexp = "^\\d+(-\\d+)*\\s*(-\\d+\\s*)?MP$", message = "Main camera must be in format: numbers separated by hyphens followed by MP (e.g., 48-12-12 MP)")
        String mainCamera,

        @Pattern(regexp = "^\\d+\\s*mAh$", message = "Battery capacity must be in format: number followed by mAh (e.g., 4323 mAh)")
        String batteryCapacity
) {
}
