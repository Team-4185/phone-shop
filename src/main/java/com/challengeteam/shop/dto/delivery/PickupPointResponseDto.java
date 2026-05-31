package com.challengeteam.shop.dto.delivery;

import java.io.Serializable;

public record PickupPointResponseDto(
        String id,
        String provider,
        String city,
        String name,
        String address) implements Serializable {
}
