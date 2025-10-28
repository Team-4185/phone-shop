package com.challengeteam.shop.service;

import com.challengeteam.shop.dto.cart.CartItemAddRequestDto;
import com.challengeteam.shop.dto.cart.CartItemUpdateRequestDto;
import com.challengeteam.shop.entity.cart.Cart;

import java.util.Optional;

public interface UserCartService {

    Optional<Cart> getUserCart(Long id);

    void addItemToCart(Long id, CartItemAddRequestDto cartItemAddRequestDto);

    void updateCartItem(Long id, CartItemUpdateRequestDto cartItemUpdateRequestDto);

    void removeItemFromCart(Long id, Long phoneId);

    void clearCart(Long id);

}
