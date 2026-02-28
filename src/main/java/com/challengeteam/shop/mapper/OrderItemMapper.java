package com.challengeteam.shop.mapper;

import com.challengeteam.shop.dto.order.OrderItemResponseDto;
import com.challengeteam.shop.entity.order.OrderItem;
import com.challengeteam.shop.entity.phone.Phone;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {

    @Mapping(source = "id.phoneId", target = "phoneId")
    OrderItemResponseDto toResponse(OrderItem item);

}
