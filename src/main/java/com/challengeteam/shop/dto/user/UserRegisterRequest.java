package com.challengeteam.shop.dto.user;

public record UserRegisterRequest (
        String email,
        String password,
        String passwordConfirmation
) {
}
