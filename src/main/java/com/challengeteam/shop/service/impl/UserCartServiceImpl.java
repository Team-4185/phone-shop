package com.challengeteam.shop.service.impl;

import com.challengeteam.shop.dto.cart.CartItemAddRequestDto;
import com.challengeteam.shop.dto.cart.CartItemUpdateRequestDto;
import com.challengeteam.shop.entity.cart.Cart;
import com.challengeteam.shop.service.CartService;
import com.challengeteam.shop.service.UserCartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserCartServiceImpl implements UserCartService {

    private final CartService cartService;

    @Transactional(readOnly = true)
    @Override
    public Optional<Cart> getUserCart(Long userId) {
        return Optional.of(cartService.getCartByUserId(userId));
    }

    @Transactional
    @Override
    public Optional<Cart> addItemToUserCart(Long userId, CartItemAddRequestDto cartItemAddRequestDto) {
        Cart cart = cartService.getCartByUserId(userId);
        return cartService.addItemToCart(cart.getId(), cartItemAddRequestDto);
    }

    @Transactional
    @Override
    public Optional<Cart> updateAmountUserCartItem(Long userId, CartItemUpdateRequestDto cartItemUpdateRequestDto) {
        Cart cart = cartService.getCartByUserId(userId);
        return cartService.updateAmountCartItem(cart.getId(), cartItemUpdateRequestDto);
    }

    @Transactional
    @Override
    public Optional<Cart> removeItemFromUserCart(Long userId, Long phoneId) {
        Cart cart = cartService.getCartByUserId(userId);
        return cartService.removeItemFromCart(cart.getId(), phoneId);
    }

    @Transactional
    @Override
    public Optional<Cart> clearUserCart(Long userId) {
        Cart cart = cartService.getCartByUserId(userId);
        return cartService.clearCart(cart.getId());
    }

}
