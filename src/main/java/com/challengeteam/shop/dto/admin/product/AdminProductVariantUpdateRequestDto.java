package com.challengeteam.shop.dto.admin.product;

import com.challengeteam.shop.entity.phone.PhoneColor;
import com.challengeteam.shop.entity.phone.ProductStatus;
import com.challengeteam.shop.entity.phone.StorageCapacity;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record AdminProductVariantUpdateRequestDto(
    @Size(min = 3, max = 64, message = "Variant sku must be between {min} and {max} characters")
        @Pattern(
            regexp = "^[A-Z0-9]+(?:-[A-Z0-9]+)*$",
            message = "Variant sku must contain only uppercase letters, numbers and hyphens")
        String sku,
    PhoneColor color,
    StorageCapacity storageCapacity,
    @DecimalMin(value = "0.00", message = "Variant price must be greater than {value}")
        BigDecimal price,
    @Min(value = 0, message = "Variant stock must be greater than or equal to {value}")
        Integer stock,
    ProductStatus status) {}
