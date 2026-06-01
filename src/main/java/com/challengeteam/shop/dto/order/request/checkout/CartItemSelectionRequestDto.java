package com.challengeteam.shop.dto.order.request.checkout;

import com.challengeteam.shop.entity.phone.PhoneColor;
import com.challengeteam.shop.entity.phone.StorageCapacity;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.io.Serializable;

public record CartItemSelectionRequestDto(
        @NotNull(message = "Phone ID must be not null")
        @Positive(message = "Phone ID must be greater than 0")
        Long phoneId,
        @NotNull(message = "Color must be present")
        PhoneColor color,
        @NotNull(message = "Storage must be present")
        StorageCapacity storage
) implements Serializable {
}
