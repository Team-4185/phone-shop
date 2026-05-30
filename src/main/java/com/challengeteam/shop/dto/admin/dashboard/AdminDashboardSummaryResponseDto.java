package com.challengeteam.shop.dto.admin.dashboard;

import java.math.BigDecimal;

/** Summary cards for the admin Dashboard section. */
public record AdminDashboardSummaryResponseDto(
    BigDecimal totalRevenue,
    BigDecimal totalRevenueChangePercent,
    Long totalOrders,
    BigDecimal totalOrdersChangePercent,
    Long processingOrders,
    Long itemsInStock,
    BigDecimal itemsInStockChangePercent,
    Long lowStockProducts,
    Long newClients,
    BigDecimal newClientsChangePercent) {}
