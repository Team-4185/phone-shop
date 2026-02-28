package com.challengeteam.shop.dto.order;

public record CreateOrderResponseDto(
    OrderResponseDto orderResponseDto,
    String instructions
) {
}
