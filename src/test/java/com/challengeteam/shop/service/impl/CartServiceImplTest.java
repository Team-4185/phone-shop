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
import com.challengeteam.shop.utility.CartUtility;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
public class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private PhoneService phoneService;

    @InjectMocks
    private CartServiceImpl cartService;

    @Nested
    class GetCartByIdTest {

        @Test
        void whenCartExists_thenReturnOptionalWithCart() {
            // given
            Cart cart = TestData.buildCart();
            Cart expected = TestData.buildCart();

            // mockito
            Mockito.when(cartRepository.findById(TestData.CART_ID))
                    .thenReturn(Optional.of(cart));

            // when
            Optional<Cart> result = cartService.getCart(TestData.CART_ID);

            // then
            assertNotNull(result);
            assertTrue(result.isPresent());
            assertEquals(expected.getId(), result.get().getId());
            Mockito.verify(cartRepository).findById(TestData.CART_ID);
        }

        @Test
        void whenCartDoesNotExist_thenReturnEmptyOptional() {
            // given
            // ...

            // mockito
            Mockito.when(cartRepository.findById(TestData.CART_ID))
                    .thenReturn(Optional.empty());

            // when
            Optional<Cart> result = cartService.getCart(TestData.CART_ID);

            // then
            assertNotNull(result);
            assertFalse(result.isPresent());
            Mockito.verify(cartRepository).findById(TestData.CART_ID);
        }

        @Test
        void whenIdIsNull_thenThrowException() {
            // given
            Long id = null;

            // mockito
            // ...

            // when + then
            assertThrows(NullPointerException.class, () -> cartService.getCart(id));
        }
    }

    @Nested
    class PutItemToCartTest {

        @Test
        void whenItemNotInCart_thenAddSuccessfully() {
            // given
            Cart cart = TestData.buildCart();
            CartItemAddRequestDto dto = TestData.buildAddDto();
            Phone phone = TestData.buildPhone();

            // mockito
            Mockito.when(phoneService.getById(TestData.PHONE_ID))
                    .thenReturn(Optional.of(phone));
            Mockito.when(cartItemRepository.save(any(CartItem.class)))
                    .thenReturn(TestData.buildCartItem());
            Mockito.when(cartRepository.save(cart))
                    .thenReturn(cart);

            // when
            try (MockedStatic<CartUtility> util = mockStatic(CartUtility.class)) {
                util.when(() -> CartUtility.isCartHasPhone(cart, TestData.PHONE_ID))
                        .thenReturn(false);
                util.when(() -> CartUtility.countCartTotalPrice(cart))
                        .thenReturn(BigDecimal.valueOf(100));

                Cart result = cartService.putItemToCart(cart, dto);

                // then
                assertNotNull(result);
                assertEquals(BigDecimal.valueOf(100), result.getTotalPrice());
                Mockito.verify(cartItemRepository).save(any(CartItem.class));
                Mockito.verify(cartRepository).save(cart);
            }
        }

        @Test
        void whenPhoneAlreadyInCart_thenThrowException() {
            // given
            Cart cart = TestData.buildCart();
            CartItemAddRequestDto dto = TestData.buildAddDto();

            // mockito
            Mockito.when(phoneService.getById(TestData.PHONE_ID))
                    .thenReturn(Optional.of(TestData.buildPhone()));

            // when + then
            try (MockedStatic<CartUtility> util = mockStatic(CartUtility.class)) {
                util.when(() -> CartUtility.isCartHasPhone(cart, TestData.PHONE_ID))
                        .thenReturn(true);

                assertThrows(PhoneAlreadyInCartException.class,
                        () -> cartService.putItemToCart(cart, dto));
            }
        }

        @Test
        void whenPhoneNotFound_thenThrowException() {
            // given
            Cart cart = TestData.buildCart();
            CartItemAddRequestDto dto = TestData.buildAddDto();

            // mockito
            Mockito.when(phoneService.getById(TestData.PHONE_ID))
                    .thenReturn(Optional.empty());

            // when + then
            assertThrows(ResourceNotFoundException.class,
                    () -> cartService.putItemToCart(cart, dto));
        }
    }

    @Nested
    class UpdateAmountCartItemTest {

        @Test
        void whenCartItemExists_thenUpdateSuccessfully() {
            // given
            Cart cart = TestData.buildCart();
            CartItemUpdateRequestDto dto = TestData.buildUpdateDto();
            CartItem item = TestData.buildCartItem();

            // mockito
            Mockito.when(cartItemRepository.findByCartIdAndPhoneId(cart.getId(), TestData.PHONE_ID))
                    .thenReturn(Optional.of(item));
            Mockito.when(cartRepository.save(cart))
                    .thenReturn(cart);

            // when
            try (MockedStatic<CartUtility> util = mockStatic(CartUtility.class)) {
                util.when(() -> CartUtility.countCartTotalPrice(cart))
                        .thenReturn(BigDecimal.valueOf(300));

                Cart result = cartService.updateAmountCartItem(cart, dto);

                // then
                assertNotNull(result);
                assertEquals(BigDecimal.valueOf(300), result.getTotalPrice());
                Mockito.verify(cartItemRepository).save(item);
                Mockito.verify(cartRepository).save(cart);
            }
        }

        @Test
        void whenCartItemNotFound_thenThrowException() {
            // given
            Cart cart = TestData.buildCart();
            CartItemUpdateRequestDto dto = TestData.buildUpdateDto();

            // mockito
            Mockito.when(cartItemRepository.findByCartIdAndPhoneId(cart.getId(), TestData.PHONE_ID))
                    .thenReturn(Optional.empty());

            // when + then
            assertThrows(ResourceNotFoundException.class,
                    () -> cartService.updateAmountCartItem(cart, dto));
        }
    }

    @Nested
    class RemoveItemFromCartTest {

        @Test
        void whenItemExists_thenRemoveSuccessfully() {
            // given
            Cart cart = TestData.buildCart();
            CartItem item = TestData.buildCartItem();

            // mockito
            Mockito.when(cartItemRepository.findByCartIdAndPhoneId(cart.getId(), TestData.PHONE_ID))
                    .thenReturn(Optional.of(item));
            Mockito.when(cartRepository.save(cart))
                    .thenReturn(cart);

            // when
            try (MockedStatic<CartUtility> util = mockStatic(CartUtility.class)) {
                util.when(() -> CartUtility.countCartTotalPrice(cart))
                        .thenReturn(BigDecimal.ZERO);

                Cart result = cartService.removeItemFromCart(cart, TestData.PHONE_ID);

                // then
                assertNotNull(result);
                assertEquals(BigDecimal.ZERO, result.getTotalPrice());
                Mockito.verify(cartItemRepository).delete(item);
                Mockito.verify(cartRepository).save(cart);
            }
        }

        @Test
        void whenItemNotFound_thenThrowException() {
            // given
            Cart cart = TestData.buildCart();

            // mockito
            Mockito.when(cartItemRepository.findByCartIdAndPhoneId(cart.getId(), TestData.PHONE_ID))
                    .thenReturn(Optional.empty());

            // when + then
            assertThrows(ResourceNotFoundException.class,
                    () -> cartService.removeItemFromCart(cart, TestData.PHONE_ID));
        }
    }

    @Nested
    class ClearCartTest {

        @Test
        void whenCalled_thenDeleteAllItemsAndResetPrice() {
            // given
            Cart cart = TestData.buildCart();
            cart.getCartItems().add(TestData.buildCartItem());

            // mockito
            Mockito.when(cartRepository.save(cart))
                    .thenReturn(cart);

            // when
            Cart result = cartService.clearCart(cart);

            // then
            assertNotNull(result);
            assertTrue(result.getCartItems().isEmpty());
            assertEquals(BigDecimal.ZERO, result.getTotalPrice());
            Mockito.verify(cartItemRepository).deleteAllByCartId(cart.getId());
            Mockito.verify(cartRepository).save(cart);
        }
    }

    @Nested
    class GetCartByUserIdTest {

        @Test
        void whenCartExists_thenReturnOptionalWithCart() {
            // given
            Cart cart = TestData.buildCart();

            // mockito
            Mockito.when(cartRepository.findByUserId(TestData.USER_ID))
                    .thenReturn(Optional.of(cart));

            // when
            Optional<Cart> result = cartService.getCartByUserId(TestData.USER_ID);

            // then
            assertNotNull(result);
            assertTrue(result.isPresent());
            assertEquals(cart, result.get());
            Mockito.verify(cartRepository).findByUserId(TestData.USER_ID);
        }

    }

    static class TestData {
        static final Long CART_ID = 1L;
        static final Long PHONE_ID = 2L;
        static final Long USER_ID = 3L;

        static Cart buildCart() {
            Cart c = new Cart();
            c.setId(CART_ID);
            c.setCartItems(new ArrayList<>());
            c.setTotalPrice(BigDecimal.ZERO);
            return c;
        }

        static Phone buildPhone() {
            Phone p = new Phone();
            p.setId(PHONE_ID);
            p.setPrice(BigDecimal.valueOf(100));
            return p;
        }

        static CartItem buildCartItem() {
            CartItem item = new CartItem();
            item.setCart(buildCart());
            item.setPhone(buildPhone());
            item.setAmount(1);
            return item;
        }

        static CartItemAddRequestDto buildAddDto() {
            return new CartItemAddRequestDto(PHONE_ID, 1);
        }

        static CartItemUpdateRequestDto buildUpdateDto() {
            return new CartItemUpdateRequestDto(PHONE_ID, 3);
        }
    }
}