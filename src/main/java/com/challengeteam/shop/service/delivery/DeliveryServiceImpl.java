package com.challengeteam.shop.service.delivery;

import com.challengeteam.shop.dto.delivery.DeliveryProviderResponseDto;
import com.challengeteam.shop.dto.delivery.PickupPointResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeliveryServiceImpl implements DeliveryService {
    private final DeliveryProviderResolver deliveryProviderResolver;

    @Override
    public List<DeliveryProviderResponseDto> getProviders() {
        return deliveryProviderResolver.getProviders().stream()
                .map(provider -> new DeliveryProviderResponseDto(
                        provider.providerCode(), provider.displayName()))
                .toList();
    }

    @Override
    public List<PickupPointResponseDto> getPickupPoints(String provider, String city) {
        return deliveryProviderResolver.getProvider(provider).findPickupPoints(city);
    }
}
