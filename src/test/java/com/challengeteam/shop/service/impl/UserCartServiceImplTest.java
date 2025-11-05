package com.challengeteam.shop.service.impl;

import com.challengeteam.shop.dto.cart.CartItemAddRequestDto;
import com.challengeteam.shop.dto.cart.CartItemUpdateRequestDto;
import com.challengeteam.shop.entity.cart.Cart;
import com.challengeteam.shop.entity.cart.CartItem;
import com.challengeteam.shop.exceptionHandling.exception.ResourceNotFoundException;
import com.challengeteam.shop.service.CartService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserCartServiceImplTest {

    @Mock
    private CartService cartService;

    @InjectMocks
    private UserCartServiceImpl userCartService;

    @Nested
    class GetUserCartTest {

        @Test
        void whenCartExists_thenReturnCart() {
            // mockito
            when(cartService.getCartByUserId(TestResources.USER_ID)).thenReturn(TestResources.buildCart());

            // when
            Optional<Cart> result = userCartService.getUserCart(TestResources.USER_ID);

            // then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(TestResources.buildCart());
            verify(cartService).getCartByUserId(TestResources.USER_ID);
        }
    }

    @Nested
    class AddItemToUserCartTest {

        @Test
        void whenCalled_thenDelegateToCartService() {
            // mockito
            when(cartService.getCartByUserId(TestResources.USER_ID)).thenReturn(TestResources.buildCart());
            when(cartService.getCartItem(TestResources.CART_ID, TestResources.PHONE_ID)).thenReturn(Optional.empty());
            when(cartService.addItemToCart(TestResources.CART_ID, TestResources.buildCartItemAddRequestDto()))
                    .thenReturn(Optional.of(TestResources.buildCart()));

            // when
            userCartService.addItemToUserCart(TestResources.USER_ID, TestResources.buildCartItemAddRequestDto());

            // then
            verify(cartService).getCartByUserId(TestResources.USER_ID);
            verify(cartService).getCartItem(TestResources.CART_ID, TestResources.PHONE_ID);
            verify(cartService).addItemToCart(TestResources.CART_ID, TestResources.buildCartItemAddRequestDto());
        }

        @Test
        void whenItemAlreadyExists_thenUpdateAmount() {
            // mockito
            CartItem existingItem = TestResources.buildCartItem(3);
            when(cartService.getCartByUserId(TestResources.USER_ID)).thenReturn(TestResources.buildCart());
            when(cartService.getCartItem(TestResources.CART_ID, TestResources.PHONE_ID)).thenReturn(Optional.of(existingItem));
            when(cartService.updateAmountCartItem(TestResources.CART_ID, new CartItemUpdateRequestDto(TestResources.PHONE_ID, 4)))
                    .thenReturn(Optional.of(TestResources.buildCart()));

            // when
            userCartService.addItemToUserCart(TestResources.USER_ID, TestResources.buildCartItemAddRequestDto());

            // then
            verify(cartService).getCartByUserId(TestResources.USER_ID);
            verify(cartService).getCartItem(TestResources.CART_ID, TestResources.PHONE_ID);
            verify(cartService).updateAmountCartItem(TestResources.CART_ID, new CartItemUpdateRequestDto(TestResources.PHONE_ID, 4));
        }
    }

    @Nested
    class UpdateAmountUserCartItemTest {

        @Test
        void whenCalled_thenDelegateToCartService() {
            // mockito
            when(cartService.getCartByUserId(TestResources.USER_ID)).thenReturn(TestResources.buildCart());
            when(cartService.updateAmountCartItem(TestResources.CART_ID, TestResources.buildCartItemUpdateRequestDto()))
                    .thenReturn(Optional.of(TestResources.buildCart()));

            // when
            userCartService.updateAmountUserCartItem(TestResources.USER_ID, TestResources.buildCartItemUpdateRequestDto());

            // then
            verify(cartService).getCartByUserId(TestResources.USER_ID);
            verify(cartService).updateAmountCartItem(TestResources.CART_ID, TestResources.buildCartItemUpdateRequestDto());
        }
    }

    @Nested
    class RemoveItemFromUserCartTest {

        @Test
        void whenCalled_thenDelegateToCartService() {
            // mockito
            CartItem cartItem = TestResources.buildCartItem(1);
            when(cartService.getCartByUserId(TestResources.USER_ID)).thenReturn(TestResources.buildCart());
            when(cartService.getCartItem(TestResources.CART_ID, TestResources.PHONE_ID)).thenReturn(Optional.of(cartItem));
            when(cartService.removeItemFromCart(TestResources.CART_ID, TestResources.PHONE_ID))
                    .thenReturn(Optional.of(TestResources.buildCart()));

            // when
            userCartService.removeItemFromUserCart(TestResources.USER_ID, TestResources.PHONE_ID);

            // then
            verify(cartService).getCartByUserId(TestResources.USER_ID);
            verify(cartService).getCartItem(TestResources.CART_ID, TestResources.PHONE_ID);
            verify(cartService).removeItemFromCart(TestResources.CART_ID, TestResources.PHONE_ID);
        }

        @Test
        void whenAmountGreaterThanOne_thenDecreaseAmount() {
            // mockito
            CartItem cartItem = TestResources.buildCartItem(3);
            when(cartService.getCartByUserId(TestResources.USER_ID)).thenReturn(TestResources.buildCart());
            when(cartService.getCartItem(TestResources.CART_ID, TestResources.PHONE_ID)).thenReturn(Optional.of(cartItem));
            when(cartService.updateAmountCartItem(TestResources.CART_ID, new CartItemUpdateRequestDto(TestResources.PHONE_ID, 2)))
                    .thenReturn(Optional.of(TestResources.buildCart()));

            // when
            userCartService.removeItemFromUserCart(TestResources.USER_ID, TestResources.PHONE_ID);

            // then
            verify(cartService).getCartByUserId(TestResources.USER_ID);
            verify(cartService).getCartItem(TestResources.CART_ID, TestResources.PHONE_ID);
            verify(cartService).updateAmountCartItem(TestResources.CART_ID, new CartItemUpdateRequestDto(TestResources.PHONE_ID, 2));
        }

        @Test
        void whenItemNotFound_thenThrowException() {
            // mockito
            when(cartService.getCartByUserId(TestResources.USER_ID)).thenReturn(TestResources.buildCart());
            when(cartService.getCartItem(TestResources.CART_ID, TestResources.PHONE_ID)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userCartService.removeItemFromUserCart(TestResources.USER_ID, TestResources.PHONE_ID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Phone with id " + TestResources.PHONE_ID + " not found in user cart");

            verify(cartService).getCartByUserId(TestResources.USER_ID);
            verify(cartService).getCartItem(TestResources.CART_ID, TestResources.PHONE_ID);
        }
    }

    @Nested
    class ClearUserCartTest {

        @Test
        void whenCalled_thenDelegateToCartService() {
            // mockito
            when(cartService.getCartByUserId(TestResources.USER_ID)).thenReturn(TestResources.buildCart());
            when(cartService.clearCart(TestResources.CART_ID)).thenReturn(Optional.of(TestResources.buildCart()));

            // when
            userCartService.clearUserCart(TestResources.USER_ID);

            // then
            verify(cartService).getCartByUserId(TestResources.USER_ID);
            verify(cartService).clearCart(TestResources.CART_ID);
        }
    }

    static class TestResources {
        static final Long CART_ID = 1L;
        static final Long PHONE_ID = 2L;
        static final Long USER_ID = 3L;

        static Cart buildCart() {
            Cart cart = new Cart();
            cart.setId(CART_ID);
            return cart;
        }

        static CartItem buildCartItem(Integer amount) {
            CartItem cartItem = new CartItem();
            cartItem.setAmount(amount);
            return cartItem;
        }

        static CartItemAddRequestDto buildCartItemAddRequestDto() {
            return new CartItemAddRequestDto(PHONE_ID, 1);
        }

        static CartItemUpdateRequestDto buildCartItemUpdateRequestDto() {
            return new CartItemUpdateRequestDto(PHONE_ID, 5);
        }

    }
}