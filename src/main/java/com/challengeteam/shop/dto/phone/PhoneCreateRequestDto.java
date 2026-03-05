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
        Integer releaseYear,

        @NotBlank(message = "Cpu must not be empty")
        @Pattern(regexp = "^[A-Za-z0-9\\s\\-]+$", message = "CPU must contain only letters, numbers, spaces and hyphens")
        @Size(max = 50, message = "CPU must be at most {max} characters long")
        String cpu,

        @NotNull(message = "Cores number must be not null")
        @Min(value = 1, message = "Number of cores must be at least {value}")
        @Max(value = 32, message = "Number of cores must be at most {value}")
        Integer coresNumber,

        @NotBlank(message = "Screen size must not be empty")
        @Pattern(regexp = "^\\d+(\\.\\d+)?\"$", message = "Screen size must be in format: number followed by \" (e.g., 6.7\")")
        String screenSize,

        @NotBlank(message = "Front camera must not be empty")
        @Pattern(regexp = "^\\d+\\s*MP$", message = "Front camera must be in format: number followed by MP (e.g., 12 MP)")
        String frontCamera,

        @NotBlank(message = "Main camera must not be empty")
        @Pattern(regexp = "^\\d+(-\\d+)*\\s*(-\\d+\\s*)?MP$", message = "Main camera must be in format: numbers separated by hyphens followed by MP (e.g., 48-12-12 MP)")
        String mainCamera,

        @NotBlank(message = "Battery capacity must not be empty")
        @Pattern(regexp = "^\\d+\\s*mAh$", message = "Battery capacity must be in format: number followed by mAh (e.g., 4323 mAh)")
        String batteryCapacity
) {
}
