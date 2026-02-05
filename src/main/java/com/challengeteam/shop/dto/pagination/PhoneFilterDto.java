package com.challengeteam.shop.dto.pagination;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record PhoneFilterDto(
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
