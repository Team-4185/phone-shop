package com.challengeteam.shop.dto.user.response;

import com.challengeteam.shop.dto.cart.CartResponseDto;
import com.challengeteam.shop.dto.order.OrderResponseDto;
import com.challengeteam.shop.dto.phone.response.PhoneResponseDto;
import com.challengeteam.shop.dto.role.RoleResponseDto;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

/**
 * DTO for {@link com.challengeteam.shop.entity.user.User}
 */
public record UserPersonalInfoResponseDto(
        Long id,
        Instant createdAt,
        Instant updatedAt,
        String email,
        String firstName,
        String lastName,
        String city,
        String phoneNumber,
        RoleResponseDto role,
        CartResponseDto cart,
        List<OrderResponseDto> orders,
        List<PhoneResponseDto> favorites) implements Serializable {
}