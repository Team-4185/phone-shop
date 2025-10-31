package com.challengeteam.shop.dto.cart;

public record CartItemResponseDto(
        Long id,
        Long cartId,
        Long phoneId,
        Integer amount
) {
}
