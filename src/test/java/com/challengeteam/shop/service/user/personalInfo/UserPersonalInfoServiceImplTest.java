package com.challengeteam.shop.service.user.personalInfo;

import com.challengeteam.shop.dto.user.response.UserPersonalInfoResponseDto;
import com.challengeteam.shop.entity.cart.Cart;
import com.challengeteam.shop.entity.cart.CartItem;
import com.challengeteam.shop.entity.favorite.Favorite;
import com.challengeteam.shop.entity.order.DeliveryMethod;
import com.challengeteam.shop.entity.order.Order;
import com.challengeteam.shop.entity.order.OrderItem;
import com.challengeteam.shop.entity.order.OrderStatus;
import com.challengeteam.shop.entity.order.payment.PaymentDetails;
import com.challengeteam.shop.entity.order.payment.PaymentStatus;
import com.challengeteam.shop.entity.phone.Phone;
import com.challengeteam.shop.entity.phone.PhoneColor;
import com.challengeteam.shop.entity.phone.StorageCapacity;
import com.challengeteam.shop.entity.user.Role;
import com.challengeteam.shop.entity.user.User;
import com.challengeteam.shop.exceptionHandling.exception.user.InvalidUserCredentialsException;
import com.challengeteam.shop.exceptionHandling.exception.user.UsernameMissingException;
import com.challengeteam.shop.mapper.user.UserPersonalInfoMapper;
import com.challengeteam.shop.persistence.repository.FavoriteRepository;
import com.challengeteam.shop.persistence.repository.OrderRepository;
import com.challengeteam.shop.persistence.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserPersonalInfoServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private FavoriteRepository favoriteRepository;
    @Mock
    private UserPersonalInfoMapper userPersonalInfoMapper;
    @Mock
    private OrderRepository orderRepository;
    @InjectMocks
    private UserPersonalInfoServiceImpl userPersonalInfoService;
    private Role role;
    private String username;
    private Phone phone1;
    private Phone phone2;

    @BeforeEach
    void setUp() {
        username = "username@email.com";
        role = new Role("ROLE_USER", null);
        phone1 = Phone.builder()
                .id(1L)
                .name("iPhone 15")
                .description("Latest Apple smartphone")
                .brand("Apple")
                .price(BigDecimal.TEN)
                .build();
        phone2 = Phone.builder()
                .id(2L)
                .name("A2")
                .description("Samsung smartphone")
                .brand("Samsung")
                .price(BigDecimal.TEN)
                .build();
        UserPersonalInfoResponseDto dto = mock(UserPersonalInfoResponseDto.class);
        lenient().when(userPersonalInfoMapper.toDto(any(), anyList(), anyList())).thenReturn(dto);
    }

    /**
     * cases:
     * if the username is null or empty -> throw InvalidUserCredentialsException exception;
     * if the username was not found -> throw UserByUsernameWasNotFoundException exception;
     */
    @Nested
    class CheckInvalidDataTest {

        @ParameterizedTest
        @NullAndEmptySource
        void shouldThrowException_whenUsernameIsNullOrEmpty(String username) {

            UsernameMissingException exception = assertThrows(UsernameMissingException.class,
                    () -> userPersonalInfoService.getUserPersonalInfo(username));

            assertNotNull(exception);
            assertNotNull(exception.getMessage());
            verifyNoInteractions(userRepository, favoriteRepository, userPersonalInfoMapper);
        }

        @Test
        void shouldThrowException_whenUserWasNotFoundByUsername() {
            when(userRepository.findByEmail(username)).thenReturn(Optional.empty());

            InvalidUserCredentialsException exception = assertThrows(InvalidUserCredentialsException.class, () ->
                    userPersonalInfoService.getUserPersonalInfo(username));

            assertNotNull(exception);
            assertNotNull(exception.getMessage());
            verify(userRepository).findByEmail(username);
            verifyNoInteractions(favoriteRepository, userPersonalInfoMapper);
        }
    }

    /**
     * cases:
     * if the username is valid -> return user personal info;
     */
    @Nested
    class GetCorrectUserPersonalInfoTest {

        @Test
        void shouldReturnCorrectUserPersonalInfo() {
            User user = userBuilderHelper();
            List<Favorite> favorites = favoriteBuilderHelper(user);
            List<Order> orders = orderBuilderHelper(user);
            Cart cart = cartBuilderHelper(user);
            user.setCart(cart);
            when(userRepository.findByEmail(username)).thenReturn(Optional.of(user));
            when(favoriteRepository.findAllByUserIdWithPhoneImages(user.getId())).thenReturn(favorites);
            when(orderRepository.findAllByUserId(user.getId())).thenReturn(orders);


            UserPersonalInfoResponseDto userPersonalInfo = userPersonalInfoService.getUserPersonalInfo(username);

            verify(userRepository).findByEmail(username);
            verify(favoriteRepository).findAllByUserIdWithPhoneImages(user.getId());
            verify(orderRepository).findAllByUserId(user.getId());
            verify(userPersonalInfoMapper).toDto(user, favorites, orders);
            assertNotNull(userPersonalInfo);
        }

        @Test
        void shouldReturnCorrectUserPersonalInfoWithCart() {
            User user = userBuilderHelper();
            Cart cart = cartBuilderHelper(user);
            user.setCart(cart);
            when(userRepository.findByEmail(username)).thenReturn(Optional.of(user));

            UserPersonalInfoResponseDto userPersonalInfo = userPersonalInfoService.getUserPersonalInfo(username);

            verify(orderRepository).findAllByUserId(user.getId());
            verify(userPersonalInfoMapper).toDto(eq(user), anyList(), anyList());
        }

        @Test
        void shouldReturnCorrectUserPersonalInfoWithOrders() {
            User user = userBuilderHelper();
            List<Order> orders = orderBuilderHelper(user);
            when(orderRepository.findAllByUserId(user.getId())).thenReturn(orders);
            when(userRepository.findByEmail(username)).thenReturn(Optional.of(user));

            UserPersonalInfoResponseDto userPersonalInfoResponseDto = userPersonalInfoService.getUserPersonalInfo(username);

            verify(orderRepository).findAllByUserId(user.getId());
            verify(userPersonalInfoMapper).toDto(eq(user), anyList(), eq(orders));
        }

        @Test
        void shouldReturnCorrectUserPersonalInfoWithFavorites() {
            User user = userBuilderHelper();
            List<Favorite> favorites = favoriteBuilderHelper(user);
            when(userRepository.findByEmail(username)).thenReturn(Optional.of(user));
            when(favoriteRepository.findAllByUserIdWithPhoneImages(user.getId())).thenReturn(favorites);

            UserPersonalInfoResponseDto userPersonalInfoResponseDto = userPersonalInfoService.getUserPersonalInfo(username);

            verify(favoriteRepository).findAllByUserIdWithPhoneImages(user.getId());
            verify(userPersonalInfoMapper).toDto(eq(user), eq(favorites), anyList());
        }


    }

    private User userBuilderHelper() {
        return User.builder()
                .id(1L)
                .email(username)
                .firstName("John")
                .lastName("Doe")
                .phoneNumber("+380991234567")
                .role(role)
                .build();
    }

    private Cart cartBuilderHelper(User user) {
        return Cart.builder()
                .id(1L)
                .cartItems(List.of(
                                CartItem.builder()
                                        .id(1L)
                                        .phone(phone1)
                                        .amount(1)
                                        .build(),
                                CartItem.builder()
                                        .id(5L)
                                        .phone(phone2)
                                        .amount(3)
                                        .build()
                        )
                )
                .totalPrice(BigDecimal.TEN.multiply(BigDecimal.valueOf(4)))
                .user(user)
                .build();
    }

    private List<Order> orderBuilderHelper(User user) {
        return List.of(
                Order.builder()
                        .id(1L)
                        .paymentDetails(new PaymentDetails(PaymentStatus.PAID, "UUID-23xq"))
                        .status(OrderStatus.DELIVERED)
                        .deliveryMethod(DeliveryMethod.PICKUP)
                        .customerPhoneNumber("+380991234567")
                        .customerEmail(username)
                        .customerFirstName("John")
                        .customerLastName("Doe")
                        .items(List.of(OrderItem.builder()
                                .id(1L)
                                .phone(phone1)
                                .selectedColor(PhoneColor.GOLD)
                                .selectedStorage(StorageCapacity.CAPACITY_512GB)
                                .totalPrice(BigDecimal.TEN)
                                .build()))
                        .user(user)
                        .build(),
                Order.builder()
                        .id(1L)
                        .paymentDetails(new PaymentDetails(PaymentStatus.PAID, "UUID-2323xq"))
                        .status(OrderStatus.PROCESSING)
                        .deliveryMethod(DeliveryMethod.PICKUP)
                        .customerPhoneNumber("+380991234567")
                        .customerEmail(username)
                        .customerFirstName("John")
                        .customerLastName("Doe")
                        .items(List.of(OrderItem.builder()
                                        .id(1L)
                                        .phone(phone1)
                                        .selectedColor(PhoneColor.BLACK)
                                        .selectedStorage(StorageCapacity.CAPACITY_256GB)
                                        .totalPrice(BigDecimal.TEN)
                                        .build(),
                                OrderItem.builder()
                                        .id(1L)
                                        .phone(phone2)
                                        .selectedColor(PhoneColor.WHITE)
                                        .selectedStorage(StorageCapacity.CAPACITY_1TB)
                                        .totalPrice(BigDecimal.TEN)
                                        .build()))
                        .user(user)
                        .build()
        );
    }

    private List<Favorite> favoriteBuilderHelper(User user) {
        return List.of(
                new Favorite(user, phone1),
                new Favorite(user, phone2)
        );
    }
}