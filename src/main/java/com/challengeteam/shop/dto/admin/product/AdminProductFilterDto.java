package com.challengeteam.shop.dto.admin.product;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record AdminProductFilterDto(
    @Size(max = 100, message = "Search length must be less than or equal to 100 characters")
        String search,
    String brand,
    @DecimalMin(value = "0.0", message = "Minimum price cannot be negative") BigDecimal minPrice,
    @DecimalMin(value = "0.0", message = "Maximum price cannot be negative") BigDecimal maxPrice,
    @Pattern(
            regexp =
                "name_asc|name_desc|price_asc|price_desc|releaseYear_asc|releaseYear_desc|brand_asc|brand_desc",
            message =
                "Sort must be one of: name_asc, name_desc, price_asc, price_desc, releaseYear_asc, releaseYear_desc, brand_asc, brand_desc")
        String sort) {
  public AdminProductFilterDto {
    sort = (sort == null || sort.isBlank()) ? "name_asc" : sort;
  }
}
