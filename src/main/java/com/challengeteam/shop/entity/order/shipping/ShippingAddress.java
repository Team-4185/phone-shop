package com.challengeteam.shop.entity.order.shipping;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@Embeddable
@NoArgsConstructor
@Builder
public class ShippingAddress {
    private String apartmentNumber;
    @Column(nullable = false)
    private String houseNumber;
    @Enumerated(EnumType.STRING)
    private LogisticsCompany logisticsCompany;
    private String logisticPostOffice;
    private String trackingNumber;
    @Column(nullable = false)
    private String street;
    @Column(nullable = false)
    private String city;
    @Column(nullable = false)
    private String region;
    @Column(nullable = false)
    private String country;
    @Column(nullable = false)
    private String zipCode;
}