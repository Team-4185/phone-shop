package com.challengeteam.shop.dto.phone;

import java.math.BigDecimal;

public record PhoneResponseDto(
        Long id,
        String name,
        String description,
        BigDecimal price,
        String brand,
        Integer releaseYear
) {
}
