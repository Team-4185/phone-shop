package com.challengeteam.shop.service;

import com.challengeteam.shop.dto.cart.CartItemAddRequestDto;
import com.challengeteam.shop.dto.cart.CartItemUpdateRequestDto;
import com.challengeteam.shop.entity.cart.Cart;
import com.challengeteam.shop.entity.cart.CartItem;

import java.util.Optional;

public interface CartService {

    Optional<Cart> getCart(Long cartId);

    Optional<Cart> addItemToCart(Long cartId, CartItemAddRequestDto cartItemAddRequestDto);

    Optional<Cart> updateAmountCartItem(Long cartId, CartItemUpdateRequestDto cartItemUpdateRequestDto);

    Optional<Cart> removeItemFromCart(Long cartId, Long phoneId);

    Optional<Cart> clearCart(Long cartId);

    Cart getCartByUserId(Long userId);

    Optional<CartItem> getCartItem(Long cartId, Long phoneId);

}
