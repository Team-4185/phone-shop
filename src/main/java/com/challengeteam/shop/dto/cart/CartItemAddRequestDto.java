package com.challengeteam.shop.dto.cart;

public record CartItemAddRequestDto(
        Long phoneId,
        Integer amount
) {
}
