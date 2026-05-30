package com.challengeteam.shop.dto.admin.dashboard;

import com.challengeteam.shop.entity.phone.ProductStatus;

/** Product stock warning returned by the dashboard low-stock widget. */
public record AdminDashboardLowStockAlertResponseDto(
    Long id,
    String name,
    String sku,
    String brand,
    Integer stock,
    Integer threshold,
    ProductStatus status) {}
