package com.challengeteam.shop.service.impl;

import com.challengeteam.shop.dto.cart.CartItemAddRequestDto;
import com.challengeteam.shop.dto.cart.CartItemUpdateRequestDto;
import com.challengeteam.shop.entity.cart.Cart;
import com.challengeteam.shop.entity.cart.CartItem;
import com.challengeteam.shop.entity.phone.Phone;
import com.challengeteam.shop.exceptionHandling.exception.PhoneAlreadyInCartException;
import com.challengeteam.shop.exceptionHandling.exception.ResourceNotFoundException;
import com.challengeteam.shop.persistence.repository.CartItemRepository;
import com.challengeteam.shop.persistence.repository.CartRepository;
import com.challengeteam.shop.service.PhoneService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock private CartRepository cartRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private PhoneService phoneService;
    @InjectMocks private CartServiceImpl cartService;

    @Nested
    class GetCartTest {

        @Test
        void whenCartExists_thenReturnOptionalWithCart() {
            // mockito
            when(cartRepository.findById(TestResources.CART_ID))
                    .thenReturn(Optional.of(TestResources.buildCart()));

            // when
            Optional<Cart> result = cartService.getCart(TestResources.CART_ID);

            // then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(TestResources.buildCart());
        }

        @Test
        void whenCartDoesNotExist_thenReturnEmptyOptional() {
            // mockito
            when(cartRepository.findById(TestResources.CART_ID))
                    .thenReturn(Optional.empty());

            // when
            Optional<Cart> result = cartService.getCart(TestResources.CART_ID);

            // then
            assertThat(result).isNotPresent();
        }

        @Test
        void whenIdIsNull_thenThrowException() {
            // when + then
            assertThatThrownBy(() -> cartService.getCart(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    class AddItemToCartTest {

        @Test
        void whenItemNotInCart_thenAddSuccessfully() {
            // mockito
            when(cartItemRepository.existsByCartIdAndPhoneId(TestResources.CART_ID, TestResources.PHONE_ID))
                    .thenReturn(false);
            when(cartRepository.findById(TestResources.CART_ID))
                    .thenReturn(Optional.of(TestResources.buildCart()));
            when(phoneService.getById(TestResources.PHONE_ID))
                    .thenReturn(Optional.of(TestResources.buildPhone()));
            when(cartItemRepository.save(any(CartItem.class)))
                    .thenReturn(TestResources.buildCartItem());

            // when
            cartService.addItemToCart(TestResources.CART_ID, TestResources.buildCartItemAddRequestDto());

            // then
            verify(cartItemRepository).save(any(CartItem.class));
            verify(cartRepository).save(any(Cart.class));
        }

        @Test
        void whenItemAlreadyInCart_thenThrowException() {
            // mockito
            when(cartItemRepository.existsByCartIdAndPhoneId(TestResources.CART_ID, TestResources.PHONE_ID))
                    .thenReturn(true);

            // when + then
            assertThatThrownBy(() -> cartService.addItemToCart(TestResources.CART_ID, TestResources.buildCartItemAddRequestDto()))
                    .isInstanceOf(PhoneAlreadyInCartException.class);
        }
    }

    @Nested
    class UpdateAmountCartItemTest {

        @Test
        void whenCartItemExists_thenUpdateAmount() {
            // mockito
            when(cartRepository.findById(TestResources.CART_ID))
                    .thenReturn(Optional.of(TestResources.buildCart()));
            when(cartItemRepository.findByCartIdAndPhoneId(TestResources.CART_ID, TestResources.PHONE_ID))
                    .thenReturn(Optional.of(TestResources.buildCartItem()));

            // when
            cartService.updateAmountCartItem(TestResources.CART_ID, TestResources.buildCartItemUpdateRequestDto());

            // then
            verify(cartItemRepository).save(any(CartItem.class));
            verify(cartRepository).save(any(Cart.class));
        }

        @Test
        void whenCartItemDoesNotExist_thenThrowException() {
            // mockito
            when(cartRepository.findById(TestResources.CART_ID))
                    .thenReturn(Optional.of(TestResources.buildCart()));
            when(cartItemRepository.findByCartIdAndPhoneId(TestResources.CART_ID, TestResources.PHONE_ID))
                    .thenReturn(Optional.empty());

            // when + then
            assertThatThrownBy(() -> cartService.updateAmountCartItem(TestResources.CART_ID, TestResources.buildCartItemUpdateRequestDto()))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    class RemoveItemFromCartTest {

        @Test
        void whenCartItemExists_thenRemoveSuccessfully() {
            // mockito
            when(cartRepository.findById(TestResources.CART_ID))
                    .thenReturn(Optional.of(TestResources.buildCart()));
            when(cartItemRepository.findByCartIdAndPhoneId(TestResources.CART_ID, TestResources.PHONE_ID))
                    .thenReturn(Optional.of(TestResources.buildCartItem()));

            // when
            cartService.removeItemFromCart(TestResources.CART_ID, TestResources.PHONE_ID);

            // then
            verify(cartItemRepository).delete(any(CartItem.class));
            verify(cartRepository).save(any(Cart.class));
        }

        @Test
        void whenCartItemDoesNotExist_thenThrowException() {
            // mockito
            when(cartRepository.findById(TestResources.CART_ID))
                    .thenReturn(Optional.of(TestResources.buildCart()));
            when(cartItemRepository.findByCartIdAndPhoneId(TestResources.CART_ID, TestResources.PHONE_ID))
                    .thenReturn(Optional.empty());

            // when + then
            assertThatThrownBy(() -> cartService.removeItemFromCart(TestResources.CART_ID, TestResources.PHONE_ID))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    class ClearCartTest {

        @Test
        void whenCartExists_thenClearSuccessfully() {
            // mockito
            when(cartRepository.findById(TestResources.CART_ID))
                    .thenReturn(Optional.of(TestResources.buildCart()));

            // when
            cartService.clearCart(TestResources.CART_ID);

            // then
            verify(cartItemRepository).deleteAllByCartId(TestResources.CART_ID);
            verify(cartRepository).save(any(Cart.class));
        }
    }

    @Nested
    class GetCartIdByUserIdTest {

        @Test
        void whenCartExists_thenReturnCartId() {
            // mockito
            when(cartRepository.findByUserId(TestResources.USER_ID))
                    .thenReturn(Optional.of(TestResources.buildCart()));

            // when
            Long result = cartService.getCartIdByUserId(TestResources.USER_ID);

            // then
            assertThat(result).isEqualTo(TestResources.CART_ID);
        }

        @Test
        void whenCartDoesNotExist_thenThrowException() {
            // mockito
            when(cartRepository.findByUserId(TestResources.USER_ID))
                    .thenReturn(Optional.empty());

            // when + then
            assertThatThrownBy(() -> cartService.getCartIdByUserId(TestResources.USER_ID))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }


    static class TestResources {
        static final Long CART_ID = 1L;
        static final Long PHONE_ID = 2L;
        static final Long USER_ID = 3L;
        static final BigDecimal PRICE = BigDecimal.valueOf(100);

        static Cart buildCart() {
            Cart cart = new Cart();
            cart.setId(CART_ID);
            cart.setTotalPrice(BigDecimal.ZERO);
            return cart;
        }

        static Phone buildPhone() {
            Phone phone = new Phone();
            phone.setId(PHONE_ID);
            phone.setPrice(PRICE);
            return phone;
        }

        static CartItem buildCartItem() {
            CartItem item = new CartItem();
            item.setCart(buildCart());
            item.setPhone(buildPhone());
            item.setAmount(1);
            return item;
        }

        static CartItemAddRequestDto buildCartItemAddRequestDto() {
            return new CartItemAddRequestDto(PHONE_ID, 1);
        }

        static CartItemUpdateRequestDto buildCartItemUpdateRequestDto() {
            return new CartItemUpdateRequestDto(PHONE_ID, 5);
        }

    }
}
