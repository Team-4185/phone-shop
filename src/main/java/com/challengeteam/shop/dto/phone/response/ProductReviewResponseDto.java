package com.challengeteam.shop.dto.phone.response;

import java.time.Instant;

public record ProductReviewResponseDto(
        Long id,
        Long phoneId,
        Long userId,
        String authorName,
        Integer rating,
        String comment,
        Instant createdAt,
        Instant updatedAt
) {
}
