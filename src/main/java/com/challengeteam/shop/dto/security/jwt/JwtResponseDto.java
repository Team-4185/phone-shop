package com.challengeteam.shop.dto.security.jwt;

public record JwtResponseDto(
        Long userId,
        String email,
        String accessToken,
        String refreshToken,
        boolean rememberMe
) {
}