package com.challengeteam.shop.dto.jwt;

public record JwtResponseDto(
        Long userId,
        String email,
        String accessToken,
        String refreshToken
) {
}
