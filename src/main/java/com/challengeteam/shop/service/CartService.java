package com.challengeteam.shop.service;

import com.challengeteam.shop.dto.cart.CartItemAddRequestDto;
import com.challengeteam.shop.entity.cart.Cart;

import java.util.Optional;

public interface CartService {

    Optional<Cart> getCart(Long cartId);

    Cart putItemToCart(Cart cart, CartItemAddRequestDto cartItemAddRequestDto);

    Cart updateAmountCartItem(Cart cart, Long variantId, Integer amount);

    Cart removeItemFromCart(Cart cart, Long variantId);

    Cart clearCart(Cart cart);

    Optional<Cart> getCartByUserId(Long userId);

}
