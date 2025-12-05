package com.challengeteam.shop.dto.jwt;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record JwtRefreshRequestDto(
        @NotBlank(message = "Refresh token must be present")
        @Pattern(
                regexp = "^[A-Za-z0-9_-]{1,500}\\.[A-Za-z0-9_-]{1,500}\\.[A-Za-z0-9_-]{10,1000}$",
                message = "Refresh token is invalid"
        )
        String refreshToken
) {
}
