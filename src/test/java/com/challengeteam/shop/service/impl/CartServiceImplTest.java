package com.challengeteam.shop.service.impl;

import com.challengeteam.shop.dto.cart.CartItemAddRequestDto;
import com.challengeteam.shop.entity.cart.Cart;
import com.challengeteam.shop.entity.cart.CartItem;
import com.challengeteam.shop.entity.phone.Phone;
import com.challengeteam.shop.exceptionHandling.exception.PhoneAlreadyInCartException;
import com.challengeteam.shop.exceptionHandling.exception.ResourceNotFoundException;
import com.challengeteam.shop.persistence.repository.CartItemRepository;
import com.challengeteam.shop.persistence.repository.CartRepository;
import com.challengeteam.shop.service.PhoneService;
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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private PhoneService phoneService;

    @Mock
    private CartValidator cartValidator;

    @InjectMocks
    private CartServiceImpl cartService;

    private static final Integer INVALID_AMOUNT = -1;

    @Nested
    class GetCartTest {

        @Test
        void whenCartExists_thenReturnCart() {
            // mockito
            Mockito.when(cartRepository.findById(CART_ID))
                    .thenReturn(Optional.of(buildCart()));

            // when
            Optional<Cart> result = cartService.getCart(CART_ID);

            // then
            assertThat(result).isPresent();
            Mockito.verify(cartRepository).findById(CART_ID);
        }

        @Test
        void whenCartNotExists_thenReturnEmptyOptional() {
            // mockito
            Mockito.when(cartRepository.findById(CART_ID))
                    .thenReturn(Optional.empty());

            // when
            Optional<Cart> result = cartService.getCart(CART_ID);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        void whenIdIsNull_thenThrowException() {
            // when + then
            assertThatThrownBy(() -> cartService.getCart(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    class PutItemToCartTest {

        @Test
        void whenItemNotInCart_thenAddSuccessfully() {
            // given
            Cart cart = buildCart();
            CartItemAddRequestDto dto = buildCartItemAddRequestDto();

            // mockito
            Mockito.when(phoneService.getById(PHONE_ID))
                    .thenReturn(Optional.of(buildPhone()));
            Mockito.when(cartRepository.save(cart))
                    .thenReturn(cart);

            //when
            Cart result = cartService.putItemToCart(cart, dto);

            // then
            assertThat(result.getCartItems()).hasSize(1);
            assertThat(result.getTotalPrice()).isEqualTo(BigDecimal.valueOf(100));
            Mockito.verify(cartValidator).validateItemAmount(dto.amount());
            Mockito.verify(cartValidator).validateTotalAmount(cart, dto.amount());
            Mockito.verify(cartItemRepository).save(any(CartItem.class));
            Mockito.verify(cartRepository).save(cart);
        }

        @Test
        void whenPhoneAlreadyInCart_thenThrowException() {
            // given
            Cart cart = buildCartWithItem();

            // mockito
            Mockito.when(phoneService.getById(PHONE_ID))
                    .thenReturn(Optional.of(buildPhone()));

            // when + then
            assertThatThrownBy(() -> cartService.putItemToCart(cart, buildCartItemAddRequestDto()))
                    .isInstanceOf(PhoneAlreadyInCartException.class);
        }

        @Test
        void whenPhoneNotFound_thenThrowException() {
            // mockito
            Mockito.when(phoneService.getById(PHONE_ID))
                    .thenReturn(Optional.empty());

            // when + then
            assertThatThrownBy(() -> cartService.putItemToCart(buildCart(), buildCartItemAddRequestDto()))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        void whenAmountInvalid_thenThrowExceptionFromValidator() {
            // given
            CartItemAddRequestDto dto = buildCartItemAddRequestDtoWithAmount(INVALID_AMOUNT);

            // mockito
            Mockito.doThrow(IllegalArgumentException.class)
                    .when(cartValidator).validateItemAmount(INVALID_AMOUNT);

            // when + then
            assertThatThrownBy(() -> cartService.putItemToCart(buildCart(), dto))
                    .isInstanceOf(IllegalArgumentException.class);
            Mockito.verifyNoInteractions(phoneService);
        }
    }

    @Nested
    class UpdateAmountCartItemTest {

        @Test
        void whenItemExists_thenUpdateSuccessfully() {
            // given
            Cart cart = buildCart();
            CartItem item = buildCartItem(cart);

            // mockito
            Mockito.when(cartItemRepository.findByCartIdAndPhoneId(CART_ID, PHONE_ID))
                    .thenReturn(Optional.of(item));
            Mockito.when(cartRepository.save(cart))
                    .thenReturn(cart);

            // when
            Cart result = cartService.updateAmountCartItem(cart, PHONE_ID, 5);

            // then
            assertThat(result).isNotNull();
            Mockito.verify(cartValidator).validateTotalAmount(cart, 4);
            Mockito.verify(cartItemRepository).save(item);
        }

        @Test
        void whenItemNotFound_thenThrowException() {
            // mockito
            Mockito.when(cartItemRepository.findByCartIdAndPhoneId(CART_ID, PHONE_ID))
                    .thenReturn(Optional.empty());

            // when + then
            assertThatThrownBy(() -> cartService.updateAmountCartItem(buildCart(), PHONE_ID, 5))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    class RemoveItemFromCartTest {

        @Test
        void whenItemExists_thenRemoveSuccessfully() {
            // given
            Cart cart = buildCartWithItem();
            CartItem item = cart.getCartItems().get(0);

            // mockito
            Mockito.when(cartItemRepository.findByCartIdAndPhoneId(CART_ID, PHONE_ID))
                    .thenReturn(Optional.of(item));
            Mockito.when(cartRepository.save(cart))
                    .thenReturn(cart);

            // when
            Cart result = cartService.removeItemFromCart(cart, PHONE_ID);

            // then
            assertThat(result.getCartItems()).isEmpty();
            Mockito.verify(cartItemRepository).delete(item);
        }
    }

    @Nested
    class ClearCartTest {

        @Test
        void whenClearCart_thenDeleteAllItems() {
            // given
            Cart cart = buildCartWithItem();

            // mockito
            Mockito.when(cartRepository.save(cart))
                    .thenReturn(cart);

            // when
            Cart result = cartService.clearCart(cart);

            // then
            assertThat(result.getCartItems()).isEmpty();
            assertThat(result.getTotalPrice()).isEqualTo(BigDecimal.ZERO);
            Mockito.verify(cartItemRepository).deleteAllByCartId(CART_ID);
        }
    }

    @Nested
    class GetCartByUserIdTest {

        @Test
        void whenCartExists_thenReturnCart() {
            // mockito
            Mockito.when(cartRepository.findByUserId(USER_ID))
                    .thenReturn(Optional.of(buildCart()));

            // when
            Optional<Cart> result = cartService.getCartByUserId(USER_ID);

            // then
            assertThat(result).isPresent();
        }

        @Test
        void whenUserIdNull_thenThrowException() {
            // when + then
            assertThatThrownBy(() -> cartService.getCartByUserId(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    static final Long CART_ID = 1L;
    static final Long PHONE_ID = 2L;
    static final Long USER_ID = 3L;

    static Cart buildCart() {
        return Cart.builder()
                .id(CART_ID)
                .cartItems(new ArrayList<>())
                .totalPrice(BigDecimal.ZERO)
                .build();
    }

    static Cart buildCartWithItem() {
        Cart cart = buildCart();
        cart.getCartItems().add(buildCartItem(cart));
        return cart;
    }

    static CartItem buildCartItem(Cart cart) {
        return CartItem.builder()
                .id(1L)
                .cart(cart)
                .phone(buildPhone())
                .amount(1)
                .build();
    }

    static Phone buildPhone() {
        return Phone.builder()
                .id(PHONE_ID)
                .price(BigDecimal.valueOf(100))
                .build();
    }

    static CartItemAddRequestDto buildCartItemAddRequestDto() {
        return new CartItemAddRequestDto(PHONE_ID, 1);
    }

    static CartItemAddRequestDto buildCartItemAddRequestDtoWithAmount(Integer amount) {
        return new CartItemAddRequestDto(PHONE_ID, amount);
    }
}
