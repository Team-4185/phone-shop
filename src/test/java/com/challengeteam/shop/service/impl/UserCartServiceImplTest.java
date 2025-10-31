package com.challengeteam.shop.service.impl;

import com.challengeteam.shop.dto.cart.CartItemAddRequestDto;
import com.challengeteam.shop.dto.cart.CartItemUpdateRequestDto;
import com.challengeteam.shop.entity.cart.Cart;
import com.challengeteam.shop.service.CartService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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
            when(cartService.getCartIdByUserId(TestResources.USER_ID)).thenReturn(TestResources.CART_ID);
            when(cartService.getCart(TestResources.CART_ID)).thenReturn(Optional.of(TestResources.buildCart()));

            // when
            Optional<Cart> result = userCartService.getUserCart(TestResources.USER_ID);

            // then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(TestResources.buildCart());
            verify(cartService).getCartIdByUserId(TestResources.USER_ID);
            verify(cartService).getCart(TestResources.CART_ID);
        }
    }

    @Nested
    class AddItemToUserCartTest {

        @Test
        void whenCalled_thenDelegateToCartService() {
            // mockito
            when(cartService.getCartIdByUserId(TestResources.USER_ID)).thenReturn(TestResources.CART_ID);

            // when
            userCartService.addItemToUserCart(TestResources.USER_ID, TestResources.buildCartItemAddRequestDto());

            // then
            verify(cartService).getCartIdByUserId(TestResources.USER_ID);
            verify(cartService).addItemToCart(TestResources.CART_ID, TestResources.buildCartItemAddRequestDto());
        }
    }

    @Nested
    class UpdateAmountUserCartItemTest {

        @Test
        void whenCalled_thenDelegateToCartService() {
            // mockito
            when(cartService.getCartIdByUserId(TestResources.USER_ID)).thenReturn(TestResources.CART_ID);

            // when
            userCartService.updateAmountUserCartItem(TestResources.USER_ID, TestResources.buildCartItemUpdateRequestDto());

            // then
            verify(cartService).getCartIdByUserId(TestResources.USER_ID);
            verify(cartService).updateAmountCartItem(TestResources.CART_ID, TestResources.buildCartItemUpdateRequestDto());
        }
    }

    @Nested
    class RemoveItemFromUserCartTest {

        @Test
        void whenCalled_thenDelegateToCartService() {
            // mockito
            when(cartService.getCartIdByUserId(TestResources.USER_ID)).thenReturn(TestResources.CART_ID);

            // when
            userCartService.removeItemFromUserCart(TestResources.USER_ID, TestResources.PHONE_ID);

            // then
            verify(cartService).getCartIdByUserId(TestResources.USER_ID);
            verify(cartService).removeItemFromCart(TestResources.CART_ID, TestResources.PHONE_ID);
        }
    }

    @Nested
    class ClearUserCartTest {

        @Test
        void whenCalled_thenDelegateToCartService() {
            // mockito
            when(cartService.getCartIdByUserId(TestResources.USER_ID)).thenReturn(TestResources.CART_ID);

            // when
            userCartService.clearUserCart(TestResources.USER_ID);

            // then
            verify(cartService).getCartIdByUserId(TestResources.USER_ID);
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

        static CartItemAddRequestDto buildCartItemAddRequestDto() {
            return new CartItemAddRequestDto(PHONE_ID, 1);
        }

        static CartItemUpdateRequestDto buildCartItemUpdateRequestDto() {
            return new CartItemUpdateRequestDto(PHONE_ID, 5);
        }

    }
}
