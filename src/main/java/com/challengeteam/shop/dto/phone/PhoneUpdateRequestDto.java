package com.challengeteam.shop.dto.phone;

import java.math.BigDecimal;

public record PhoneUpdateRequestDto(
        String newName,
        String newDescription,
        BigDecimal newPrice,
        String newBrand,
        Integer newReleaseYear
) {
}
