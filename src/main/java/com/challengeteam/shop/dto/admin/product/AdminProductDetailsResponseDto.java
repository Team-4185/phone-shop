package com.challengeteam.shop.dto.admin.product;

import com.challengeteam.shop.dto.image.ImageMetadataResponseDto;
import com.challengeteam.shop.entity.phone.ProductStatus;

import java.math.BigDecimal;
import java.util.List;

public record AdminProductDetailsResponseDto(
    Long id,
    String name,
    String sku,
    String description,
    BigDecimal price,
    String brand,
    Integer releaseYear,
    Integer stock,
    ProductStatus status,
    String cpu,
    Integer coresNumber,
    String screenSize,
    String frontCamera,
    String mainCamera,
    String batteryCapacity,
    List<ImageMetadataResponseDto> images) {}
