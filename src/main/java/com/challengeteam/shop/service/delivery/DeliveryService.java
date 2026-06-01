package com.challengeteam.shop.service.delivery;

import com.challengeteam.shop.dto.delivery.DeliveryProviderResponseDto;
import com.challengeteam.shop.dto.delivery.PickupPointResponseDto;

import java.util.List;

public interface DeliveryService {
    List<DeliveryProviderResponseDto> getProviders();

    List<PickupPointResponseDto> getPickupPoints(String provider, String city);
}
