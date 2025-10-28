//package com.challengeteam.shop.service.impl;
//
//import com.challengeteam.shop.dto.cart.CartItemAddRequestDto;
//import com.challengeteam.shop.dto.cart.CartItemUpdateRequestDto;
//import com.challengeteam.shop.entity.cart.Cart;
//import com.challengeteam.shop.entity.cart.CartItem;
//import com.challengeteam.shop.entity.phone.Phone;
//import com.challengeteam.shop.entity.user.User;
//import com.challengeteam.shop.exceptionHandling.exception.ResourceNotFoundException;
//import com.challengeteam.shop.repository.CartRepository;
//import com.challengeteam.shop.service.PhoneService;
//import com.challengeteam.shop.service.UserService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mock;
//import org.mockito.Mockito;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.math.BigDecimal;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//
//@ExtendWith(MockitoExtension.class)
//public class CartServiceImplTest {
//
//    @Mock
//    private CartRepository cartRepository;
//
//    @Mock
//    private UserService userService;
//
//    @Mock
//    private PhoneService phoneService;
//
//    private CartServiceImpl cartService;
//
//    @BeforeEach
//    void setUp() {
//        cartService = new CartServiceImpl(cartRepository, userService, phoneService);
//    }
//
//    @Nested
//    class GetByUsernameTest {
//
//        @Test
//        void whenUserAndCartExist_thenReturnCart() {
//            // given
//            User user = buildUser();
//            Cart cart = buildCart(user);
//
//            // mockito
//            Mockito.when(userService.getByEmail("test@example.com")).thenReturn(Optional.of(user));
//            Mockito.when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.of(cart));
//
//            // when
//            Cart result = cartService.getByUsername("test@example.com").orElse(null);
//
//            // then
//            assertNotNull(result);
//            assertEquals(cart, result);
//        }
//
//        @Test
//        void whenUserDoesNotExist_thenThrowException() {
//            //given
//            // ...
//
//            // mockito
//            Mockito.when(userService.getByEmail("test@example.com")).thenReturn(Optional.empty());
//
//            // when + then
//            assertThrows(ResourceNotFoundException.class,
//                    () -> cartService.getByUsername("test@example.com"));
//        }
//
//        @Test
//        void whenCartDoesNotExist_thenThrowException() {
//            // given
//            User user = buildUser();
//
//            // mockito
//            Mockito.when(userService.getByEmail("test@example.com")).thenReturn(Optional.of(user));
//            Mockito.when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.empty());
//
//            // when + then
//            assertThrows(ResourceNotFoundException.class,
//                    () -> cartService.getByUsername("test@example.com"));
//        }
//    }
//
//    @Nested
//    class AddItemToCartTest {
//
//        @Test
//        void whenPhoneNotInCart_thenAddNewItemAndUpdateTotalPrice() {
//            // given
//            User user = buildUser();
//            Cart cart = buildCart(user);
//            Phone phone = buildPhone(BigDecimal.valueOf(500));
//            CartItemAddRequestDto dto = new CartItemAddRequestDto(10L, 2);
//
//            Mockito.when(userService.getByEmail("test@example.com")).thenReturn(Optional.of(user));
//            Mockito.when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.of(cart));
//            Mockito.when(phoneService.getById(10L)).thenReturn(Optional.of(phone));
//            Mockito.when(cartRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
//
//            // when
//            cartService.addItemToCart("test@example.com", dto);
//
//            // then
//            assertEquals(1, cart.getCartItems().size());
//            CartItem item = cart.getCartItems().get(0);
//            assertEquals(2, item.getAmount());
//            assertEquals(BigDecimal.valueOf(1000), cart.getTotalPrice());
//        }
//
//        @Test
//        void whenPhoneAlreadyInCart_thenIncreaseAmountAndUpdateTotalPrice() {
//            // given
//            User user = buildUser();
//            Cart cart = buildCart(user);
//            Phone phone = buildPhone(BigDecimal.valueOf(500));
//
//            CartItem existingItem = new CartItem();
//            existingItem.setPhone(phone);
//            existingItem.setAmount(3);
//            cart.getCartItems().add(existingItem);
//
//            CartItemAddRequestDto dto = new CartItemAddRequestDto(10L, 2);
//
//            Mockito.when(userService.getByEmail("test@example.com")).thenReturn(Optional.of(user));
//            Mockito.when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.of(cart));
//            Mockito.when(phoneService.getById(10L)).thenReturn(Optional.of(phone));
//            Mockito.when(cartRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
//
//            // when
//            cartService.addItemToCart("test@example.com", dto);
//
//            // then
//            assertEquals(1, cart.getCartItems().size());
//            assertEquals(5, existingItem.getAmount()); // 3 + 2
//            assertEquals(BigDecimal.valueOf(2500), cart.getTotalPrice());
//        }
//
//        @Test
//        void whenPhoneDoesNotExist_thenThrowException() {
//            // given
//            User user = buildUser();
//            Cart cart = buildCart(user);
//            CartItemAddRequestDto dto = new CartItemAddRequestDto(10L, 2);
//
//            Mockito.when(userService.getByEmail("test@example.com")).thenReturn(Optional.of(user));
//            Mockito.when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.of(cart));
//            Mockito.when(phoneService.getById(10L)).thenReturn(Optional.empty());
//
//            // when + then
//            assertThrows(ResourceNotFoundException.class,
//                    () -> cartService.addItemToCart("test@example.com", dto));
//        }
//    }
//
//
//    @Nested
//    class UpdateItemTest {
//
//        @Test
//        void whenItemExists_thenUpdateAmountAndTotalPrice() {
//            // given
//            User user = buildUser();
//            Cart cart = buildCart(user);
//            Phone phone = buildPhone(BigDecimal.valueOf(100));
//            CartItem item = new CartItem();
//            item.setPhone(phone);
//            item.setAmount(1);
//            cart.getCartItems().add(item);
//
//            CartItemUpdateRequestDto dto = new CartItemUpdateRequestDto(10L, 3);
//
//            // mockito
//            Mockito.when(userService.getByEmail("test@example.com")).thenReturn(Optional.of(user));
//            Mockito.when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.of(cart));
//            Mockito.when(cartRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
//
//            // when
//            cartService.updateItem("test@example.com", dto);
//
//            // then
//            assertEquals(3, item.getAmount());
//            assertEquals(BigDecimal.valueOf(300), cart.getTotalPrice());
//        }
//
//        @Test
//        void whenItemDoesNotExist_thenThrowException() {
//            // given
//            User user = buildUser();
//            Cart cart = buildCart(user);
//            CartItemUpdateRequestDto dto = new CartItemUpdateRequestDto(10L, 3);
//
//            // mockito
//            Mockito.when(userService.getByEmail("test@example.com")).thenReturn(Optional.of(user));
//            Mockito.when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.of(cart));
//
//            // when + then
//            assertThrows(ResourceNotFoundException.class,
//                    () -> cartService.updateItem("test@example.com", dto));
//        }
//    }
//
//    @Nested
//    class RemoveItemTest {
//
//        @Test
//        void whenItemExists_thenRemoveItemAndUpdateTotalPrice() {
//            // given
//            User user = buildUser();
//            Cart cart = buildCart(user);
//            Phone phone = buildPhone(BigDecimal.valueOf(200));
//            CartItem item = new CartItem();
//            item.setPhone(phone);
//            item.setAmount(2);
//            cart.getCartItems().add(item);
//
//            // mockito
//            Mockito.when(userService.getByEmail("test@example.com")).thenReturn(Optional.of(user));
//            Mockito.when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.of(cart));
//            Mockito.when(cartRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
//
//            // when
//            cartService.removeItem("test@example.com", 10L);
//
//            // then
//            assertTrue(cart.getCartItems().isEmpty());
//            assertEquals(BigDecimal.ZERO, cart.getTotalPrice());
//        }
//
//        @Test
//        void whenItemDoesNotExist_thenThrowException() {
//            // given
//            User user = buildUser();
//            Cart cart = buildCart(user);
//
//            // mockito
//            Mockito.when(userService.getByEmail("test@example.com")).thenReturn(Optional.of(user));
//            Mockito.when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.of(cart));
//
//            // when + then
//            assertThrows(ResourceNotFoundException.class,
//                    () -> cartService.removeItem("test@example.com", 10L));
//        }
//    }
//
//    @Nested
//    class ClearCartTest {
//
//        @Test
//        void whenCalled_thenClearItemsAndSetTotalPriceZero() {
//            // given
//            User user = buildUser();
//            Cart cart = buildCart(user);
//            cart.getCartItems().add(new CartItem());
//            cart.setTotalPrice(BigDecimal.valueOf(500));
//
//            // mockito
//            Mockito.when(userService.getByEmail("test@example.com")).thenReturn(Optional.of(user));
//            Mockito.when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.of(cart));
//            Mockito.when(cartRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
//
//            // when
//            cartService.clearCart("test@example.com");
//
//            // then
//            assertTrue(cart.getCartItems().isEmpty());
//            assertEquals(BigDecimal.ZERO, cart.getTotalPrice());
//        }
//    }
//
//    private User buildUser() {
//        User user = new User();
//        user.setId(1L);
//        user.setEmail("test@example.com");
//        return user;
//    }
//
//    private Cart buildCart(User user) {
//        Cart cart = new Cart();
//        cart.setId(1L);
//        cart.setUser(user);
//        cart.setCartItems(new java.util.ArrayList<>());
//        cart.setTotalPrice(BigDecimal.ZERO);
//        return cart;
//    }
//
//    private Phone buildPhone(BigDecimal price) {
//        Phone phone = new Phone();
//        phone.setId(10L);
//        phone.setPrice(price);
//        return phone;
//    }
//}
