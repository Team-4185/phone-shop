package com.challengeteam.shop.service;

import com.challengeteam.shop.dto.cart.CartItemAddRequestDto;
import com.challengeteam.shop.dto.cart.CartItemUpdateRequestDto;
import com.challengeteam.shop.entity.cart.Cart;

import java.util.Optional;

public interface CartService {

    Optional<Cart> getCart(Long cartId);

    void addItemToCart(Long cartId, CartItemAddRequestDto cartItemAddRequestDto);

    void updateAmountCartItem(Long cartId, CartItemUpdateRequestDto cartItemUpdateRequestDto);

    void removeItemFromCart(Long cartId, Long phoneId);

    void clearCart(Long cartId);

    Long getCartIdByUserId(Long userId);

}
