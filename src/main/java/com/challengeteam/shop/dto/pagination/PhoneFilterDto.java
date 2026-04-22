package com.challengeteam.shop.dto.pagination;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record PhoneFilterDto(
        @Size(max = 255, message = "Brand must be at most {max} characters long")
        String brand,

        @DecimalMin(value = "0.0", message = "Minimum price cannot be negative")
        BigDecimal minPrice,

        @DecimalMin(value = "0.0", message = "Maximum price cannot be negative")
        BigDecimal maxPrice,

        @Pattern(
                regexp = "name_asc|name_desc|price_asc|price_desc",
                message = "Sort must be one of: name_asc, name_desc, price_asc, price_desc"
        )
        String sort
) {
    public PhoneFilterDto {
        sort = (sort == null || sort.isBlank()) ? "name_asc" : sort;
    }
}
