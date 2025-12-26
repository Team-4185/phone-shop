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
    class GetCartByIdTest {

        @Test
        void whenCartExists_thenReturnOptionalWithCart() {
            // mockito
            Mockito.when(cartRepository.findById(CART_ID))
                    .thenReturn(Optional.of(buildCart()));

            // when
            Optional<Cart> result = cartService.getCart(CART_ID);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(CART_ID);
            Mockito.verify(cartRepository).findById(CART_ID);
        }

        @Test
        void whenCartDoesNotExist_thenReturnEmptyOptional() {
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
        void whenIdIsNull_thenThrowNullPointerException() {
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
            Phone phone = buildPhone();
            CartItem cartItem = buildCartItem();

            // mockito
            Mockito.when(phoneService.getById(PHONE_ID))
                    .thenReturn(Optional.of(phone));
            Mockito.when(cartItemRepository.save(any(CartItem.class)))
                    .thenReturn(cartItem);
            Mockito.when(cartRepository.save(cart))
                    .thenReturn(cart);

            // when
            Cart result = cartService.putItemToCart(cart, dto);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(CART_ID);
            Mockito.verify(phoneService).getById(PHONE_ID);
            Mockito.verify(cartItemRepository).save(any(CartItem.class));
            Mockito.verify(cartRepository).save(cart);
        }

        @Test
        void whenPhoneAlreadyInCart_thenThrowPhoneAlreadyInCartException() {
            // given
            Cart cart = buildCartWithItem();
            CartItemAddRequestDto dto = buildCartItemAddRequestDto();

            // mockito
            Mockito.when(phoneService.getById(PHONE_ID))
                    .thenReturn(Optional.of(buildPhone()));

            // when + then
            assertThatThrownBy(() -> cartService.putItemToCart(cart, dto))
                    .isInstanceOf(PhoneAlreadyInCartException.class);
        }

        @Test
        void whenPhoneNotFound_thenThrowResourceNotFoundException() {
            // given
            Cart cart = buildCart();
            CartItemAddRequestDto dto = buildCartItemAddRequestDto();

            // mockito
            Mockito.when(phoneService.getById(PHONE_ID))
                    .thenReturn(Optional.empty());

            // when + then
            assertThatThrownBy(() -> cartService.putItemToCart(cart, dto))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        void whenDtoIsNull_thenThrowNullPointerException() {
            // given
            Cart cart = buildCart();

            // when + then
            assertThatThrownBy(() -> cartService.putItemToCart(cart, null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void whenCartIsNull_thenThrowNullPointerException() {
            // given
            CartItemAddRequestDto dto = buildCartItemAddRequestDto();

            // when + then
            assertThatThrownBy(() -> cartService.putItemToCart(null, dto))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void whenAmountIsInvalid_thenThrowExceptionFromValidator() {
            // given
            Cart cart = buildCart();
            CartItemAddRequestDto dto = buildCartItemAddRequestDtoWithAmount(INVALID_AMOUNT);

            // mockito
            Mockito.doThrow(IllegalArgumentException.class)
                    .when(cartValidator)
                    .validateCartItemAmountToUpdate(dto.amount());

            // when + then
            assertThatThrownBy(() -> cartService.putItemToCart(cart, dto))
                    .isInstanceOf(IllegalArgumentException.class);

            Mockito.verify(cartValidator).validateCartItemAmountToUpdate(dto.amount());
            Mockito.verifyNoInteractions(phoneService);
            Mockito.verifyNoInteractions(cartItemRepository);
        }

    }

    @Nested
    class UpdateAmountCartItemTest {

        @Test
        void whenCartItemExists_thenUpdateSuccessfully() {
            // given
            Cart cart = buildCart();
            Integer newAmount = 5;
            CartItem item = buildCartItem();

            // mockito
            Mockito.when(cartItemRepository.findByCartIdAndPhoneId(CART_ID, PHONE_ID))
                    .thenReturn(Optional.of(item));
            Mockito.when(cartItemRepository.save(item))
                    .thenReturn(item);
            Mockito.when(cartRepository.save(cart))
                    .thenReturn(cart);

            // when
            Cart result = cartService.updateAmountCartItem(cart, PHONE_ID, newAmount);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(CART_ID);
            Mockito.verify(cartItemRepository).findByCartIdAndPhoneId(CART_ID, PHONE_ID);
            Mockito.verify(cartItemRepository).save(item);
            Mockito.verify(cartRepository).save(cart);
        }

        @Test
        void whenCartItemNotFound_thenThrowResourceNotFoundException() {
            // given
            Cart cart = buildCart();

            // mockito
            Mockito.when(cartItemRepository.findByCartIdAndPhoneId(CART_ID, PHONE_ID))
                    .thenReturn(Optional.empty());

            // when + then
            assertThatThrownBy(() -> cartService.updateAmountCartItem(cart, PHONE_ID, 5))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        void whenCartIsNull_thenThrowNullPointerException() {
            // when + then
            assertThatThrownBy(() -> cartService.updateAmountCartItem(null, PHONE_ID, 5))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void whenPhoneIdIsNull_thenThrowNullPointerException() {
            // given
            Cart cart = buildCart();

            // when + then
            assertThatThrownBy(() -> cartService.updateAmountCartItem(cart, null, 5))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void whenAmountIsNull_thenThrowNullPointerException() {
            // given
            Cart cart = buildCart();

            // when + then
            assertThatThrownBy(() -> cartService.updateAmountCartItem(cart, PHONE_ID, null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void whenAmountIsInvalid_thenThrowExceptionFromValidator() {
            // given
            Cart cart = buildCart();

            // mockito
            Mockito.doThrow(IllegalArgumentException.class)
                    .when(cartValidator)
                    .validateCartItemAmountToUpdate(INVALID_AMOUNT);

            // when + then
            assertThatThrownBy(() -> cartService.updateAmountCartItem(cart, PHONE_ID, INVALID_AMOUNT))
                    .isInstanceOf(IllegalArgumentException.class);

            Mockito.verify(cartValidator).validateCartItemAmountToUpdate(INVALID_AMOUNT);
            Mockito.verifyNoInteractions(cartItemRepository);
            Mockito.verifyNoInteractions(cartRepository);
        }

    }

    @Nested
    class RemoveItemFromCartTest {

        @Test
        void whenItemExists_thenRemoveSuccessfully() {
            // given
            Cart cart = buildCartWithItem();
            CartItem item = buildCartItem();

            // mockito
            Mockito.when(cartItemRepository.findByCartIdAndPhoneId(CART_ID, PHONE_ID))
                    .thenReturn(Optional.of(item));
            Mockito.when(cartRepository.save(cart))
                    .thenReturn(cart);

            // when
            Cart result = cartService.removeItemFromCart(cart, PHONE_ID);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(CART_ID);
            Mockito.verify(cartItemRepository).findByCartIdAndPhoneId(CART_ID, PHONE_ID);
            Mockito.verify(cartItemRepository).delete(item);
            Mockito.verify(cartRepository).save(cart);
        }

        @Test
        void whenItemNotFound_thenThrowResourceNotFoundException() {
            // given
            Cart cart = buildCart();

            // mockito
            Mockito.when(cartItemRepository.findByCartIdAndPhoneId(CART_ID, PHONE_ID))
                    .thenReturn(Optional.empty());

            // when + then
            assertThatThrownBy(() -> cartService.removeItemFromCart(cart, PHONE_ID))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        void whenCartIsNull_thenThrowNullPointerException() {
            // when + then
            assertThatThrownBy(() -> cartService.removeItemFromCart(null, PHONE_ID))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void whenPhoneIdIsNull_thenThrowNullPointerException() {
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
        void whenCalled_thenDeleteAllItemsAndResetPrice() {
            // given
            Cart cart = buildCartWithItem();

            // mockito
            Mockito.when(cartRepository.save(cart))
                    .thenReturn(cart);

            // when
            Cart result = cartService.clearCart(cart);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getCartItems()).isEmpty();
            assertThat(result.getTotalPrice()).isEqualTo(BigDecimal.ZERO);
            Mockito.verify(cartItemRepository).deleteAllByCartId(CART_ID);
            Mockito.verify(cartRepository).save(cart);
        }

        @Test
        void whenCartIsNull_thenThrowNullPointerException() {
            // when + then
            assertThatThrownBy(() -> cartService.clearCart(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    class GetCartByUserIdTest {

        @Test
        void whenCartExists_thenReturnOptionalWithCart() {
            // mockito
            Mockito.when(cartRepository.findByUserId(USER_ID))
                    .thenReturn(Optional.of(buildCart()));

            // when
            Optional<Cart> result = cartService.getCartByUserId(USER_ID);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(CART_ID);
            Mockito.verify(cartRepository).findByUserId(USER_ID);
        }

        @Test
        void whenCartDoesNotExist_thenReturnEmptyOptional() {
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
        void whenUserIdIsNull_thenThrowNullPointerException() {
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
            cart.getCartItems().add(buildCartItem());
            return cart;
        }

        static Phone buildPhone() {
            return Phone.builder()
                    .id(PHONE_ID)
                    .price(BigDecimal.valueOf(100))
                    .build();
        }

        static CartItem buildCartItem() {
            return CartItem.builder()
                    .id(1L)
                    .cart(buildCart())
                    .phone(buildPhone())
                    .amount(1)
                    .build();
        }

        static CartItemAddRequestDto buildCartItemAddRequestDto() {
            return new CartItemAddRequestDto(PHONE_ID, 1);
        }

        static CartItemAddRequestDto buildCartItemAddRequestDtoWithAmount(Integer amount) {
            return new CartItemAddRequestDto(PHONE_ID, amount);
        }

    }
}