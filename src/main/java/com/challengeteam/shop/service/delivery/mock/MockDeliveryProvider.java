package com.challengeteam.shop.service.delivery.mock;

import com.challengeteam.shop.dto.delivery.DeliveryQuote;
import com.challengeteam.shop.dto.delivery.PickupPointResponseDto;
import com.challengeteam.shop.dto.order.request.shippingAddress.ShippingAddressRequestDto;
import com.challengeteam.shop.entity.order.DeliveryMethod;
import com.challengeteam.shop.service.delivery.DeliveryProvider;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class MockDeliveryProvider implements DeliveryProvider {
    public static final String PROVIDER_CODE = "mock";

    @Override
    public String providerCode() {
        return PROVIDER_CODE;
    }

    @Override
    public String displayName() {
        return "Mock Delivery";
    }

    @Override
    public List<PickupPointResponseDto> findPickupPoints(String city) {
        String normalizedCity = city == null ? "Kyiv" : city.strip();
        return List.of(
                new PickupPointResponseDto("mock-pickup-1", providerCode(), normalizedCity,
                        "Central pickup point", normalizedCity + ", Main Street 1"),
                new PickupPointResponseDto("mock-pickup-2", providerCode(), normalizedCity,
                        "North pickup point", normalizedCity + ", North Avenue 15"));
    }

    @Override
    public DeliveryQuote quote(DeliveryMethod deliveryMethod,
                               ShippingAddressRequestDto shippingAddress,
                               BigDecimal itemsTotal) {
        BigDecimal price = switch (deliveryMethod) {
            case PICKUP -> BigDecimal.ZERO;
            case POST_OFFICE, COURIER -> BigDecimal.ZERO;
        };
        int deliveryDays = switch (deliveryMethod) {
            case PICKUP -> 1;
            case POST_OFFICE -> 3;
            case COURIER -> 2;
        };
        return new DeliveryQuote(providerCode(), price, LocalDate.now().plusDays(deliveryDays));
    }
}
