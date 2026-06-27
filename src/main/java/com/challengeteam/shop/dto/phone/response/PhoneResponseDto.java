package com.challengeteam.shop.dto.phone.response;

import com.challengeteam.shop.dto.image.ImageMetadataResponseDto;
import com.challengeteam.shop.entity.phone.ProductStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public record PhoneResponseDto(
        Long id,
        String name,
        String description,
        BigDecimal price,
        String brand,
        Integer stock,
        ProductStatus status,
        ImageMetadataResponseDto previewImage,
        Integer releaseYear,
        String cpu,
        Integer coresNumber,
        String screenSize,
        String frontCamera,
        String mainCamera,
        String batteryCapacity,
        Set<PhoneColorResponseDto> colors,
        Set<StorageCapacityResponseDto> storageCapacity,
        List<ProductVariantResponseDto> variants,
        List<ImageMetadataResponseDto> images,
        Set<ProductBadgeResponseDto> badges,
        Integer discountPercent,
        BigDecimal averageRating,
        Long reviewsCount
) {
    public PhoneResponseDto(
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
            List<ImageMetadataResponseDto> images) {
        this(id, name, description, price, brand, null, null, null, releaseYear, cpu, coresNumber, screenSize,
                frontCamera, mainCamera, batteryCapacity, colors, storageCapacity, List.of(), images, Set.of(), 0,
                BigDecimal.ZERO, 0L);
    }

    public PhoneResponseDto(
            Long id,
            String name,
            String description,
            BigDecimal price,
            String brand,
            Integer stock,
            ProductStatus status,
            ImageMetadataResponseDto previewImage,
            Integer releaseYear,
            String cpu,
            Integer coresNumber,
            String screenSize,
            String frontCamera,
            String mainCamera,
            String batteryCapacity,
            Set<PhoneColorResponseDto> colors,
            Set<StorageCapacityResponseDto> storageCapacity,
            List<ImageMetadataResponseDto> images,
            Set<ProductBadgeResponseDto> badges,
            Integer discountPercent,
            BigDecimal averageRating,
            Long reviewsCount) {
        this(id, name, description, price, brand, stock, status, previewImage, releaseYear, cpu, coresNumber,
                screenSize, frontCamera, mainCamera, batteryCapacity, colors, storageCapacity, List.of(), images,
                badges, discountPercent, averageRating, reviewsCount);
    }
}
