package com.challengeteam.shop.dto.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OrderDestination(
        @NotBlank(message = "Destination address is required")
        @Size(min = 5, max = 500, message = "Address length must be between {min} and {max} symbols")
        String address
) {
}
