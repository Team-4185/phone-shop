package com.challengeteam.shop.dto.security.jwt;

public record JwtPublicResponseDto(
        Long userId,
        String email,
        String accessToken
) {
}