package com.challengeteam.shop.dto.cart;

import com.challengeteam.shop.dto.image.ImageMetadataResponseDto;
import com.challengeteam.shop.dto.phone.response.ProductVariantResponseDto;
import com.challengeteam.shop.entity.phone.ProductStatus;

import java.math.BigDecimal;

public record CartItemResponseDto(
        Long phoneId,
        Long variantId,
        ProductVariantResponseDto variant,
        String productName,
        String brand,
        BigDecimal price,
        ImageMetadataResponseDto previewImage,
        Integer stock,
        ProductStatus status,
        Integer quantity,
        Integer amount,
        BigDecimal lineTotal
) {
}
