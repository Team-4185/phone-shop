package com.challengeteam.shop.service.impl.validator.impl;

import com.challengeteam.shop.entity.cart.Cart;
import com.challengeteam.shop.entity.cart.CartItem;
import com.challengeteam.shop.exceptionHandling.exception.InvalidCartItemAmountException;
import com.challengeteam.shop.service.impl.validator.CartValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CartValidatorImpl implements CartValidator {

    private static final Integer MIN_AMOUNT = 1;
    private static final Integer MAX_AMOUNT = 20;
    private static final Integer TOTAL_AMOUNT = 100;

    @Override
    public void validateItemAmount(Integer amount) {
        if (amount < MIN_AMOUNT || amount > MAX_AMOUNT) {
            throw new InvalidCartItemAmountException("Cart item amount must be between " + MIN_AMOUNT + " and " + MAX_AMOUNT);
        }
    }

    @Override
    public void validateTotalAmount(Cart cart) {
        int totalAmountInCart = cart.getCartItems().stream()
                .mapToInt(CartItem::getAmount)
                .sum();

        if (totalAmountInCart > TOTAL_AMOUNT) {
            throw new InvalidCartItemAmountException("Cart items total amount must be less than " + TOTAL_AMOUNT);
        }
    }

}
