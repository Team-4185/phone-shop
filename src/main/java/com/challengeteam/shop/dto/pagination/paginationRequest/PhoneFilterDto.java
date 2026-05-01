package com.challengeteam.shop.dto.pagination.paginationRequest;

import com.challengeteam.shop.constraints.filter.FilterRequestConstraints;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record PhoneFilterDto(
        @Size(max = 255, message = "Brand must be at most {max} characters long")
        String brand,

        @DecimalMin(value = FilterRequestConstraints.MIN_PHONE_PRICE, message = "Minimum price cannot be negative")
        BigDecimal minPrice,

        @DecimalMin(value = FilterRequestConstraints.MIN_PHONE_PRICE, message = "Maximum price cannot be negative")
        BigDecimal maxPrice,

        @Pattern(
                regexp = FilterRequestConstraints.SORT_ORDER_REGEXP,
                message = "Sort must be one of: name_asc, name_desc, price_asc, price_desc"
        )
        String sort
) {
    public PhoneFilterDto {
        sort = (sort == null || sort.isBlank()) ? "name_asc" : sort;
    }
}