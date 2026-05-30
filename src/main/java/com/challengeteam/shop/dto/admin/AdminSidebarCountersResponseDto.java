package com.challengeteam.shop.dto.admin;

/** Counters used by admin sidebar navigation badges. */
public record AdminSidebarCountersResponseDto(
    long productsCount,
    long ordersCount,
    long customersCount) {}
