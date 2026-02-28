package com.challengeteam.shop.mapper;

import com.challengeteam.shop.dto.cart.CartItemResponseDto;
import com.challengeteam.shop.entity.cart.CartItem;
import com.challengeteam.shop.entity.phone.Phone;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring")
public interface CartItemMapper {

    @Mapping(target = "phoneId", source = "phone.id")
    CartItemResponseDto toCartItemResponseDto(CartItem cartItem);

    default Map<Phone, Integer> productsAsMap(List<CartItem> products) {
        Map<Phone, Integer> result = new HashMap<>();

        for (CartItem item : products) {
            Phone key = item.getPhone();
            int value = item.getAmount();
            result.put(key, value);
        }

        return result;
    }

}
