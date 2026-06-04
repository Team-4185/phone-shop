package com.challengeteam.shop.service.model;

import com.challengeteam.shop.dto.phone.response.ProductBadgeResponseDto;

import java.util.Set;

public record ProductBadgeInfo(
        Set<ProductBadgeResponseDto> badges,
        Integer discountPercent
) {
    public static ProductBadgeInfo empty() {
        return new ProductBadgeInfo(Set.of(), 0);
    }
}
