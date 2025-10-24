package com.challengeteam.shop.service;

import com.challengeteam.shop.dto.cart.CartItemAddRequestDto;
import com.challengeteam.shop.dto.cart.CartItemUpdateRequestDto;
import com.challengeteam.shop.entity.cart.Cart;

import java.util.Optional;

public interface CartService {

    Optional<Cart> getByUsername(String username);

    void addItemToCart(String username, CartItemAddRequestDto cartItemAddRequestDto);

    void updateItem(String username, CartItemUpdateRequestDto cartItemUpdateRequestDto);

    void removeItem(String username, Long phoneId);

    void clearCart(String username);

}
