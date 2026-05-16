package com.challengeteam.shop.dto.admin.dashboard;

import java.math.BigDecimal;

/** Brand revenue and quantity aggregation for the dashboard. */
public record AdminDashboardSalesByBrandResponseDto(
    String brand, BigDecimal revenue, Long unitsSold) {}
