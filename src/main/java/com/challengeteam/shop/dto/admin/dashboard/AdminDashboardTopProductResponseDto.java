package com.challengeteam.shop.dto.admin.dashboard;

import java.math.BigDecimal;
import com.challengeteam.shop.entity.phone.ProductStatus;

/** Best-selling product aggregation for the dashboard. */
public record AdminDashboardTopProductResponseDto(
    Long phoneId,
    String name,
    String sku,
    Long unitsSold,
    BigDecimal revenue,
    Integer stock,
    ProductStatus status,
    BigDecimal growthPercent) {}
