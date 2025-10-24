package com.challengeteam.shop.dto.cart;

public record CartItemUpdateRequestDto(
    Long phoneId,
    Integer newAmount
) {
}
