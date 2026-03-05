package com.challengeteam.shop.dto.cart;

import java.math.BigDecimal;
import java.util.List;

public record CartResponseDto(
        Long id,
        BigDecimal totalPrice,
        Integer totalAmount,
        List<CartItemResponseDto> cartItems
) {
}
