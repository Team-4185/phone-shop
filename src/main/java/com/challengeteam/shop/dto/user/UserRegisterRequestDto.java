package com.challengeteam.shop.dto.user;

public record UserRegisterRequestDto(
        String email,
        String password,
        String passwordConfirmation
) {
}
