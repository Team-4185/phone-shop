package com.challengeteam.shop.dto.admin.order;

public record AdminOrderKpiResponseDto(
    long confirmed, long processing, long delivered, long cancelled) {}
