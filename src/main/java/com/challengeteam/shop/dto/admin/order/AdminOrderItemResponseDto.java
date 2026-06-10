package com.challengeteam.shop.dto.admin.order;

import com.challengeteam.shop.entity.phone.PhoneColor;
import com.challengeteam.shop.entity.phone.StorageCapacity;
import java.math.BigDecimal;

/** Item snapshot returned in admin order details. */
public record AdminOrderItemResponseDto(
    Long id,
    Long phoneId,
    Long variantId,
    String productName,
    String sku,
    PhoneColor selectedColor,
    StorageCapacity selectedStorage,
    BigDecimal unitPrice,
    Integer quantity,
    BigDecimal totalPrice) {}
