package com.challengeteam.shop.dto.admin.product;

import com.challengeteam.shop.dto.image.ImageMetadataResponseDto;
import com.challengeteam.shop.entity.phone.ProductStatus;

import java.math.BigDecimal;

public record AdminProductListItemResponseDto(
    Long id,
    String name,
    String sku,
    String brand,
    BigDecimal price,
    Integer stock,
    ProductStatus status,
    ImageMetadataResponseDto previewImage) {}
