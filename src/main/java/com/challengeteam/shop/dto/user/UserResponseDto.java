package com.challengeteam.shop.dto.user;

import com.challengeteam.shop.dto.role.RoleResponseDto;

public record UserResponseDto (
        Long id,
        String email,
        String firstName,
        String lastName,
        String city,
        String phoneNumber,
        RoleResponseDto role
) {
}
