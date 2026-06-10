package com.challengeteam.shop.service.impl;

import com.challengeteam.shop.dto.cart.CartItemAddRequestDto;
import com.challengeteam.shop.dto.cart.CartItemRemoveRequestDto;
import com.challengeteam.shop.entity.cart.Cart;
import com.challengeteam.shop.entity.cart.CartItem;
import com.challengeteam.shop.exceptionHandling.exception.ResourceNotFoundException;
import com.challengeteam.shop.service.CartService;
import com.challengeteam.shop.service.UserCartService;
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

        Long variantId = resolveVariantId(cart, cartItemAddRequestDto.variantId(), cartItemAddRequestDto.phoneId());
        Integer amountToAdd = cartItemAddRequestDto.amount();

        return CartUtility.isCartHasVariant(cart, variantId)
                ? increaseExistingVariant(cart, variantId, amountToAdd)
                : cartService.putItemToCart(cart, cartItemAddRequestDto);
    }

    @Transactional
    @Override
    public Cart removeItemFromUserCart(Long userId, CartItemRemoveRequestDto cartItemRemoveRequestDto) {
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(cartItemRemoveRequestDto, "cartItemRemoveRequestDto");

        Cart cart = cartService.getCartByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart for user with id " + userId + " not found"));

        Long variantId = resolveVariantId(cart, cartItemRemoveRequestDto.variantId(), cartItemRemoveRequestDto.phoneId());
        Integer amountToRemove = cartItemRemoveRequestDto.amount();

        ensureVariantExistsInCart(cart, variantId);

        Integer currentAmount = CartUtility.getCartItemAmountByVariant(cart, variantId);

        return currentAmount > amountToRemove
                ? decreaseExistingVariant(cart, variantId, currentAmount, amountToRemove)
                : cartService.removeItemFromCart(cart, variantId);
    }

    @Transactional
    @Override
    public Cart clearUserCart(Long userId) {
        Objects.requireNonNull(userId, "userId");

        Cart cart = cartService.getCartByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart for user with id " + userId + " not found"));
        return cartService.clearCart(cart);
    }

    private Cart increaseExistingVariant(Cart cart, Long variantId, Integer amountToAdd) {
        Integer currentAmount = CartUtility.getCartItemAmountByVariant(cart, variantId);
        Integer newAmount = currentAmount + amountToAdd;
        log.debug("Increasing amount of variant {} from {} to {}", variantId, currentAmount, newAmount);
        return cartService.updateAmountCartItem(cart, variantId, newAmount);
    }

    private Cart decreaseExistingVariant(
            Cart cart, Long variantId, Integer currentAmount, Integer amountToRemove) {
        Integer newAmount = currentAmount - amountToRemove;
        log.debug("Decreasing amount of variant {} from {} to {}", variantId, currentAmount, newAmount);
        return cartService.updateAmountCartItem(cart, variantId, newAmount);
    }

    private Long resolveVariantId(Cart cart, Long variantId, Long legacyPhoneId) {
        return Optional.ofNullable(variantId)
                .orElseGet(() -> cart.getCartItems().stream()
                        .filter(item -> item.getPhone().getId().equals(legacyPhoneId))
                        .map(CartItem::getVariant)
                        .filter(Objects::nonNull)
                        .map(variant -> variant.getId())
                        .findFirst()
                        .orElse(legacyPhoneId));
    }

    private void ensureVariantExistsInCart(Cart cart, Long variantId) {
        if (!CartUtility.isCartHasVariant(cart, variantId)) {
            throw new ResourceNotFoundException("Variant with id " + variantId + " not found in user's cart");
        }
    }

}
