package com.challengeteam.shop.mapper;

import com.challengeteam.shop.dto.cart.CartResponseDto;
import com.challengeteam.shop.entity.cart.Cart;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {CartItemMapper.class}, imports = {com.challengeteam.shop.utility.CartUtility.class})
public interface CartMapper {

    @Mapping(target = "cartItems", source = "cartItems")
    @Mapping(target = "totalAmount", expression = "java(CartUtility.countTotalAmount(cart))")
    CartResponseDto toCartResponseDto(Cart cart);

}
