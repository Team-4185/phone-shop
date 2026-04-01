package com.challengeteam.shop.dto.admin.product;

import com.challengeteam.shop.dto.image.ImageMetadataResponseDto;

import java.math.BigDecimal;

public record AdminProductListItemResponseDto(
    Long id,
    String name,
    String brand,
    BigDecimal price,
    Integer releaseYear,
    ImageMetadataResponseDto previewImage) {}
