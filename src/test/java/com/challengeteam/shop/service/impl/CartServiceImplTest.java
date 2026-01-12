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

import static com.challengeteam.shop.service.impl.CartServiceImplTest.TestResources.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
            assertThat(result.get()).isEqualTo(buildCart());
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
            assertThat(result).isNotPresent();
            Mockito.verify(cartRepository).findById(CART_ID);
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

            // when
            Cart result = cartService.putItemToCart(cart, dto);

            // then
            assertThat(result.getCartItems()).hasSize(1);
            assertThat(result.getTotalPrice()).isEqualTo(BigDecimal.valueOf(100));
            Mockito.verify(cartValidator).validateItemAmount(dto.amount());
            Mockito.verify(phoneService).getById(PHONE_ID);
            Mockito.verify(cartValidator).validateTotalAmount(cart);
            Mockito.verify(cartItemRepository).save(any(CartItem.class));
            Mockito.verify(cartRepository).save(cart);
        }

        @Test
        void whenPhoneAlreadyInCart_thenThrowException() {
            // given
            Cart cart = buildCartWithItem();
            CartItemAddRequestDto dto = buildCartItemAddRequestDto();

            // mockito
            Mockito.when(phoneService.getById(PHONE_ID))
                    .thenReturn(Optional.of(buildPhone()));

            // when + then
            assertThatThrownBy(() -> cartService.putItemToCart(cart, dto))
                    .isInstanceOf(PhoneAlreadyInCartException.class);

            Mockito.verify(cartValidator).validateItemAmount(dto.amount());
            Mockito.verify(phoneService).getById(PHONE_ID);
            Mockito.verify(cartItemRepository, never()).save(any());
            Mockito.verify(cartRepository, never()).save(any());
        }

        @Test
        void whenPhoneNotFound_thenThrowException() {
            // given
            Cart cart = buildCart();
            CartItemAddRequestDto dto = buildCartItemAddRequestDto();

            // mockito
            Mockito.when(phoneService.getById(PHONE_ID))
                    .thenReturn(Optional.empty());

            // when + then
            assertThatThrownBy(() -> cartService.putItemToCart(cart, dto))
                    .isInstanceOf(ResourceNotFoundException.class);

            Mockito.verify(cartValidator).validateItemAmount(dto.amount());
            Mockito.verify(phoneService).getById(PHONE_ID);
            Mockito.verify(cartItemRepository, never()).save(any());
            Mockito.verify(cartRepository, never()).save(any());
        }

        @Test
        void whenCartIsNull_thenThrowException() {
            // given
            CartItemAddRequestDto dto = buildCartItemAddRequestDto();

            // when + then
            assertThatThrownBy(() -> cartService.putItemToCart(null, dto))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void whenDtoIsNull_thenThrowException() {
            // given
            Cart cart = buildCart();

            // when + then
            assertThatThrownBy(() -> cartService.putItemToCart(cart, null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    class UpdateAmountCartItemTest {

        @Test
        void whenItemExists_thenUpdateSuccessfully() {
            // given
            Cart cart = buildCart();
            CartItem item = buildCartItem(cart);
            cart.getCartItems().add(item);
            int newAmount = 5;

            // mockito
            Mockito.when(cartRepository.save(cart))
                    .thenReturn(cart);

            // when
            Cart result = cartService.updateAmountCartItem(cart, PHONE_ID, newAmount);

            // then
            assertThat(result).isNotNull();
            assertThat(item.getAmount()).isEqualTo(newAmount);
            assertThat(result.getTotalPrice()).isEqualTo(BigDecimal.valueOf(500)); // 5 * 100
            Mockito.verify(cartValidator).validateItemAmount(newAmount);
            Mockito.verify(cartValidator).validateTotalAmount(cart);
            Mockito.verify(cartItemRepository).save(item);
            Mockito.verify(cartRepository).save(cart);
        }

        @Test
        void whenItemNotFound_thenThrowException() {
            // given
            Cart cart = buildCart();
            int newAmount = 5;

            // when + then
            assertThatThrownBy(() -> cartService.updateAmountCartItem(cart, PHONE_ID, newAmount))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Phone with id " + PHONE_ID + " not found in cart");

            Mockito.verify(cartValidator).validateItemAmount(newAmount);
            Mockito.verify(cartItemRepository, never()).save(any());
            Mockito.verify(cartRepository, never()).save(any());
        }

        @Test
        void whenCartIsNull_thenThrowException() {
            // when + then
            assertThatThrownBy(() -> cartService.updateAmountCartItem(null, PHONE_ID, 5))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void whenPhoneIdIsNull_thenThrowException() {
            // given
            Cart cart = buildCart();

            // when + then
            assertThatThrownBy(() -> cartService.updateAmountCartItem(cart, null, 5))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void whenAmountIsNull_thenThrowException() {
            // given
            Cart cart = buildCart();

            // when + then
            assertThatThrownBy(() -> cartService.updateAmountCartItem(cart, PHONE_ID, null))
                    .isInstanceOf(NullPointerException.class);
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
            assertThat(result.getTotalPrice()).isEqualTo(BigDecimal.ZERO);
            Mockito.verify(cartItemRepository).findByCartIdAndPhoneId(CART_ID, PHONE_ID);
            Mockito.verify(cartItemRepository).delete(item);
            Mockito.verify(cartRepository).save(cart);
        }

        @Test
        void whenItemNotFound_thenThrowException() {
            // given
            Cart cart = buildCart();

            // mockito
            Mockito.when(cartItemRepository.findByCartIdAndPhoneId(CART_ID, PHONE_ID))
                    .thenReturn(Optional.empty());

            // when + then
            assertThatThrownBy(() -> cartService.removeItemFromCart(cart, PHONE_ID))
                    .isInstanceOf(ResourceNotFoundException.class);

            Mockito.verify(cartItemRepository).findByCartIdAndPhoneId(CART_ID, PHONE_ID);
            Mockito.verify(cartItemRepository, never()).delete(any());
            Mockito.verify(cartRepository, never()).save(any());
        }

        @Test
        void whenCartIsNull_thenThrowException() {
            // when + then
            assertThatThrownBy(() -> cartService.removeItemFromCart(null, PHONE_ID))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void whenPhoneIdIsNull_thenThrowException() {
            // given
            Cart cart = buildCart();

            // when + then
            assertThatThrownBy(() -> cartService.removeItemFromCart(cart, null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    class ClearCartTest {

        @Test
        void whenCartHasItems_thenClearSuccessfully() {
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
            Mockito.verify(cartRepository).save(cart);
        }

        @Test
        void whenCartIsEmpty_thenClearSuccessfully() {
            // given
            Cart cart = buildCart();

            // mockito
            Mockito.when(cartRepository.save(cart))
                    .thenReturn(cart);

            // when
            Cart result = cartService.clearCart(cart);

            // then
            assertThat(result.getCartItems()).isEmpty();
            assertThat(result.getTotalPrice()).isEqualTo(BigDecimal.ZERO);
            Mockito.verify(cartItemRepository).deleteAllByCartId(CART_ID);
            Mockito.verify(cartRepository).save(cart);
        }

        @Test
        void whenCartIsNull_thenThrowException() {
            // when + then
            assertThatThrownBy(() -> cartService.clearCart(null))
                    .isInstanceOf(NullPointerException.class);
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
            assertThat(result.get()).isEqualTo(buildCart());
            Mockito.verify(cartRepository).findByUserId(USER_ID);
        }

        @Test
        void whenCartNotExists_thenReturnEmptyOptional() {
            // mockito
            Mockito.when(cartRepository.findByUserId(USER_ID))
                    .thenReturn(Optional.empty());

            // when
            Optional<Cart> result = cartService.getCartByUserId(USER_ID);

            // then
            assertThat(result).isNotPresent();
            Mockito.verify(cartRepository).findByUserId(USER_ID);
        }

        @Test
        void whenUserIdIsNull_thenThrowException() {
            // when + then
            assertThatThrownBy(() -> cartService.getCartByUserId(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    static class TestResources {

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
            CartItem cartItem = buildCartItem(cart);
            cart.getCartItems().add(cartItem);
            cart.setTotalPrice(BigDecimal.valueOf(100));
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
    }
}