package com.challengeteam.shop.dto.phone;

import com.challengeteam.shop.dto.image.ImageMetadataResponseDto;

import java.math.BigDecimal;
import java.util.List;

public record PhoneResponseDto(
        Long id,
        String name,
        String description,
        BigDecimal price,
        String brand,
        Integer releaseYear,
        String cpu,
        Integer coresNumber,
        String screenSize,
        String frontCamera,
        String mainCamera,
        String batteryCapacity,
        List<ImageMetadataResponseDto> images
) {
}
