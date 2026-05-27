package com.challengeteam.shop.dto.order.request.item;

import com.challengeteam.shop.entity.phone.PhoneColor;
import com.challengeteam.shop.entity.phone.StorageCapacity;
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
        Integer quantity,
        @NotNull(message = "Color must be present")
        PhoneColor color,
        @NotNull(message = "Storage must be present")
        StorageCapacity storage
) implements Serializable {
}