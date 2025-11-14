package com.challengeteam.shop.service;

import com.challengeteam.shop.dto.cart.CartItemAddRequestDto;
import com.challengeteam.shop.dto.cart.CartItemRemoveRequestDto;
import com.challengeteam.shop.entity.cart.Cart;

import java.util.Optional;

public interface UserCartService {

    Optional<Cart> getUserCart(Long userId);

    Cart putItemToUserCart(Long userId, CartItemAddRequestDto cartItemAddRequestDto);

    Cart removeItemFromUserCart(Long userId, CartItemRemoveRequestDto cartItemRemoveRequestDto);

    Cart clearUserCart(Long userId);

}
