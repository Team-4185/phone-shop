package com.challengeteam.shop.mapper.order;

import com.challengeteam.shop.dto.order.OrderResponseDto;
import com.challengeteam.shop.entity.order.Order;
import com.challengeteam.shop.mapper.phone.PhoneMapper;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {UserOrderMapper.class, OrderItemMapper.class,
                ShippingAddressOrderMapper.class, PhoneMapper.class,
                OrderPaymentDetailsMapper.class})
public interface OrderMapper {
    OrderResponseDto toDto(Order order);
}