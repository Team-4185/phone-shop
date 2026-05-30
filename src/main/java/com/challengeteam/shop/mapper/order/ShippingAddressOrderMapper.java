package com.challengeteam.shop.mapper.order;

import com.challengeteam.shop.dto.order.request.shippingAddress.ShippingAddressRequestDto;
import com.challengeteam.shop.dto.order.response.shippingAddress.ShippingAddressResponseDto;
import com.challengeteam.shop.entity.order.shipping.ShippingAddress;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ShippingAddressOrderMapper {

    ShippingAddressResponseDto toShippingAddressDto(ShippingAddress shippingAddress);

    ShippingAddress toShippingAddress(ShippingAddressRequestDto shippingAddressRequestDto);
}