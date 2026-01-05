package com.challengeteam.shop.service.impl.validator.impl;

import com.challengeteam.shop.entity.cart.Cart;
import com.challengeteam.shop.entity.cart.CartItem;
import com.challengeteam.shop.exceptionHandling.exception.InvalidCartItemAmountException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class CartValidatorImplTest {

    @InjectMocks
    private CartValidatorImpl cartValidator;

    @Nested
    class ValidateItemAmountTest {

        @Test
        void whenAmountIsValid_thenDoNothing() {
            // given
            Integer amount = 5;

            // when + then
            assertDoesNotThrow(() -> cartValidator.validateItemAmount(amount));
        }

        @Test
        void whenAmountIsLessThanMin_thenThrowException() {
            // given
            Integer amount = 0;

            // when + then
            assertThrows(
                    InvalidCartItemAmountException.class,
                    () -> cartValidator.validateItemAmount(amount)
            );
        }

        @Test
        void whenAmountIsGreaterThanMax_thenThrowException() {
            // given
            Integer amount = 25;

            // when + then
            assertThrows(
                    InvalidCartItemAmountException.class,
                    () -> cartValidator.validateItemAmount(amount)
            );
        }
    }

    @Nested
    class ValidateTotalAmountTest {

        @Test
        void whenTotalAmountIsValid_thenDoNothing() {
            // given
            CartItem item1 = new CartItem();
            item1.setAmount(30);

            CartItem item2 = new CartItem();
            item2.setAmount(40);

            Cart cart = new Cart();
            cart.setCartItems(List.of(item1, item2));

            Integer newAmount = 20; // 30 + 40 + 20 = 90

            // when + then
            assertDoesNotThrow(() -> cartValidator.validateTotalAmount(cart, newAmount));
        }

        @Test
        void whenTotalAmountExceedsLimit_thenThrowException() {
            // given
            CartItem item1 = new CartItem();
            item1.setAmount(60);

            CartItem item2 = new CartItem();
            item2.setAmount(30);

            Cart cart = new Cart();
            cart.setCartItems(List.of(item1, item2));

            Integer newAmount = 20; // 60 + 30 + 20 = 110

            // when + then
            assertThrows(
                    InvalidCartItemAmountException.class,
                    () -> cartValidator.validateTotalAmount(cart, newAmount)
            );
        }
    }
}
