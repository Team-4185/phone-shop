package com.challengeteam.shop.utility;

import com.challengeteam.shop.entity.cart.Cart;
import com.challengeteam.shop.entity.cart.CartItem;
import com.challengeteam.shop.entity.phone.Phone;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.challengeteam.shop.utility.CartUtilityTest.TestResources.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CartUtilityTest {

    @Nested
    class CountCartTotalPriceTest {

        @Test
        void whenGivenCartWithItems_thenReturnTotalPrice() {
            // when
            BigDecimal result = CartUtility.countCartTotalPrice(buildCartWithItems());

            // then
            assertThat(result).isEqualTo(TOTAL_PRICE);
        }

        @Test
        void whenGivenEmptyCart_thenReturnZero() {
            // when
            BigDecimal result = CartUtility.countCartTotalPrice(buildEmptyCart());

            // then
            assertThat(result).isEqualTo(BigDecimal.ZERO);
        }

        @Test
        void whenGivenCartWithSingleItem_thenReturnCorrectPrice() {
            // when
            BigDecimal result = CartUtility.countCartTotalPrice(buildCartWithSingleItem());

            // then
            assertThat(result).isEqualTo(SINGLE_ITEM_TOTAL_PRICE);
        }

        @Test
        void whenParameterCartIsNull_thenThrowException() {
            // when + then
            assertThatThrownBy(() -> CartUtility.countCartTotalPrice(null))
                    .isInstanceOf(NullPointerException.class);
        }

    }

    @Nested
    class CountTotalAmountTest {

        @Test
        void whenGivenCartWithItems_thenReturnTotalAmount() {
            // when
            Integer result = CartUtility.countTotalAmount(buildCartWithItems());

            // then
            assertThat(result).isEqualTo(TOTAL_AMOUNT);
        }

        @Test
        void whenGivenEmptyCart_thenReturnZero() {
            // when
            Integer result = CartUtility.countTotalAmount(buildEmptyCart());

            // then
            assertThat(result).isEqualTo(0);
        }

        @Test
        void whenGivenCartWithSingleItem_thenReturnCorrectPrice() {
            // when
            Integer result = CartUtility.countTotalAmount(buildCartWithSingleItem());

            // then
            assertThat(result).isEqualTo(1);
        }

        @Test
        void whenParameterCartIsNull_thenThrowException() {
            // when + then
            assertThatThrownBy(() -> CartUtility.countCartTotalPrice(null))
                    .isInstanceOf(NullPointerException.class);
        }

    }

    @Nested
    class IsCartHasPhoneTest {

        @Test
        void whenGivenCartWithPhone_thenReturnTrue() {
            // when
            boolean result = CartUtility.isCartHasPhone(buildCartWithItems(), PHONE_ID);

            // then
            assertThat(result).isTrue();
        }

        @Test
        void whenGivenCartWithoutPhone_thenReturnFalse() {
            // when
            boolean result = CartUtility.isCartHasPhone(buildCartWithItems(), UNKNOWN_PHONE_ID);

            // then
            assertThat(result).isFalse();
        }

        @Test
        void whenGivenEmptyCart_thenReturnFalse() {
            // when
            boolean result = CartUtility.isCartHasPhone(buildEmptyCart(), PHONE_ID);

            // then
            assertThat(result).isFalse();
        }

        @Test
        void whenParameterCartIsNull_thenThrowException() {
            // when + then
            assertThatThrownBy(() -> CartUtility.isCartHasPhone(null, PHONE_ID))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void whenParameterPhoneIdIsNull_thenThrowException() {
            // when + then
            assertThatThrownBy(() -> CartUtility.isCartHasPhone(buildCartWithItems(), null))
                    .isInstanceOf(NullPointerException.class);
        }

    }

    @Nested
    class GetCartItemAmountTest {

        @Test
        void whenGivenCartWithPhone_thenReturnItemAmount() {
            // when
            Integer result = CartUtility.getCartItemAmount(buildCartWithItems(), PHONE_ID);

            // then
            assertThat(result).isEqualTo(ITEM_AMOUNT);
        }

        @Test
        void whenGivenCartWithoutPhone_thenReturnZero() {
            // when
            Integer result = CartUtility.getCartItemAmount(buildCartWithItems(), UNKNOWN_PHONE_ID);

            // then
            assertThat(result).isEqualTo(0);
        }

        @Test
        void whenGivenEmptyCart_thenReturnZero() {
            // when
            Integer result = CartUtility.getCartItemAmount(buildEmptyCart(), PHONE_ID);

            // then
            assertThat(result).isEqualTo(0);
        }

        @Test
        void whenParameterCartIsNull_thenThrowException() {
            // when + then
            assertThatThrownBy(() -> CartUtility.getCartItemAmount(null, PHONE_ID))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void whenParameterPhoneIdIsNull_thenThrowException() {
            // when + then
            assertThatThrownBy(() -> CartUtility.getCartItemAmount(buildCartWithItems(), null))
                    .isInstanceOf(NullPointerException.class);
        }

    }

    static class TestResources {
        static final Long PHONE_ID = 1L;
        static final Long UNKNOWN_PHONE_ID = 999L;
        static final Integer ITEM_AMOUNT = 2;
        static final Integer TOTAL_AMOUNT = 3;
        static final BigDecimal PHONE_PRICE = new BigDecimal("500.00");
        static final BigDecimal SINGLE_ITEM_TOTAL_PRICE = new BigDecimal("500.00");
        static final BigDecimal TOTAL_PRICE = new BigDecimal("1500.00");

        static Cart buildEmptyCart() {
            Cart cart = new Cart();
            cart.setCartItems(new ArrayList<>());
            return cart;
        }

        static Cart buildCartWithSingleItem() {
            Cart cart = new Cart();
            List<CartItem> cartItems = new ArrayList<>();
            cartItems.add(buildCartItem(PHONE_ID, PHONE_PRICE, 1));
            cart.setCartItems(cartItems);
            return cart;
        }

        static Cart buildCartWithItems() {
            Cart cart = new Cart();
            List<CartItem> cartItems = new ArrayList<>();
            cartItems.add(buildCartItem(PHONE_ID, PHONE_PRICE, ITEM_AMOUNT));
            cartItems.add(buildCartItem(2L, new BigDecimal("500.00"), 1));
            cart.setCartItems(cartItems);
            return cart;
        }

        static CartItem buildCartItem(Long phoneId, BigDecimal price, Integer amount) {
            CartItem cartItem = new CartItem();
            Phone phone = new Phone();
            phone.setId(phoneId);
            phone.setPrice(price);
            cartItem.setPhone(phone);
            cartItem.setAmount(amount);
            return cartItem;
        }
    }

}