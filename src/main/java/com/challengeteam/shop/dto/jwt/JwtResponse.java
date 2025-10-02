package com.challengeteam.shop.dto.jwt;

public record JwtResponse(
        Long userId,
        String email,
        String accessToken,
        String refreshToken
) {
}
