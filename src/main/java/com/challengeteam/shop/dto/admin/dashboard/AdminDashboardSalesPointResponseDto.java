package com.challengeteam.shop.dto.admin.dashboard;

import java.math.BigDecimal;
import java.time.LocalDate;

/** One point in the dashboard sales analytics chart. */
public record AdminDashboardSalesPointResponseDto(
    LocalDate date, BigDecimal revenue, Long ordersCount) {}
