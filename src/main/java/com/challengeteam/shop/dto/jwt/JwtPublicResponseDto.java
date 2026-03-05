package com.challengeteam.shop.dto.jwt;

public record JwtPublicResponseDto(
        Long userId,
        String email,
        String accessToken
) {
}
