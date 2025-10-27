package com.challengeteam.shop.dto.phone;

import java.math.BigDecimal;
import java.time.Instant;

public record PhoneResponseDto(
        Long id,
        String name,
        String description,
        BigDecimal price,
        String brand,
        Integer releaseYear
) {
}
