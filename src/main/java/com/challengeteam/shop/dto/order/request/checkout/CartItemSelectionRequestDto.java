package com.challengeteam.shop.dto.order.request.checkout;

import com.challengeteam.shop.entity.phone.PhoneColor;
import com.challengeteam.shop.entity.phone.StorageCapacity;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Positive;

import java.io.Serializable;

public record CartItemSelectionRequestDto(
        @Positive(message = "Phone ID must be greater than 0")
        Long phoneId,
        @Positive(message = "Variant ID must be greater than 0")
        Long variantId,
        PhoneColor color,
        StorageCapacity storage
) implements Serializable {
        public CartItemSelectionRequestDto(Long phoneId, PhoneColor color, StorageCapacity storage) {
                this(phoneId, null, color, storage);
        }

        @AssertTrue(message = "Variant ID or phone ID with color and storage must be provided")
        public boolean isVariantOrLegacySelectionProvided() {
                return variantId != null || (phoneId != null && color != null && storage != null);
        }
}
