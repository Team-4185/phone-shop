package com.challengeteam.shop.dto.order.request.item;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.io.Serializable;

/**
 * DTO for {@link com.challengeteam.shop.entity.order.OrderItem}
 */
public record OrderItemRequestDto(
        @NotNull(message = "Phone ID must be not null")
        @Positive(message = "Phone ID must be greater than 0")
        Long phoneId,
        @NotNull(message = "Quantity must be not null")
        @Positive(message = "Quantity must be greater than 0")
        Integer quantity) implements Serializable {
}