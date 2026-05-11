package com.challengeteam.shop.dto.admin.order;

import java.math.BigDecimal;

/** Item snapshot returned in admin order details. */
public record AdminOrderItemResponseDto(
    Long id,
    Long phoneId,
    String productName,
    String sku,
    BigDecimal unitPrice,
    Integer quantity,
    BigDecimal totalPrice) {}
