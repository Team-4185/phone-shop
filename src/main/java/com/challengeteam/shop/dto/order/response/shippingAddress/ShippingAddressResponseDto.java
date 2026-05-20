package com.challengeteam.shop.dto.order.response.shippingAddress;

import com.challengeteam.shop.entity.order.shipping.LogisticsCompany;

import java.io.Serializable;

/**
 * DTO for {@link com.challengeteam.shop.entity.order.shipping.ShippingAddress}
 */
public record ShippingAddressResponseDto(String apartmentNumber,
                                         String houseNumber,
                                         LogisticsCompany logisticsCompany,
                                         String logisticPostOffice,
                                         String trackingNumber,
                                         String street,
                                         String city,
                                         String region,
                                         String country,
                                         String zipCode) implements Serializable {
}