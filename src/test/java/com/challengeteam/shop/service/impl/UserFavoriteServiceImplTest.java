package com.challengeteam.shop.service.impl;

import com.challengeteam.shop.dto.phone.response.PhoneResponseDto;
import com.challengeteam.shop.dto.phone.response.PhoneColorResponseDto;
import com.challengeteam.shop.dto.phone.response.StorageCapacityResponseDto;
import com.challengeteam.shop.entity.favorite.Favorite;
import com.challengeteam.shop.entity.phone.Phone;
import com.challengeteam.shop.entity.user.User;
import com.challengeteam.shop.exceptionHandling.exception.ResourceNotFoundException;
import com.challengeteam.shop.mapper.phone.PhoneMapper;
import com.challengeteam.shop.persistence.repository.FavoriteRepository;
import com.challengeteam.shop.persistence.repository.PhoneRepository;
import com.challengeteam.shop.persistence.repository.UserRepository;
import com.challengeteam.shop.service.CatalogProductResponseAssembler;
import com.challengeteam.shop.service.ProductBadgeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.challengeteam.shop.service.impl.UserFavoriteServiceImplTest.TestResources.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class UserFavoriteServiceImplTest {

    @Mock
    private FavoriteRepository favoriteRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PhoneRepository phoneRepository;
    @Mock
    private PhoneMapper phoneMapper;
    @Mock
    private ProductBadgeService productBadgeService;
    @Mock
    private CatalogProductResponseAssembler catalogProductResponseAssembler;

    @InjectMocks
    private UserFavoriteServiceImpl userFavoriteService;

    @BeforeEach
    void configureBadgeService() {
        Mockito.lenient().when(productBadgeService.applyBadges(Mockito.anyList(), Mockito.anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        Mockito.lenient().when(catalogProductResponseAssembler.toResponses(Mockito.anyList()))
                .thenAnswer(invocation -> {
                    List<Phone> phones = invocation.getArgument(0);
                    return phones.isEmpty() ? List.of() : List.of(buildPhoneResponseDto());
                });
    }

    @Nested
    class GetUserFavoritesTest {

        @Test
        void whenFavoritesExist_thenReturnMappedPhones() {
            Favorite favorite = buildFavorite();
            List<PhoneResponseDto> expected = List.of(buildPhoneResponseDto());

            Mockito.when(favoriteRepository.findAllByUserIdWithPhoneImages(USER_ID))
                    .thenReturn(List.of(favorite));
            Mockito.when(catalogProductResponseAssembler.toResponses(List.of(favorite.getPhone())))
                    .thenReturn(expected);

            List<PhoneResponseDto> result = userFavoriteService.getUserFavorites(USER_ID);

            assertThat(result).isEqualTo(expected);
            Mockito.verify(favoriteRepository).findAllByUserIdWithPhoneImages(USER_ID);
            Mockito.verify(catalogProductResponseAssembler).toResponses(List.of(favorite.getPhone()));
        }

        @Test
        void whenUserIdIsNull_thenThrowException() {
            assertThatThrownBy(() -> userFavoriteService.getUserFavorites(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    class AddProductToFavoritesTest {

        @Test
        void whenFavoriteDoesNotExist_thenCreateFavorite() {
            Phone phone = buildPhone();
            User user = buildUser();
            List<PhoneResponseDto> expected = List.of(buildPhoneResponseDto());

            Mockito.when(phoneRepository.findById(PHONE_ID))
                    .thenReturn(Optional.of(phone));
            Mockito.when(favoriteRepository.existsByUserIdAndPhoneId(USER_ID, PHONE_ID))
                    .thenReturn(false);
            Mockito.when(userRepository.findById(USER_ID))
                    .thenReturn(Optional.of(user));
            Mockito.when(favoriteRepository.findAllByUserIdWithPhoneImages(USER_ID))
                    .thenReturn(List.of(buildFavorite(user, phone)));
            Mockito.when(catalogProductResponseAssembler.toResponses(anyList()))
                    .thenReturn(expected);

            List<PhoneResponseDto> result = userFavoriteService.addProductToFavorites(USER_ID, PHONE_ID);

            assertThat(result).isEqualTo(expected);
            ArgumentCaptor<Favorite> captor = ArgumentCaptor.forClass(Favorite.class);
            Mockito.verify(favoriteRepository).save(captor.capture());
            assertThat(captor.getValue().getUser()).isEqualTo(user);
            assertThat(captor.getValue().getPhone()).isEqualTo(phone);
        }

        @Test
        void whenFavoriteAlreadyExists_thenDoNotCreateDuplicate() {
            Phone phone = buildPhone();

            Mockito.when(phoneRepository.findById(PHONE_ID))
                    .thenReturn(Optional.of(phone));
            Mockito.when(favoriteRepository.existsByUserIdAndPhoneId(USER_ID, PHONE_ID))
                    .thenReturn(true);
            Mockito.when(favoriteRepository.findAllByUserIdWithPhoneImages(USER_ID))
                    .thenReturn(List.of(buildFavorite()));
            Mockito.when(catalogProductResponseAssembler.toResponses(anyList()))
                    .thenReturn(List.of(buildPhoneResponseDto()));

            userFavoriteService.addProductToFavorites(USER_ID, PHONE_ID);

            Mockito.verify(userRepository, never()).findById(USER_ID);
            Mockito.verify(favoriteRepository, never()).save(Mockito.any());
        }

        @Test
        void whenPhoneDoesNotExist_thenThrowException() {
            Mockito.when(phoneRepository.findById(PHONE_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> userFavoriteService.addProductToFavorites(USER_ID, PHONE_ID))
                    .isInstanceOf(ResourceNotFoundException.class);

            Mockito.verify(favoriteRepository, never()).save(Mockito.any());
        }
    }

    @Nested
    class RemoveProductFromFavoritesTest {

        @Test
        void whenFavoriteExists_thenDeleteFavorite() {
            Favorite favorite = buildFavorite();

            Mockito.when(phoneRepository.findById(PHONE_ID))
                    .thenReturn(Optional.of(buildPhone()));
            Mockito.when(favoriteRepository.findByUserIdAndPhoneId(USER_ID, PHONE_ID))
                    .thenReturn(Optional.of(favorite));
            Mockito.when(favoriteRepository.findAllByUserIdWithPhoneImages(USER_ID))
                    .thenReturn(List.of());
            Mockito.when(catalogProductResponseAssembler.toResponses(List.of()))
                    .thenReturn(List.of());

            List<PhoneResponseDto> result = userFavoriteService.removeProductFromFavorites(USER_ID, PHONE_ID);

            assertThat(result).isEmpty();
            Mockito.verify(favoriteRepository).delete(favorite);
        }

        @Test
        void whenFavoriteDoesNotExist_thenReturnCurrentFavorites() {
            Mockito.when(phoneRepository.findById(PHONE_ID))
                    .thenReturn(Optional.of(buildPhone()));
            Mockito.when(favoriteRepository.findByUserIdAndPhoneId(USER_ID, PHONE_ID))
                    .thenReturn(Optional.empty());
            Mockito.when(favoriteRepository.findAllByUserIdWithPhoneImages(USER_ID))
                    .thenReturn(List.of());
            Mockito.when(catalogProductResponseAssembler.toResponses(List.of()))
                    .thenReturn(List.of());

            List<PhoneResponseDto> result = userFavoriteService.removeProductFromFavorites(USER_ID, PHONE_ID);

            assertThat(result).isEmpty();
            Mockito.verify(favoriteRepository, never()).delete(Mockito.any());
        }

        @Test
        void whenPhoneDoesNotExist_thenThrowException() {
            Mockito.when(phoneRepository.findById(PHONE_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> userFavoriteService.removeProductFromFavorites(USER_ID, PHONE_ID))
                    .isInstanceOf(ResourceNotFoundException.class);

            Mockito.verify(favoriteRepository, never()).findByUserIdAndPhoneId(USER_ID, PHONE_ID);
        }
    }

    static class TestResources {
        static final Long USER_ID = 1L;
        static final Long PHONE_ID = 2L;

        static User buildUser() {
            return User.builder()
                    .id(USER_ID)
                    .email("test@example.com")
                    .build();
        }

        static Phone buildPhone() {
            return Phone.builder()
                    .id(PHONE_ID)
                    .name("iPhone 15")
                    .description("Latest Apple smartphone")
                    .price(new BigDecimal("999.99"))
                    .brand("Apple")
                    .releaseYear(2024)
                    .images(List.of())
                    .build();
        }

        static Favorite buildFavorite() {
            return buildFavorite(buildUser(), buildPhone());
        }

        static Favorite buildFavorite(User user, Phone phone) {
            return Favorite.builder()
                    .id(10L)
                    .user(user)
                    .phone(phone)
                    .build();
        }

        static PhoneResponseDto buildPhoneResponseDto() {
            Phone phone = buildPhone();
            return new PhoneResponseDto(
                    phone.getId(),
                    phone.getName(),
                    phone.getDescription(),
                    phone.getPrice(),
                    phone.getBrand(),
                    phone.getReleaseYear(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    Set.of(new PhoneColorResponseDto("BLACK", "Black", "#000000")),
                    Set.of(new StorageCapacityResponseDto("CAPACITY_128GB", 128, "GB")),
                    List.of()
            );
        }
    }
}
