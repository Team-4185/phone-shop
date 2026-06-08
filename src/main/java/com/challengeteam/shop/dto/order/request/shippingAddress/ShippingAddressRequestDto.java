package com.challengeteam.shop.dto.order.request.shippingAddress;

import com.challengeteam.shop.constraints.order.orderInputData.ShippingInputDataValidationRules;
import com.challengeteam.shop.constraints.order.validation.annotation.delivery.group.CourierGroupValidation;
import com.challengeteam.shop.constraints.order.validation.annotation.delivery.group.PostOfficeGroupValidation;
import com.challengeteam.shop.entity.order.shipping.LogisticsCompany;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.io.Serializable;

/**
 * DTO for {@link com.challengeteam.shop.entity.order.shipping.ShippingAddress}
 */
public record ShippingAddressRequestDto(
        @Pattern(regexp = "^[0-9]{1,5}[a-zA-Zа-яА-ЯёЁіІїЇєЄґҐ]?$",
                message = "Apartment number must be 1-5 digits, optionally followed by a letter",
                groups = CourierGroupValidation.class)
        String apartmentNumber,
        @Pattern(regexp = "^[0-9]{1,5}[a-zA-Zа-яА-ЯёЁіІїЇєЄґҐ/\\-]?([0-9]{1,3})?$",
                message = "Invalid house number format. Examples: '10', '10A', '10/2', '12-B'",
                groups = CourierGroupValidation.class)
        @NotNull(message = "House number is required", groups = CourierGroupValidation.class)
        String houseNumber,
        @NotNull(message = "Logistics company is required",
                groups = {CourierGroupValidation.class, PostOfficeGroupValidation.class})
        LogisticsCompany logisticsCompany,
        @NotNull(message = "Logistic post office is required",
                groups = {PostOfficeGroupValidation.class})
        String logisticPostOffice,
        @Pattern(regexp = "^[a-zA-Zа-яА-ЯёЁіІїЇєЄґҐ0-9.\\s\\-',/]{2,100}$",
                message = "Street name contains invalid characters or is too short",
                groups = CourierGroupValidation.class)
        @NotNull(message = "Street is required",
                groups = CourierGroupValidation.class)
        String street,
        @Pattern(regexp = ShippingInputDataValidationRules.REGION_PATTER_CONSTRAINT,
                message = "City name must contain only letters, spaces, or hyphens",
                groups = {CourierGroupValidation.class, PostOfficeGroupValidation.class})
        @NotNull(message = "City is required")
        String city,
        @Pattern(regexp = ShippingInputDataValidationRules.REGION_PATTER_CONSTRAINT,
                message = "Region name must contain only letters, spaces, or hyphens",
                groups = {CourierGroupValidation.class, PostOfficeGroupValidation.class})
        @NotNull(message = "Region is required",
                groups = {CourierGroupValidation.class, PostOfficeGroupValidation.class})
        String region,
        @Pattern(regexp = ShippingInputDataValidationRules.REGION_PATTER_CONSTRAINT,
                message = "Country name must contain only letters, spaces, or hyphens",
                groups = {CourierGroupValidation.class, PostOfficeGroupValidation.class})
        @NotNull(message = "Country is required",
                groups = {CourierGroupValidation.class, PostOfficeGroupValidation.class})
        String country,
        @Pattern(regexp = "^[0-9a-zA-Z\\s\\-]{3,10}$",
                message = "Zip code must be between 3 and 10 alphanumeric characters",
                groups = {CourierGroupValidation.class, PostOfficeGroupValidation.class})
        @NotNull(message = "Zip code is required",
                groups = {CourierGroupValidation.class, PostOfficeGroupValidation.class})
        String zipCode) implements Serializable {
}
