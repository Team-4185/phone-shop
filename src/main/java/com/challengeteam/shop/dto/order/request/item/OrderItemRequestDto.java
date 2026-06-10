package com.challengeteam.shop.dto.order.request.item;

import com.challengeteam.shop.entity.phone.PhoneColor;
import com.challengeteam.shop.entity.phone.StorageCapacity;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.io.Serializable;

/**
 * DTO for {@link com.challengeteam.shop.entity.order.OrderItem}
 */
public record OrderItemRequestDto(
        @Positive(message = "Phone ID must be greater than 0")
        Long phoneId,
        @Positive(message = "Variant ID must be greater than 0")
        Long variantId,
        @NotNull(message = "Quantity must be not null")
        @Positive(message = "Quantity must be greater than 0")
        Integer quantity,
        PhoneColor color,
        StorageCapacity storage
) implements Serializable {
        public OrderItemRequestDto(Long phoneId, Integer quantity, PhoneColor color, StorageCapacity storage) {
                this(phoneId, null, quantity, color, storage);
        }

        @AssertTrue(message = "Variant ID or phone ID with color and storage must be provided")
        public boolean isVariantOrLegacySelectionProvided() {
                return variantId != null || (phoneId != null && color != null && storage != null);
        }
}
