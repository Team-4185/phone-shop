package com.challengeteam.shop.mapper.order;

import com.challengeteam.shop.dto.order.response.orderItem.OrderItemResponseDto;
import com.challengeteam.shop.entity.order.OrderItem;
import com.challengeteam.shop.mapper.phone.PhoneMapper;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {PhoneMapper.class})
public interface OrderItemMapper {
    OrderItemResponseDto toOrderItemResponseDto(OrderItem orderItem);
}