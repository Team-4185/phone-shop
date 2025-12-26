package com.challengeteam.shop.service.impl;

import com.challengeteam.shop.dto.cart.CartItemAddRequestDto;
import com.challengeteam.shop.dto.cart.CartItemRemoveRequestDto;
import com.challengeteam.shop.entity.cart.Cart;
import com.challengeteam.shop.exceptionHandling.exception.InvalidCartItemAmountException;
import com.challengeteam.shop.exceptionHandling.exception.ResourceNotFoundException;
import com.challengeteam.shop.service.CartService;
import com.challengeteam.shop.service.UserCartService;
import com.challengeteam.shop.service.impl.validator.CartValidator;
import com.challengeteam.shop.utility.CartUtility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserCartServiceImpl implements UserCartService {

    private final CartService cartService;

    private final CartValidator cartValidator;

    @Transactional(readOnly = true)
    @Override
    public Optional<Cart> getUserCart(Long userId) {
        Objects.requireNonNull(userId, "userId");

        return cartService.getCartByUserId(userId);
    }

    @Transactional
    @Override
    public Cart putItemToUserCart(Long userId, CartItemAddRequestDto cartItemAddRequestDto) {
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(cartItemAddRequestDto, "cartItemAddRequestDto");

        Cart cart = cartService.getCartByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart for user with id " + userId + " not found"));

        Long phoneId = cartItemAddRequestDto.phoneId();
        Integer amountToAdd = cartItemAddRequestDto.amount();

        cartValidator.validateCartItemAmountToUpdate(amountToAdd);

        boolean isCartHasPhone = CartUtility.isCartHasPhone(cart, phoneId);

        if (isCartHasPhone) {
            log.debug("Phone {} already in cart.", phoneId);
            Integer currentAmount = CartUtility.getCartItemAmount(cart, phoneId);
            Integer newAmount = currentAmount + amountToAdd;
            cartValidator.validateCartItemTotalAmount(newAmount);
            return cartService.updateAmountCartItem(cart, phoneId, newAmount);
        } else {
            log.debug("Adding new phone {} to cart with amount {}", phoneId, amountToAdd);
            return cartService.putItemToCart(cart, cartItemAddRequestDto);
        }
    }

    @Transactional
    @Override
    public Cart removeItemFromUserCart(Long userId, CartItemRemoveRequestDto cartItemRemoveRequestDto) {
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(cartItemRemoveRequestDto, "cartItemRemoveRequestDto");

        Cart cart = cartService.getCartByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart for user with id " + userId + " not found"));

        Long phoneId = cartItemRemoveRequestDto.phoneId();
        Integer amountToRemove = cartItemRemoveRequestDto.amount();

        if (!CartUtility.isCartHasPhone(cart, phoneId)) {
            throw new ResourceNotFoundException("Phone with id " + phoneId + " not found in user's cart");
        }

        Integer currentAmount = CartUtility.getCartItemAmount(cart, phoneId);

        if (currentAmount - amountToRemove < 0) {
            throw new InvalidCartItemAmountException("Invalid amount to remove from cart");
        }

        if (currentAmount > amountToRemove) {
            Integer newAmount = currentAmount - amountToRemove;
            cartValidator.validateCartItemTotalAmount(newAmount);
            log.debug("Decreasing amount of phone {} from {} to {}", phoneId, currentAmount, newAmount);

            return cartService.updateAmountCartItem(cart, phoneId, newAmount);
        } else {
            log.debug("Removing phone {} from cart completely", phoneId);
            return cartService.removeItemFromCart(cart, phoneId);
        }
    }

    @Transactional
    @Override
    public Cart clearUserCart(Long userId) {
        Objects.requireNonNull(userId, "userId");

        Cart cart = cartService.getCartByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart for user with id " + userId + " not found"));
        return cartService.clearCart(cart);
    }

}
