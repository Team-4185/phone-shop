package com.challengeteam.shop.dto.user;

public record UserLoginRequestDto(
        String email,
        String password
) {
}
