package com.challengeteam.shop.service.impl;

import com.challengeteam.shop.dto.cart.CartItemAddRequestDto;
import com.challengeteam.shop.dto.cart.CartItemRemoveRequestDto;
import com.challengeteam.shop.dto.cart.CartItemUpdateRequestDto;
import com.challengeteam.shop.entity.cart.Cart;
import com.challengeteam.shop.exceptionHandling.exception.ResourceNotFoundException;
import com.challengeteam.shop.service.CartService;
import com.challengeteam.shop.utility.CartUtility;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
public class UserCartServiceImplTest {

    @Mock
    private CartService cartService;

    @InjectMocks
    private UserCartServiceImpl userCartService;

    @Nested
    class GetUserCartTest {

        @Test
        void whenCartExists_thenReturnOptionalWithCart() {
            // given
            Cart cart = TestData.buildCart();
            Cart expected = TestData.buildCart();

            // mockito
            Mockito.when(cartService.getCartByUserId(TestData.USER_ID))
                    .thenReturn(Optional.of(cart));

            // when
            Optional<Cart> result = userCartService.getUserCart(TestData.USER_ID);

            // then
            assertNotNull(result);
            assertTrue(result.isPresent());
            assertEquals(expected.getId(), result.get().getId());
            Mockito.verify(cartService).getCartByUserId(TestData.USER_ID);
        }

        @Test
        void whenCartDoesNotExist_thenReturnEmptyOptional() {
            // given
            // ...

            // mockito
            Mockito.when(cartService.getCartByUserId(TestData.USER_ID))
                    .thenReturn(Optional.empty());

            // when
            Optional<Cart> result = userCartService.getUserCart(TestData.USER_ID);

            // then
            assertNotNull(result);
            assertFalse(result.isPresent());
            Mockito.verify(cartService).getCartByUserId(TestData.USER_ID);
        }
    }

    @Nested
    class PutItemToUserCartTest {

        @Test
        void whenCartDoesNotHavePhone_thenAddItemToCart() {
            // given
            Cart cart = TestData.buildCart();
            CartItemAddRequestDto dto = TestData.buildCartItemAddRequestDto();

            // mockito
            Mockito.when(cartService.getCartByUserId(TestData.USER_ID))
                    .thenReturn(Optional.of(cart));
            Mockito.when(cartService.putItemToCart(cart, dto))
                    .thenReturn(cart);

            // when
            try (MockedStatic<CartUtility> util = mockStatic(CartUtility.class)) {
                util.when(() -> CartUtility.isCartHasPhone(cart, TestData.PHONE_ID))
                        .thenReturn(false);

                Cart result = userCartService.putItemToUserCart(TestData.USER_ID, dto);

                // then
                assertNotNull(result);
                assertEquals(cart, result);
                Mockito.verify(cartService).putItemToCart(cart, dto);
            }
        }

        @Test
        void whenCartAlreadyHasPhone_thenUpdateAmount() {
            // given
            Cart cart = TestData.buildCart();
            CartItemAddRequestDto dto = TestData.buildCartItemAddRequestDto();
            CartItemUpdateRequestDto expectedUpdate = new CartItemUpdateRequestDto(TestData.PHONE_ID, 3);

            // mockito
            Mockito.when(cartService.getCartByUserId(TestData.USER_ID))
                    .thenReturn(Optional.of(cart));
            Mockito.when(cartService.updateAmountCartItem(cart, expectedUpdate))
                    .thenReturn(cart);

            // when
            try (MockedStatic<CartUtility> util = mockStatic(CartUtility.class)) {
                util.when(() -> CartUtility.isCartHasPhone(cart, TestData.PHONE_ID))
                        .thenReturn(true);
                util.when(() -> CartUtility.getCartItemAmount(cart, TestData.PHONE_ID))
                        .thenReturn(2);

                Cart result = userCartService.putItemToUserCart(TestData.USER_ID, dto);

                // then
                assertNotNull(result);
                assertEquals(cart, result);
                Mockito.verify(cartService).updateAmountCartItem(cart, expectedUpdate);
            }
        }
    }

    @Nested
    class RemoveItemFromUserCartTest {

        @Test
        void whenAmountIsGreaterThanOne_thenDecreaseAmount() {
            // given
            Cart cart = TestData.buildCart();
            CartItemRemoveRequestDto removeRequest = new CartItemRemoveRequestDto(TestData.PHONE_ID, 1);
            CartItemUpdateRequestDto expectedUpdate = new CartItemUpdateRequestDto(TestData.PHONE_ID, 2);

            // mockito
            Mockito.when(cartService.getCartByUserId(TestData.USER_ID))
                    .thenReturn(Optional.of(cart));
            Mockito.when(cartService.updateAmountCartItem(cart, expectedUpdate))
                    .thenReturn(cart);

            // when
            try (MockedStatic<CartUtility> util = mockStatic(CartUtility.class)) {
                util.when(() -> CartUtility.getCartItemAmount(cart, TestData.PHONE_ID))
                        .thenReturn(3);

                Cart result = userCartService.removeItemFromUserCart(TestData.USER_ID, removeRequest);

                // then
                assertNotNull(result);
                assertEquals(cart, result);
                Mockito.verify(cartService).updateAmountCartItem(cart, expectedUpdate);
            }
        }

        @Test
        void whenAmountIsOne_thenRemoveItemCompletely() {
            // given
            Cart cart = TestData.buildCart();
            CartItemRemoveRequestDto removeRequest = new CartItemRemoveRequestDto(TestData.PHONE_ID, 1);

            // mockito
            Mockito.when(cartService.getCartByUserId(TestData.USER_ID))
                    .thenReturn(Optional.of(cart));
            Mockito.when(cartService.removeItemFromCart(cart, TestData.PHONE_ID))
                    .thenReturn(cart);

            // when
            try (MockedStatic<CartUtility> util = mockStatic(CartUtility.class)) {
                util.when(() -> CartUtility.getCartItemAmount(cart, TestData.PHONE_ID))
                        .thenReturn(1);

                Cart result = userCartService.removeItemFromUserCart(TestData.USER_ID, removeRequest);

                // then
                assertNotNull(result);
                assertEquals(cart, result);
                Mockito.verify(cartService).removeItemFromCart(cart, TestData.PHONE_ID);
            }
        }

        @Test
        void whenPhoneNotFoundInCart_thenThrowException() {
            // given
            Cart cart = TestData.buildCart();
            CartItemRemoveRequestDto removeRequest = new CartItemRemoveRequestDto(TestData.PHONE_ID, 1);

            // mockito
            Mockito.when(cartService.getCartByUserId(TestData.USER_ID))
                    .thenReturn(Optional.of(cart));

            // when + then
            try (MockedStatic<CartUtility> util = mockStatic(CartUtility.class)) {
                util.when(() -> CartUtility.getCartItemAmount(cart, TestData.PHONE_ID))
                        .thenThrow(new ResourceNotFoundException("Phone with id " + TestData.PHONE_ID + " not found in user cart"));

                assertThrows(ResourceNotFoundException.class,
                        () -> userCartService.removeItemFromUserCart(TestData.USER_ID, removeRequest));
            }
        }
    }

    @Nested
    class ClearUserCartTest {

        @Test
        void whenCalled_thenDelegateToCartService() {
            // given
            Cart cart = TestData.buildCart();

            // mockito
            Mockito.when(cartService.getCartByUserId(TestData.USER_ID))
                    .thenReturn(Optional.of(cart));
            Mockito.when(cartService.clearCart(cart))
                    .thenReturn(cart);

            // when
            Cart result = userCartService.clearUserCart(TestData.USER_ID);

            // then
            assertNotNull(result);
            assertEquals(cart, result);
            Mockito.verify(cartService).clearCart(cart);
        }
    }

    static class TestData {
        static final Long USER_ID = 1L;
        static final Long CART_ID = 10L;
        static final Long PHONE_ID = 5L;

        static Cart buildCart() {
            Cart cart = new Cart();
            cart.setId(CART_ID);
            return cart;
        }

        static CartItemAddRequestDto buildCartItemAddRequestDto() {
            return new CartItemAddRequestDto(PHONE_ID, 1);
        }
    }
}