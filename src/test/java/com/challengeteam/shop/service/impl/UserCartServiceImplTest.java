package com.challengeteam.shop.service.impl;

import com.challengeteam.shop.dto.cart.CartItemAddRequestDto;
import com.challengeteam.shop.dto.cart.CartItemRemoveRequestDto;
import com.challengeteam.shop.entity.cart.Cart;
import com.challengeteam.shop.entity.cart.CartItem;
import com.challengeteam.shop.entity.phone.Phone;
import com.challengeteam.shop.exceptionHandling.exception.ResourceNotFoundException;
import com.challengeteam.shop.service.CartService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static com.challengeteam.shop.service.impl.UserCartServiceImplTest.TestResources.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
            // given
            Cart expectedCart = buildCart();

            // mockito
            Mockito.when(cartService.getCartByUserId(USER_ID))
                    .thenReturn(Optional.of(expectedCart));

            // when
            Optional<Cart> result = userCartService.getUserCart(USER_ID);

            // then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(expectedCart);
            Mockito.verify(cartService).getCartByUserId(USER_ID);
        }

        @Test
        void whenCartNotExists_thenReturnEmptyOptional() {
            // mockito
            Mockito.when(cartService.getCartByUserId(USER_ID))
                    .thenReturn(Optional.empty());

            // when
            Optional<Cart> result = userCartService.getUserCart(USER_ID);

            // then
            assertThat(result).isNotPresent();
            Mockito.verify(cartService).getCartByUserId(USER_ID);
        }

        @Test
        void whenUserIdIsNull_thenThrowException() {
            // when + then
            assertThatThrownBy(() -> userCartService.getUserCart(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    class PutItemToUserCartTest {

        @Test
        void whenPhoneNotInCart_thenAddNewItem() {
            // given
            Cart cart = buildCart();
            CartItemAddRequestDto dto = buildCartItemAddRequestDto(5);

            // mockito
            Mockito.when(cartService.getCartByUserId(USER_ID))
                    .thenReturn(Optional.of(cart));
            Mockito.when(cartService.putItemToCart(cart, dto))
                    .thenReturn(cart);

            // when
            Cart result = userCartService.putItemToUserCart(USER_ID, dto);

            // then
            assertThat(result).isNotNull();
            Mockito.verify(cartService).getCartByUserId(USER_ID);
            Mockito.verify(cartService).putItemToCart(cart, dto);
            Mockito.verify(cartService, never()).updateAmountCartItem(any(), any(), any());
        }

        @Test
        void whenPhoneAlreadyInCart_thenUpdateAmount() {
            // given
            Cart cart = buildCartWithItem(3);
            CartItemAddRequestDto dto = buildCartItemAddRequestDto(5);
            Integer expectedNewAmount = 8;

            // mockito
            Mockito.when(cartService.getCartByUserId(USER_ID))
                    .thenReturn(Optional.of(cart));
            Mockito.when(cartService.updateAmountCartItem(cart, PHONE_ID, expectedNewAmount))
                    .thenReturn(cart);

            // when
            Cart result = userCartService.putItemToUserCart(USER_ID, dto);

            // then
            assertThat(result).isNotNull();
            Mockito.verify(cartService).getCartByUserId(USER_ID);
            Mockito.verify(cartService).updateAmountCartItem(cart, PHONE_ID, expectedNewAmount);
            Mockito.verify(cartService, never()).putItemToCart(any(), any());
        }

        @Test
        void whenCartNotFound_thenThrowException() {
            // given
            CartItemAddRequestDto dto = buildCartItemAddRequestDto(5);

            // mockito
            Mockito.when(cartService.getCartByUserId(USER_ID))
                    .thenReturn(Optional.empty());

            // when + then
            assertThatThrownBy(() -> userCartService.putItemToUserCart(USER_ID, dto))
                    .isInstanceOf(ResourceNotFoundException.class);

            Mockito.verify(cartService).getCartByUserId(USER_ID);
            Mockito.verify(cartService, never()).putItemToCart(any(), any());
            Mockito.verify(cartService, never()).updateAmountCartItem(any(), any(), any());
        }

        @Test
        void whenUserIdIsNull_thenThrowException() {
            // given
            CartItemAddRequestDto dto = buildCartItemAddRequestDto(5);

            // when + then
            assertThatThrownBy(() -> userCartService.putItemToUserCart(null, dto))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void whenDtoIsNull_thenThrowException() {
            // when + then
            assertThatThrownBy(() -> userCartService.putItemToUserCart(USER_ID, null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    class RemoveItemFromUserCartTest {

        @Test
        void whenAmountLessThanCurrent_thenDecreaseAmount() {
            // given
            Cart cart = buildCartWithItem(10);
            CartItemRemoveRequestDto dto = buildCartItemRemoveRequestDto(3);
            Integer expectedNewAmount = 7;

            // mockito
            Mockito.when(cartService.getCartByUserId(USER_ID))
                    .thenReturn(Optional.of(cart));
            Mockito.when(cartService.updateAmountCartItem(cart, PHONE_ID, expectedNewAmount))
                    .thenReturn(cart);

            // when
            Cart result = userCartService.removeItemFromUserCart(USER_ID, dto);

            // then
            assertThat(result).isNotNull();
            Mockito.verify(cartService).getCartByUserId(USER_ID);
            Mockito.verify(cartService).updateAmountCartItem(cart, PHONE_ID, expectedNewAmount);
            Mockito.verify(cartService, never()).removeItemFromCart(any(), any());
        }

        @Test
        void whenAmountEqualsToCurrentAmount_thenRemoveItem() {
            // given
            Cart cart = buildCartWithItem(5);
            CartItemRemoveRequestDto dto = buildCartItemRemoveRequestDto(5);

            // mockito
            Mockito.when(cartService.getCartByUserId(USER_ID))
                    .thenReturn(Optional.of(cart));
            Mockito.when(cartService.removeItemFromCart(cart, PHONE_ID))
                    .thenReturn(cart);

            // when
            Cart result = userCartService.removeItemFromUserCart(USER_ID, dto);

            // then
            assertThat(result).isNotNull();
            Mockito.verify(cartService).getCartByUserId(USER_ID);
            Mockito.verify(cartService).removeItemFromCart(cart, PHONE_ID);
            Mockito.verify(cartService, never()).updateAmountCartItem(any(), any(), any());
        }

        @Test
        void whenAmountGreaterThanCurrent_thenRemoveItem() {
            // given
            Cart cart = buildCartWithItem(5);
            CartItemRemoveRequestDto dto = buildCartItemRemoveRequestDto(10);

            // mockito
            Mockito.when(cartService.getCartByUserId(USER_ID))
                    .thenReturn(Optional.of(cart));
            Mockito.when(cartService.removeItemFromCart(cart, PHONE_ID))
                    .thenReturn(cart);

            // when
            Cart result = userCartService.removeItemFromUserCart(USER_ID, dto);

            // then
            assertThat(result).isNotNull();
            Mockito.verify(cartService).getCartByUserId(USER_ID);
            Mockito.verify(cartService).removeItemFromCart(cart, PHONE_ID);
            Mockito.verify(cartService, never()).updateAmountCartItem(any(), any(), any());
        }

        @Test
        void whenPhoneNotInCart_thenThrowException() {
            // given
            Cart cart = buildCart();
            CartItemRemoveRequestDto dto = buildCartItemRemoveRequestDto(5);

            // mockito
            Mockito.when(cartService.getCartByUserId(USER_ID))
                    .thenReturn(Optional.of(cart));

            // when + then
            assertThatThrownBy(() -> userCartService.removeItemFromUserCart(USER_ID, dto))
                    .isInstanceOf(ResourceNotFoundException.class);

            Mockito.verify(cartService).getCartByUserId(USER_ID);
            Mockito.verify(cartService, never()).updateAmountCartItem(any(), any(), any());
            Mockito.verify(cartService, never()).removeItemFromCart(any(), any());
        }

        @Test
        void whenCartNotFound_thenThrowException() {
            // given
            CartItemRemoveRequestDto dto = buildCartItemRemoveRequestDto(5);

            // mockito
            Mockito.when(cartService.getCartByUserId(USER_ID))
                    .thenReturn(Optional.empty());

            // when + then
            assertThatThrownBy(() -> userCartService.removeItemFromUserCart(USER_ID, dto))
                    .isInstanceOf(ResourceNotFoundException.class);

            Mockito.verify(cartService).getCartByUserId(USER_ID);
            Mockito.verify(cartService, never()).updateAmountCartItem(any(), any(), any());
            Mockito.verify(cartService, never()).removeItemFromCart(any(), any());
        }

        @Test
        void whenUserIdIsNull_thenThrowException() {
            // given
            CartItemRemoveRequestDto dto = buildCartItemRemoveRequestDto(5);

            // when + then
            assertThatThrownBy(() -> userCartService.removeItemFromUserCart(null, dto))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void whenDtoIsNull_thenThrowException() {
            // when + then
            assertThatThrownBy(() -> userCartService.removeItemFromUserCart(USER_ID, null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    class ClearUserCartTest {

        @Test
        void whenCartExists_thenClearSuccessfully() {
            // given
            Cart cart = buildCartWithItem(5);

            // mockito
            Mockito.when(cartService.getCartByUserId(USER_ID))
                    .thenReturn(Optional.of(cart));
            Mockito.when(cartService.clearCart(cart))
                    .thenReturn(cart);

            // when
            Cart result = userCartService.clearUserCart(USER_ID);

            // then
            assertThat(result).isNotNull();
            Mockito.verify(cartService).getCartByUserId(USER_ID);
            Mockito.verify(cartService).clearCart(cart);
        }

        @Test
        void whenCartNotFound_thenThrowException() {
            // mockito
            Mockito.when(cartService.getCartByUserId(USER_ID))
                    .thenReturn(Optional.empty());

            // when + then
            assertThatThrownBy(() -> userCartService.clearUserCart(USER_ID))
                    .isInstanceOf(ResourceNotFoundException.class);

            Mockito.verify(cartService).getCartByUserId(USER_ID);
            Mockito.verify(cartService, never()).clearCart(any());
        }

        @Test
        void whenUserIdIsNull_thenThrowException() {
            // when + then
            assertThatThrownBy(() -> userCartService.clearUserCart(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    static class TestResources {

        static final Long USER_ID = 1L;
        static final Long CART_ID = 2L;
        static final Long PHONE_ID = 3L;

        static Cart buildCart() {
            return Cart.builder()
                    .id(CART_ID)
                    .cartItems(new ArrayList<>())
                    .totalPrice(BigDecimal.ZERO)
                    .build();
        }

        static Cart buildCartWithItem(Integer amount) {
            Cart cart = buildCart();
            CartItem cartItem = buildCartItem(cart, amount);
            cart.getCartItems().add(cartItem);
            cart.setTotalPrice(BigDecimal.valueOf(100L * amount));
            return cart;
        }

        static CartItem buildCartItem(Cart cart, Integer amount) {
            return CartItem.builder()
                    .id(1L)
                    .cart(cart)
                    .phone(buildPhone())
                    .amount(amount)
                    .build();
        }

        static Phone buildPhone() {
            return Phone.builder()
                    .id(PHONE_ID)
                    .price(BigDecimal.valueOf(100))
                    .build();
        }

        static CartItemAddRequestDto buildCartItemAddRequestDto(Integer amount) {
            return new CartItemAddRequestDto(PHONE_ID, amount);
        }

        static CartItemRemoveRequestDto buildCartItemRemoveRequestDto(Integer amount) {
            return new CartItemRemoveRequestDto(PHONE_ID, amount);
        }
    }
}