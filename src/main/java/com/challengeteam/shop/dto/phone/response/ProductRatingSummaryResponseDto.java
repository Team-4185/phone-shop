package com.challengeteam.shop.dto.phone.response;

import java.math.BigDecimal;

public record ProductRatingSummaryResponseDto(
        Long phoneId,
        BigDecimal averageRating,
        Long reviewsCount
) {
    public static ProductRatingSummaryResponseDto empty(Long phoneId) {
        return new ProductRatingSummaryResponseDto(phoneId, BigDecimal.ZERO, 0L);
    }
}
