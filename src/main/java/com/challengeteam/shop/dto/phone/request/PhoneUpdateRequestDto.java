package com.challengeteam.shop.dto.phone.request;

import com.challengeteam.shop.entity.phone.PhoneColor;
import com.challengeteam.shop.entity.phone.ProductStatus;
import com.challengeteam.shop.entity.phone.StorageCapacity;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.Set;

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

        @Size(min = 3, max = 64, message = "Sku must be between {min} and {max} characters")
        @Pattern(
                regexp = "^[A-Z0-9]+(?:-[A-Z0-9]+)*$",
                message = "Sku must contain only uppercase letters, numbers and hyphens"
        )
        String sku,

        @Min(value = 0, message = "Stock must be greater than or equal to {value}")
        Integer stock,

        ProductStatus status,

        @Pattern(regexp = "^[A-Za-z0-9\\s\\-]+$", message = "CPU must contain only letters, numbers, spaces and hyphens")
        @Size(max = 50, message = "CPU must be at most {max} characters long")
        String cpu,

        @Min(value = 1, message = "Number of cores must be at least {value}")
        @Max(value = 32, message = "Number of cores must be at most {value}")
        Integer coresNumber,

        @Pattern(regexp = "^\\d+(\\.\\d+)?\"$", message = "Format: 6.7\"")
        @Size(max = 5, message = "Screen size must be at most {max} characters long")
        String screenSize,

        @Pattern(regexp = "^\\d+ MP$", message = "Format: 12 MP")
        @Size(max = 10, message = "Front camera must be at most {max} characters long")
        String frontCamera,

        @Pattern(regexp = "^\\d+(-\\d+)* MP$", message = "Format: 48-12-12 MP")
        @Size(max = 20, message = "Main camera must be at most {max} characters long")
        String mainCamera,

        @Pattern(regexp = "^\\d+ mAh$", message = "Format: 4323 mAh")
        @Size(max = 10, message = "Battery capacity must be at most {max} characters long")
        String batteryCapacity,

        Set<PhoneColor> colors,

        Set<StorageCapacity> storageCapacities
) {
}