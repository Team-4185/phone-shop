package com.challengeteam.shop.dto.cart;

public record CartItemResponseDto(
        Long id,
        Long phoneId,
        Integer amount
) {
}
