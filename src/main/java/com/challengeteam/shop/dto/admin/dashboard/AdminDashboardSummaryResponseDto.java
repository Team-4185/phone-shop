package com.challengeteam.shop.dto.admin.dashboard;

import java.math.BigDecimal;

/** Summary cards for the admin Dashboard section. */
public record AdminDashboardSummaryResponseDto(
    BigDecimal totalRevenue, Long totalOrders, Long totalCustomers, Long lowStockProducts) {}
