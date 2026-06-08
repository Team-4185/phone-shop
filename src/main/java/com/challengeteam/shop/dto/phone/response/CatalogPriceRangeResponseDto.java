package com.challengeteam.shop.dto.phone.response;

import java.math.BigDecimal;

public record CatalogPriceRangeResponseDto(
        BigDecimal minPrice,
        BigDecimal maxPrice
) {
}
