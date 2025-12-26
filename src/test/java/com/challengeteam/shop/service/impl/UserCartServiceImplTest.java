package com.challengeteam.shop.service.impl;

import com.challengeteam.shop.dto.cart.CartItemAddRequestDto;
import com.challengeteam.shop.dto.cart.CartItemRemoveRequestDto;
import com.challengeteam.shop.entity.cart.Cart;
import com.challengeteam.shop.entity.cart.CartItem;
import com.challengeteam.shop.entity.phone.Phone;
import com.challengeteam.shop.entity.user.User;
import com.challengeteam.shop.exceptionHandling.exception.InvalidCartItemAmountException;
import com.challengeteam.shop.exceptionHandling.exception.ResourceNotFoundException;
import com.challengeteam.shop.service.CartService;
import com.challengeteam.shop.service.impl.validator.CartValidator;
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

@ExtendWith(MockitoExtension.class)
class UserCartServiceImplTest {

    @Mock
    private CartService cartService;

    @Mock
    private CartValidator cartValidator;

    @InjectMocks
    private UserCartServiceImpl userCartService;

    private static final Integer INVALID_AMOUNT = -1;

    @Nested
    class GetUserCartTest {

        @Test
        void whenCartExists_thenReturnOptionalWithCart() {
            // mockito
            Mockito.when(cartService.getCartByUserId(USER_ID))
                    .thenReturn(Optional.of(buildCart()));

            // when
            Optional<Cart> result = userCartService.getUserCart(USER_ID);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(CART_ID);
            Mockito.verify(cartService).getCartByUserId(USER_ID);
        }

        @Test
        void whenCartDoesNotExist_thenReturnEmptyOptional() {
            // mockito
            Mockito.when(cartService.getCartByUserId(USER_ID))
                    .thenReturn(Optional.empty());

            // when
            Optional<Cart> result = userCartService.getUserCart(USER_ID);

            // then
            assertThat(result).isNotPresent();
            Mockito.verify(cartService).getCartByUserId(USER_ID);
        }
    }

    @Nested
    class PutItemToUserCartTest {

        @Test
        void whenCartDoesNotHavePhone_thenAddItemToCart() {
            // given
            Cart cart = buildCart();
            CartItemAddRequestDto dto = buildAddDto();

            // mockito
            Mockito.when(cartService.getCartByUserId(USER_ID))
                    .thenReturn(Optional.of(cart));
            Mockito.when(cartService.putItemToCart(cart, dto))
                    .thenReturn(cart);

            // when
            Cart result = userCartService.putItemToUserCart(USER_ID, dto);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(CART_ID);
            Mockito.verify(cartService).getCartByUserId(USER_ID);
            Mockito.verify(cartService).putItemToCart(cart, dto);
        }

        @Test
        void whenCartAlreadyHasPhone_thenUpdateAmount() {
            // given
            Cart cart = buildCartWithItemAmount(1);
            CartItemAddRequestDto addDto = buildAddDto();
            int expectedNewAmount = 2;

            // mockito
            Mockito.when(cartService.getCartByUserId(USER_ID))
                    .thenReturn(Optional.of(cart));
            Mockito.when(cartService.updateAmountCartItem(cart, PHONE_ID, expectedNewAmount))
                    .thenReturn(cart);

            // when
            Cart result = userCartService.putItemToUserCart(USER_ID, addDto);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(CART_ID);
            Mockito.verify(cartService).getCartByUserId(USER_ID);
            Mockito.verify(cartService).updateAmountCartItem(cart, PHONE_ID, expectedNewAmount);
            Mockito.verify(cartService, Mockito.never()).putItemToCart(cart, addDto);
        }

        @Test
        void whenCartNotFound_thenThrowResourceNotFoundException() {
            // given
            CartItemAddRequestDto dto = buildAddDto();

            // mockito
            Mockito.when(cartService.getCartByUserId(USER_ID))
                    .thenReturn(Optional.empty());

            // when + then
            assertThatThrownBy(() -> userCartService.putItemToUserCart(USER_ID, dto))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        void whenAmountIsInvalid_thenThrowExceptionFromValidator() {
            // given
            Cart cart = buildCart();
            CartItemAddRequestDto dto =
                    buildCartItemAddRequestDtoWithAmount(INVALID_AMOUNT);

            // mockito
            Mockito.when(cartService.getCartByUserId(USER_ID))
                    .thenReturn(Optional.of(cart));
            Mockito.doThrow(InvalidCartItemAmountException.class)
                    .when(cartValidator)
                    .validateCartItemAmountToUpdate(dto.amount());

            // when + then
            assertThatThrownBy(() -> userCartService.putItemToUserCart(USER_ID, dto))
                    .isInstanceOf(InvalidCartItemAmountException.class);

            Mockito.verify(cartValidator)
                    .validateCartItemAmountToUpdate(dto.amount());
        }

        @Test
        void whenTotalAmountIsInvalid_thenThrowExceptionFromValidator() {
            // given
            Cart cart = buildCartWithItemAmount(10);
            CartItemAddRequestDto dto = buildAddDto();

            // mockito
            Mockito.when(cartService.getCartByUserId(USER_ID))
                    .thenReturn(Optional.of(cart));
            Mockito.doThrow(InvalidCartItemAmountException.class)
                    .when(cartValidator)
                    .validateCartItemTotalAmount(11);

            // when + then
            assertThatThrownBy(() -> userCartService.putItemToUserCart(USER_ID, dto))
                    .isInstanceOf(InvalidCartItemAmountException.class);

            Mockito.verify(cartValidator)
                    .validateCartItemTotalAmount(11);
            Mockito.verify(cartService, Mockito.never())
                    .updateAmountCartItem(Mockito.any(), Mockito.any(), Mockito.any());
        }

    }

    @Nested
    class RemoveItemFromUserCartTest {

        @Test
        void whenAmountIsGreaterThanRemoveAmount_thenDecreaseAmount() {
            // given
            Cart cart = buildCartWithItemAmount(5);
            CartItemRemoveRequestDto removeRequest = buildRemoveDto();
            Integer expectedNewAmount = 4;
            Cart updatedCart = buildCartWithItemAmount(expectedNewAmount);

            // mockito
            Mockito.when(cartService.getCartByUserId(USER_ID))
                    .thenReturn(Optional.of(cart));
            Mockito.when(cartService.updateAmountCartItem(cart, PHONE_ID, expectedNewAmount))
                    .thenReturn(updatedCart);

            // when
            Cart result = userCartService.removeItemFromUserCart(USER_ID, removeRequest);
            CartItem item = result.getCartItems().get(0);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(CART_ID);
            assertThat(item.getAmount()).isEqualTo(expectedNewAmount);
            Mockito.verify(cartService).getCartByUserId(USER_ID);
            Mockito.verify(cartService).updateAmountCartItem(cart, PHONE_ID, expectedNewAmount);
            Mockito.verify(cartService, Mockito.never()).removeItemFromCart(cart, PHONE_ID);
        }

        @Test
        void whenAmountEqualsRemoveAmount_thenRemoveItemCompletely() {
            // given
            Cart cart = buildCartWithItemAmount(1);
            CartItemRemoveRequestDto removeRequest = buildRemoveDto();

            // mockito
            Mockito.when(cartService.getCartByUserId(USER_ID))
                    .thenReturn(Optional.of(cart));
            Mockito.when(cartService.removeItemFromCart(cart, PHONE_ID))
                    .thenReturn(cart);

            // when
            Cart result = userCartService.removeItemFromUserCart(USER_ID, removeRequest);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(CART_ID);
            Mockito.verify(cartService).getCartByUserId(USER_ID);
            Mockito.verify(cartService).removeItemFromCart(cart, PHONE_ID);
            Mockito.verify(cartService, Mockito.never()).updateAmountCartItem(cart, PHONE_ID, 0);
        }

        @Test
        void whenPhoneNotFoundInCart_thenThrowResourceNotFoundException() {
            // given
            Cart cart = buildCart();
            CartItemRemoveRequestDto removeRequest = buildRemoveDto();

            // mockito
            Mockito.when(cartService.getCartByUserId(USER_ID))
                    .thenReturn(Optional.of(cart));

            // when + then
            assertThatThrownBy(() -> userCartService.removeItemFromUserCart(USER_ID, removeRequest))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        void whenCartNotFound_thenThrowResourceNotFoundException() {
            // given
            CartItemRemoveRequestDto removeRequest = buildRemoveDto();

            // mockito
            Mockito.when(cartService.getCartByUserId(USER_ID))
                    .thenReturn(Optional.empty());

            // when + then
            assertThatThrownBy(() -> userCartService.removeItemFromUserCart(USER_ID, removeRequest))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        void whenRemoveAmountIsGreaterThanCurrent_thenThrowException() {
            // given
            Cart cart = buildCartWithItemAmount(1);
            CartItemRemoveRequestDto dto = buildCartItemRemoveRequestDtoWithAmount(2);

            // mockito
            Mockito.when(cartService.getCartByUserId(USER_ID))
                    .thenReturn(Optional.of(cart));

            // when + then
            assertThatThrownBy(() ->
                    userCartService.removeItemFromUserCart(USER_ID, dto))
                    .isInstanceOf(InvalidCartItemAmountException.class);

            Mockito.verify(cartService, Mockito.never())
                    .updateAmountCartItem(Mockito.any(), Mockito.any(), Mockito.any());
            Mockito.verify(cartService, Mockito.never())
                    .removeItemFromCart(Mockito.any(), Mockito.any());
        }
    }

    @Nested
    class ClearUserCartTest {

        @Test
        void whenCalled_thenDelegateToCartService() {
            // given
            Cart cart = buildCartWithItem();

            // mockito
            Mockito.when(cartService.getCartByUserId(USER_ID))
                    .thenReturn(Optional.of(cart));
            Mockito.when(cartService.clearCart(cart))
                    .thenReturn(cart);

            // when
            Cart result = userCartService.clearUserCart(USER_ID);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(CART_ID);
            Mockito.verify(cartService).getCartByUserId(USER_ID);
            Mockito.verify(cartService).clearCart(cart);
        }

        @Test
        void whenCartNotFound_thenThrowResourceNotFoundException() {
            // mockito
            Mockito.when(cartService.getCartByUserId(USER_ID))
                    .thenReturn(Optional.empty());

            // when + then
            assertThatThrownBy(() -> userCartService.clearUserCart(USER_ID))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    static class TestResources {
        static final Long CART_ID = 10L;
        static final Long PHONE_ID = 5L;
        static final Long USER_ID = 1L;

        static Cart buildCart() {
            return Cart.builder()
                    .id(CART_ID)
                    .user(buildUser())
                    .cartItems(new ArrayList<>())
                    .totalPrice(BigDecimal.ZERO)
                    .build();
        }

        static User buildUser() {
            return User.builder()
                    .id(USER_ID)
                    .build();
        }

        static Cart buildCartWithItem() {
            return buildCartWithItemAmount(5);
        }

        static Cart buildCartWithItemAmount(Integer amount) {
            Cart cart = buildCart();
            cart.getCartItems().add(
                    CartItem.builder()
                            .id(1L)
                            .cart(cart)
                            .phone(buildPhone())
                            .amount(amount)
                            .build()
            );
            return cart;
        }

        static Phone buildPhone() {
            return Phone.builder()
                    .id(PHONE_ID)
                    .price(BigDecimal.valueOf(100))
                    .build();
        }

        static CartItemAddRequestDto buildAddDto() {
            return new CartItemAddRequestDto(PHONE_ID, 1);
        }

        static CartItemRemoveRequestDto buildCartItemRemoveRequestDtoWithAmount(Integer amount) {
            return new CartItemRemoveRequestDto(PHONE_ID, amount);
        }

        static CartItemRemoveRequestDto buildRemoveDto() {
            return new CartItemRemoveRequestDto(PHONE_ID, 1);
        }

        static CartItemAddRequestDto buildCartItemAddRequestDtoWithAmount(Integer amount) {
            return new CartItemAddRequestDto(PHONE_ID, amount);
        }
    }
}