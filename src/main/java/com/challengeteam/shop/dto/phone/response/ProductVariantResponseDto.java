package com.challengeteam.shop.dto.phone.response;

import com.challengeteam.shop.entity.phone.PhoneColor;
import com.challengeteam.shop.entity.phone.ProductStatus;
import com.challengeteam.shop.entity.phone.StorageCapacity;
import java.math.BigDecimal;

public record ProductVariantResponseDto(
    Long id,
    String sku,
    PhoneColor color,
    StorageCapacity storageCapacity,
    BigDecimal price,
    Integer stock,
    ProductStatus status) {}
