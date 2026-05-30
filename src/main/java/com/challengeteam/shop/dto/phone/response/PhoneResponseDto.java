package com.challengeteam.shop.dto.phone.response;

import com.challengeteam.shop.dto.image.ImageMetadataResponseDto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

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
        Set<PhoneColorResponseDto> colors,
        Set<StorageCapacityResponseDto> storageCapacity,
        List<ImageMetadataResponseDto> images
) {
}