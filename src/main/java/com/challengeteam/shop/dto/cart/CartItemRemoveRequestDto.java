package com.challengeteam.shop.dto.cart;

public record CartItemRemoveRequestDto(
        Long phoneId,
        Integer amount
) {
}
