package com.challengeteam.shop.service;

import com.challengeteam.shop.dto.cart.CartItemAddRequestDto;
import com.challengeteam.shop.dto.cart.CartItemUpdateRequestDto;
import com.challengeteam.shop.entity.cart.Cart;

import java.util.Optional;

public interface UserCartService {

    Optional<Cart> getUserCart(Long userId);

    Optional<Cart> addItemToUserCart(Long userId, CartItemAddRequestDto cartItemAddRequestDto);

    Optional<Cart> updateAmountUserCartItem(Long userId, CartItemUpdateRequestDto cartItemUpdateRequestDto);

    Optional<Cart> removeItemFromUserCart(Long userId, Long phoneId);

    Optional<Cart> clearUserCart(Long userId);

}
