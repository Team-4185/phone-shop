package com.challengeteam.shop.dto.admin.dashboard;

import java.math.BigDecimal;

/** Best-selling product aggregation for the dashboard. */
public record AdminDashboardTopProductResponseDto(
    Long phoneId, String name, String sku, Long unitsSold, BigDecimal revenue) {}
