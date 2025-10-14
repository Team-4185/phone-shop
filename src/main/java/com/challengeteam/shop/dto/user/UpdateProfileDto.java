package com.challengeteam.shop.dto.user;

public record UpdateProfileDto(
        String newFirstname,
        String newLastname,
        String newCity,
        String newPhoneNumber
) {
}
