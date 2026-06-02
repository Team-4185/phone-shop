package com.challengeteam.shop.service.delivery;

import com.challengeteam.shop.dto.delivery.DeliveryQuote;
import com.challengeteam.shop.dto.delivery.PickupPointResponseDto;
import com.challengeteam.shop.dto.order.request.shippingAddress.ShippingAddressRequestDto;
import com.challengeteam.shop.entity.order.DeliveryMethod;

import java.math.BigDecimal;
import java.util.List;

public interface DeliveryProvider {
    String providerCode();

    String displayName();

    List<PickupPointResponseDto> findPickupPoints(String city);

    DeliveryQuote quote(DeliveryMethod deliveryMethod,
                        ShippingAddressRequestDto shippingAddress,
                        BigDecimal itemsTotal);
}
