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
        Long cartId = cartService.getCartIdByUserId(userId);
        return cartService.getCart(cartId);
    }

    @Transactional
    @Override
    public void addItemToUserCart(Long userId, CartItemAddRequestDto cartItemAddRequestDto) {
        Long cartId = cartService.getCartIdByUserId(userId);
        cartService.addItemToCart(cartId, cartItemAddRequestDto);
    }

    @Transactional
    @Override
    public void updateAmountUserCartItem(Long userId, CartItemUpdateRequestDto cartItemUpdateRequestDto) {
        Long cartId = cartService.getCartIdByUserId(userId);
        cartService.updateAmountCartItem(cartId, cartItemUpdateRequestDto);
    }

    @Transactional
    @Override
    public void removeItemFromUserCart(Long userId, Long phoneId) {
        Long cartId = cartService.getCartIdByUserId(userId);
        cartService.removeItemFromCart(cartId, phoneId);
    }

    @Transactional
    @Override
    public void clearUserCart(Long userId) {
        Long cartId = cartService.getCartIdByUserId(userId);
        cartService.clearCart(cartId);
    }

}
