package com.challengeteam.shop.dto.admin.order;

import jakarta.validation.constraints.Size;

import java.io.Serializable;

public record AdminShipOrderRequestDto(
        @Size(max = 100, message = "Tracking number must not exceed {max} characters")
        String trackingNumber) implements Serializable {
}
