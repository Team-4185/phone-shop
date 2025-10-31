package com.challengeteam.shop.service;

import com.challengeteam.shop.dto.cart.CartItemAddRequestDto;
import com.challengeteam.shop.dto.cart.CartItemUpdateRequestDto;
import com.challengeteam.shop.entity.cart.Cart;

import java.util.Optional;

public interface UserCartService {

    Optional<Cart> getUserCart(Long userId);

    void addItemToUserCart(Long userId, CartItemAddRequestDto cartItemAddRequestDto);

    void updateAmountUserCartItem(Long userId, CartItemUpdateRequestDto cartItemUpdateRequestDto);

    void removeItemFromUserCart(Long userId, Long phoneId);

    void clearUserCart(Long userId);

}
