package com.challengeteam.shop.mapper;

import com.challengeteam.shop.dto.cart.CartItemResponseDto;
import com.challengeteam.shop.entity.cart.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CartItemMapper {

    @Mapping(target = "phoneId", source = "phone.id")
    CartItemResponseDto toCartItemResponseDto(CartItem cartItem);

}
