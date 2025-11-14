package com.challengeteam.shop.mapper;

import com.challengeteam.shop.dto.cart.CartResponseDto;
import com.challengeteam.shop.entity.cart.Cart;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {CartItemMapper.class})
public interface CartMapper {

    @Mapping(target = "cartItems", source = "cartItems")
    CartResponseDto toCartResponseDto(Cart cart);

}
