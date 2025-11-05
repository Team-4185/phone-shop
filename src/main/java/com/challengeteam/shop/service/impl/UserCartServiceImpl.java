package com.challengeteam.shop.service.impl;

import com.challengeteam.shop.dto.cart.CartItemAddRequestDto;
import com.challengeteam.shop.dto.cart.CartItemUpdateRequestDto;
import com.challengeteam.shop.entity.cart.Cart;
import com.challengeteam.shop.entity.cart.CartItem;
import com.challengeteam.shop.exceptionHandling.exception.ResourceNotFoundException;
import com.challengeteam.shop.service.CartService;
import com.challengeteam.shop.service.UserCartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
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

        Long phoneId = cartItemAddRequestDto.phoneId();
        Integer amountToAdd = cartItemAddRequestDto.amount();

        Optional<CartItem> existingItem = cartService.getCartItem(cart.getId(), phoneId);

        if (existingItem.isPresent()) {
            CartItem cartItem = existingItem.get();
            Integer newAmount = cartItem.getAmount() + amountToAdd;

            log.debug("Phone {} already in cart. Increasing amount from {} to {}", phoneId, cartItem.getAmount(), newAmount);

            CartItemUpdateRequestDto updateDto = new CartItemUpdateRequestDto(phoneId, newAmount);
            return cartService.updateAmountCartItem(cart.getId(), updateDto);
        } else {
            log.debug("Adding new phone {} to cart with amount {}", phoneId, amountToAdd);
            return cartService.addItemToCart(cart.getId(), cartItemAddRequestDto);
        }
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

        CartItem cartItem = cartService.getCartItem(cart.getId(), phoneId)
                .orElseThrow(() -> new ResourceNotFoundException("Phone with id " + phoneId + " not found in user cart"));

        Integer currentAmount = cartItem.getAmount();

        if (currentAmount > 1) {
            Integer newAmount = currentAmount - 1;
            log.debug("Decreasing amount of phone {} from {} to {}", phoneId, currentAmount, newAmount);

            CartItemUpdateRequestDto updateDto = new CartItemUpdateRequestDto(phoneId, newAmount);
            return cartService.updateAmountCartItem(cart.getId(), updateDto);
        } else {
            log.debug("Removing phone {} from cart completely", phoneId);
            return cartService.removeItemFromCart(cart.getId(), phoneId);
        }
    }

    @Transactional
    @Override
    public Optional<Cart> clearUserCart(Long userId) {
        Cart cart = cartService.getCartByUserId(userId);
        return cartService.clearCart(cart.getId());
    }

}
