package com.challengeteam.shop.web.controller;

import com.challengeteam.shop.dto.phone.response.PhoneResponseDto;
import com.challengeteam.shop.dto.phone.response.PhoneColorResponseDto;
import com.challengeteam.shop.dto.phone.response.StorageCapacityResponseDto;
import com.challengeteam.shop.entity.user.Role;
import com.challengeteam.shop.entity.user.User;
import com.challengeteam.shop.exceptionHandling.GlobalExceptionHandler;
import com.challengeteam.shop.exceptionHandling.exception.ResourceNotFoundException;
import com.challengeteam.shop.security.SimpleUserDetailsService.SimpleUserDetails;
import com.challengeteam.shop.service.UserFavoriteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static com.challengeteam.shop.web.controller.UserFavoriteControllerTest.TestResources.*;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserFavoriteControllerTest {

    @Mock
    private UserFavoriteService userFavoriteService;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new UserFavoriteController(userFavoriteService))
                .setCustomArgumentResolvers(new TestAuthenticationPrincipalResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Nested
    class GetUserFavoritesTest {
        private static final String URL = "/api/v1/me/favorites";

        @Test
        void whenFavoritesExist_thenStatus200AndReturnFavorites() throws Exception {
            Mockito.when(userFavoriteService.getUserFavorites(USER_ID))
                    .thenReturn(List.of(buildPhoneResponseDto(PHONE_ID_1), buildPhoneResponseDto(PHONE_ID_2)));

            mockMvc.perform(get(URL))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].id").value(PHONE_ID_1))
                    .andExpect(jsonPath("$[0].name").value(PHONE_NAME))
                    .andExpect(jsonPath("$[0].brand").value(PHONE_BRAND))
                    .andExpect(jsonPath("$[0].price").value(PHONE_PRICE.doubleValue()))
                    .andExpect(jsonPath("$[0].images").isArray());
        }

        @Test
        void whenFavoritesAreEmpty_thenReturnEmptyList() throws Exception {
            Mockito.when(userFavoriteService.getUserFavorites(USER_ID))
                    .thenReturn(List.of());

            mockMvc.perform(get(URL))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    @Nested
    class AddProductToFavoritesTest {
        private static final String URL = "/api/v1/me/favorites/{phoneId}";

        @Test
        void whenValidRequest_thenStatus200AndAddFavorite() throws Exception {
            Mockito.when(userFavoriteService.addProductToFavorites(USER_ID, PHONE_ID_1))
                    .thenReturn(List.of(buildPhoneResponseDto(PHONE_ID_1)));

            mockMvc.perform(post(URL, PHONE_ID_1))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id").value(PHONE_ID_1))
                    .andExpect(jsonPath("$[0].name").value(PHONE_NAME))
                    .andExpect(jsonPath("$[0].brand").value(PHONE_BRAND));
        }

        @Test
        void whenFavoriteAlreadyExists_thenReturnStableFavoritesList() throws Exception {
            Mockito.when(userFavoriteService.addProductToFavorites(USER_ID, PHONE_ID_1))
                    .thenReturn(List.of(buildPhoneResponseDto(PHONE_ID_1)));

            mockMvc.perform(post(URL, PHONE_ID_1))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id").value(PHONE_ID_1));
        }

        @Test
        void whenPhoneDoesNotExist_thenStatus404() throws Exception {
            Mockito.when(userFavoriteService.addProductToFavorites(USER_ID, NON_EXISTING_PHONE_ID))
                    .thenThrow(new ResourceNotFoundException("Phone with id " + NON_EXISTING_PHONE_ID + " not found"));

            mockMvc.perform(post(URL, NON_EXISTING_PHONE_ID))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class RemoveProductFromFavoritesTest {
        private static final String URL = "/api/v1/me/favorites/{phoneId}";

        @Test
        void whenFavoriteExists_thenStatus200AndRemoveFavorite() throws Exception {
            Mockito.when(userFavoriteService.removeProductFromFavorites(USER_ID, PHONE_ID_1))
                    .thenReturn(List.of());

            mockMvc.perform(delete(URL, PHONE_ID_1))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        void whenFavoriteDoesNotExist_thenStatus200AndReturnCurrentFavorites() throws Exception {
            Mockito.when(userFavoriteService.removeProductFromFavorites(USER_ID, PHONE_ID_1))
                    .thenReturn(List.of());

            mockMvc.perform(delete(URL, PHONE_ID_1))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        void whenPhoneDoesNotExist_thenStatus404() throws Exception {
            Mockito.when(userFavoriteService.removeProductFromFavorites(USER_ID, NON_EXISTING_PHONE_ID))
                    .thenThrow(new ResourceNotFoundException("Phone with id " + NON_EXISTING_PHONE_ID + " not found"));

            mockMvc.perform(delete(URL, NON_EXISTING_PHONE_ID))
                    .andExpect(status().isNotFound());
        }
    }

    static class TestAuthenticationPrincipalResolver implements HandlerMethodArgumentResolver {

        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return parameter.hasParameterAnnotation(AuthenticationPrincipal.class)
                    && parameter.getParameterType().equals(SimpleUserDetails.class);
        }

        @Override
        public Object resolveArgument(MethodParameter parameter,
                                      ModelAndViewContainer mavContainer,
                                      NativeWebRequest webRequest,
                                      WebDataBinderFactory binderFactory) {
            Role role = Role.builder()
                    .name("USER")
                    .build();
            User user = User.builder()
                    .id(USER_ID)
                    .email("test@example.com")
                    .password("password")
                    .role(role)
                    .build();

            return new SimpleUserDetails(user);
        }
    }

    static class TestResources {
        static final Long USER_ID = 1L;
        static final Long PHONE_ID_1 = 10L;
        static final Long PHONE_ID_2 = 11L;
        static final Long NON_EXISTING_PHONE_ID = 99_999L;
        static final String PHONE_NAME = "iPhone 15";
        static final String PHONE_DESCRIPTION = "Latest Apple smartphone";
        static final BigDecimal PHONE_PRICE = new BigDecimal("999.99");
        static final String PHONE_BRAND = "Apple";

        static PhoneResponseDto buildPhoneResponseDto(Long phoneId) {
            return new PhoneResponseDto(
                    phoneId,
                    PHONE_NAME,
                    PHONE_DESCRIPTION,
                    PHONE_PRICE,
                    PHONE_BRAND,
                    2024,
                    "Apple A16 Bionic",
                    6,
                    "6.1\"",
                    "12 MP",
                    "48 MP",
                    "3349 mAh",
                    Set.of(new PhoneColorResponseDto("BLACK", "Black", "#000000")),
                    Set.of(new StorageCapacityResponseDto("CAPACITY_128GB", 128, "GB")),
                    List.of()
            );
        }
    }
}
