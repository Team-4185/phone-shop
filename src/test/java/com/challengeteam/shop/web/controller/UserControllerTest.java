package com.challengeteam.shop.web.controller;

import com.challengeteam.shop.dto.user.request.CreateUserDto;
import com.challengeteam.shop.dto.user.request.UpdateProfileDto;
import com.challengeteam.shop.entity.favorite.Favorite;
import com.challengeteam.shop.entity.order.DeliveryMethod;
import com.challengeteam.shop.entity.order.Order;
import com.challengeteam.shop.entity.order.OrderItem;
import com.challengeteam.shop.entity.order.OrderStatus;
import com.challengeteam.shop.entity.order.payment.PaymentDetails;
import com.challengeteam.shop.entity.order.payment.PaymentMethod;
import com.challengeteam.shop.entity.order.payment.PaymentStatus;
import com.challengeteam.shop.entity.phone.*;
import com.challengeteam.shop.entity.user.User;
import com.challengeteam.shop.persistence.repository.FavoriteRepository;
import com.challengeteam.shop.persistence.repository.OrderRepository;
import com.challengeteam.shop.persistence.repository.PhoneRepository;
import com.challengeteam.shop.persistence.repository.UserRepository;
import com.challengeteam.shop.service.UserService;
import com.challengeteam.shop.testContainer.ContainerExtension;
import com.challengeteam.shop.testContainer.TestContextConfigurator;
import com.challengeteam.shop.web.TestAuthHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Set;

import static com.challengeteam.shop.web.controller.PhoneControllerTest.TestResources.auth;
import static com.challengeteam.shop.web.controller.UserControllerTest.TestResources.*;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(ContainerExtension.class)
class UserControllerTest {
    @Autowired
    private TestAuthHelper testAuthHelper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private FavoriteRepository favoriteRepository;
    @Autowired
    private PhoneRepository phoneRepository;

    private String token;
    private Long user1;
    private Long user2;
    private Long user3;

    @DynamicPropertySource
    static void loadPropertiesForTest(DynamicPropertyRegistry propertyRegistry) {
        TestContextConfigurator.initRequiredProperties(propertyRegistry);
    }

    @BeforeEach
    public void setup() {
        orderRepository.deleteAll();
        favoriteRepository.deleteAll();
        phoneRepository.deleteAll();
        userRepository.deleteAll();

        user1 = userService.createDefaultUser(buildCreateUserDto(TestUserCredentials.USER_1));
        user2 = userService.createDefaultUser(buildCreateUserDto(TestUserCredentials.USER_2));
        user3 = userService.createDefaultUser(buildCreateUserDto(TestUserCredentials.USER_3));

        token = testAuthHelper.authorizeLikeTestUser();
    }

    @Nested
    @DisplayName("GET /api/v1/users")
    class GetAllUsersTest {
        private static final String URL = "/api/v1/users";

        @Test
        void whenValidRequest_thenStatus200AndReturnUsers() throws Exception {
            mockMvc.perform(get(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth(token)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(4))
                    .andExpect(jsonPath("$[0].id").exists())
                    .andExpect(jsonPath("$[0].email").exists());
        }

        @Test
        void whenRequestMissingToken_thenStatus401() throws Exception {
            mockMvc.perform(get(URL))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void whenRequestHasInvalidToken_thenStatus401() throws Exception {
            mockMvc.perform(get(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth("invalid_token")))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/users/{id}")
    class GetUserByIdTest {
        private static final String URL = "/api/v1/users/{id}";

        @Test
        void whenExists_thenStatus200AndReturnUser() throws Exception {
            mockMvc.perform(get(URL, user1)
                            .header(HttpHeaders.AUTHORIZATION, auth(token)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(user1))
                    .andExpect(jsonPath("$.email").value(TestUserCredentials.USER_1.email));
        }

        @Test
        void whenDoesntExist_thenStatus404() throws Exception {
            mockMvc.perform(get(URL, NON_EXISTING_ID)
                            .header(HttpHeaders.AUTHORIZATION, auth(token)))
                    .andExpect(status().isNotFound());
        }

        @Test
        void whenRequestMissingToken_thenStatus401() throws Exception {
            mockMvc.perform(get(URL, user1))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void whenRequestHasInvalidToken_thenStatus401() throws Exception {
            mockMvc.perform(get(URL, user1)
                            .header(HttpHeaders.AUTHORIZATION, auth("invalid_token")))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void whenIdIsNotInteger_thenStatus404() throws Exception {
            var request = get(URL, "not_integer")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.AUTHORIZATION, auth(token));

            mockMvc.perform(request)
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/users")
    class CreateUserTest {
        private static final String URL = "/api/v1/users";

        @Test
        void whenValidRequest_thenStatus201AndLocationHeader() throws Exception {
            CreateUserDto request = buildCreateUserDto(TestUserCredentials.VALID_USER);

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(header().exists(HttpHeaders.LOCATION));
        }

        @Test
        void whenValidRequestWithBoundaryMinValues_thenStatus201() throws Exception {
            CreateUserDto request = buildCreateUserDto(TestUserCredentials.VALID_USER_BOUNDARY_MIN);

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }

        @Test
        void whenValidRequestWithBoundaryMaxValues_thenStatus201() throws Exception {
            CreateUserDto request = buildCreateUserDto(TestUserCredentials.VALID_USER_BOUNDARY_MAX);

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }

        @Test
        void whenRequestMissingToken_thenStatus401() throws Exception {
            CreateUserDto request = buildCreateUserDto(TestUserCredentials.VALID_USER);

            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void whenRequestHasInvalidToken_thenStatus401() throws Exception {
            CreateUserDto request = buildCreateUserDto(TestUserCredentials.VALID_USER);

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth("invalid_token"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void whenEmailIsNull_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserCredentials.EMAIL_IS_NULL);
        }

        @Test
        void whenEmailIsBlank_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserCredentials.EMAIL_IS_BLANK);
        }

        @Test
        void whenEmailTooShort_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserCredentials.EMAIL_IS_TOO_SHORT);
        }

        @Test
        void whenEmailTooLong_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserCredentials.EMAIL_IS_TOO_LONG);
        }

        @Test
        void whenEmailInvalidFormat_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserCredentials.EMAIL_IS_NOT_VALID);
        }

        @Test
        void whenEmailInvalidPattern_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserCredentials.EMAIL_INVALID_PATTERN);
        }

        @Test
        void whenPasswordIsNull_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserCredentials.PASSWORD_IS_NULL);
        }

        @Test
        void whenPasswordIsBlank_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserCredentials.PASSWORD_IS_BLANK);
        }

        @Test
        void whenPasswordTooShort_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserCredentials.PASSWORD_IS_TOO_SHORT);
        }

        @Test
        void whenPasswordTooLong_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserCredentials.PASSWORD_IS_TOO_LONG);
        }

        @Test
        void whenPasswordNoUppercase_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserCredentials.PASSWORD_IS_MISSING_CAPITAL_LETTER);
        }

        @Test
        void whenPasswordNoLowercase_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserCredentials.PASSWORD_IS_MISSING_SMALL_LETTER);
        }

        @Test
        void whenPasswordNoDigit_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserCredentials.PASSWORD_IS_MISSING_DIGIT);
        }

        @Test
        void whenPasswordNoSpecialChar_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserCredentials.PASSWORD_IS_MISSING_SPECIAL_SYMBOL);
        }

        @Test
        void whenEmailAlreadyExists_thenReturn400() throws Exception {
            CreateUserDto request = buildCreateUserDto(TestUserCredentials.USER_1);

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        private void expect400WithInvalidBody(TestUserCredentials credentials) throws Exception {
            CreateUserDto request = buildCreateUserDto(credentials);

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/users/{id}/update-profile")
    class UpdateProfileTest {
        private static final String URL = "/api/v1/users/{id}/update-profile";

        @Test
        void whenValidRequest_thenStatus204() throws Exception {
            UpdateProfileDto request = buildUpdateProfileDto(TestUserProfile.VALID_PROFILE);

            mockMvc.perform(patch(URL, user1)
                            .header(HttpHeaders.AUTHORIZATION, auth(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNoContent());
        }

        @Test
        void whenValidRequestWithBoundaryMinValues_thenStatus204() throws Exception {
            UpdateProfileDto request = buildUpdateProfileDto(TestUserProfile.VALID_PROFILE_BOUNDARY_MIN);

            mockMvc.perform(patch(URL, user1)
                            .header(HttpHeaders.AUTHORIZATION, auth(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNoContent());
        }

        @Test
        void whenValidRequestWithBoundaryMaxValues_thenStatus204() throws Exception {
            UpdateProfileDto request = buildUpdateProfileDto(TestUserProfile.VALID_PROFILE_BOUNDARY_MAX);

            mockMvc.perform(patch(URL, user1)
                            .header(HttpHeaders.AUTHORIZATION, auth(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNoContent());
        }

        @Test
        void whenValidRequestWithHyphenatedName_thenStatus204() throws Exception {
            UpdateProfileDto request = buildUpdateProfileDto(TestUserProfile.VALID_PROFILE_WITH_HYPHEN);

            mockMvc.perform(patch(URL, user1)
                            .header(HttpHeaders.AUTHORIZATION, auth(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNoContent());
        }

        @Test
        void whenValidRequestWithApostrophe_thenStatus204() throws Exception {
            UpdateProfileDto request = buildUpdateProfileDto(TestUserProfile.VALID_PROFILE_WITH_APOSTROPHE);

            mockMvc.perform(patch(URL, user1)
                            .header(HttpHeaders.AUTHORIZATION, auth(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNoContent());
        }

        @Test
        void whenValidRequestWithSpacedName_thenStatus204() throws Exception {
            UpdateProfileDto request = buildUpdateProfileDto(TestUserProfile.VALID_PROFILE_WITH_SPACE);

            mockMvc.perform(patch(URL, user1)
                            .header(HttpHeaders.AUTHORIZATION, auth(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNoContent());
        }

        @Test
        void whenValidRequestWithLatinNames_thenStatus204() throws Exception {
            UpdateProfileDto request = buildUpdateProfileDto(TestUserProfile.VALID_PROFILE_LATIN);

            mockMvc.perform(patch(URL, user1)
                            .header(HttpHeaders.AUTHORIZATION, auth(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNoContent());
        }

        @Test
        void whenValidRequestWithCyrillicNames_thenStatus204() throws Exception {
            UpdateProfileDto request = buildUpdateProfileDto(TestUserProfile.VALID_PROFILE_CYRILLIC);

            mockMvc.perform(patch(URL, user1)
                            .header(HttpHeaders.AUTHORIZATION, auth(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNoContent());
        }

        @Test
        void whenValidRequestWithOptionalPhone_thenStatus204() throws Exception {
            UpdateProfileDto request = buildUpdateProfileDto(TestUserProfile.VALID_PROFILE_WITHOUT_PHONE);

            mockMvc.perform(patch(URL, user1)
                            .header(HttpHeaders.AUTHORIZATION, auth(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNoContent());
        }

        @Test
        void whenValidRequestWithPhone380Format_thenStatus204() throws Exception {
            UpdateProfileDto request = buildUpdateProfileDto(TestUserProfile.VALID_PROFILE_PHONE_WITHOUT_PLUS);

            mockMvc.perform(patch(URL, user1)
                            .header(HttpHeaders.AUTHORIZATION, auth(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNoContent());
        }

        @Test
        void whenRequestMissingToken_thenStatus401() throws Exception {
            UpdateProfileDto request = buildUpdateProfileDto(TestUserProfile.VALID_PROFILE);

            mockMvc.perform(patch(URL, user1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void whenRequestHasInvalidToken_thenStatus401() throws Exception {
            UpdateProfileDto request = buildUpdateProfileDto(TestUserProfile.VALID_PROFILE);

            mockMvc.perform(patch(URL, user1)
                            .header(HttpHeaders.AUTHORIZATION, auth("invalid_token"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void whenIdIsNotInteger_thenStatus404() throws Exception {
            var request = put(URL, "not_integer")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.AUTHORIZATION, auth(token));

            mockMvc.perform(request)
                    .andExpect(status().isNotFound());
        }

        @Test
        void whenUserDoesntExist_thenStatus404() throws Exception {
            UpdateProfileDto request = buildUpdateProfileDto(TestUserProfile.VALID_PROFILE);

            mockMvc.perform(patch(URL, NON_EXISTING_ID)
                            .header(HttpHeaders.AUTHORIZATION, auth(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        void whenFirstNameTooShort_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserProfile.FIRSTNAME_TOO_SHORT);
        }

        @Test
        void whenFirstNameTooLong_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserProfile.FIRSTNAME_TOO_LONG);
        }

        @Test
        void whenFirstNameWithDigits_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserProfile.FIRSTNAME_WITH_DIGITS);
        }

        @Test
        void whenFirstNameWithSpecialChars_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserProfile.FIRSTNAME_WITH_SPECIAL_CHARS);
        }

        @Test
        void whenFirstNameStartsWithHyphen_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserProfile.FIRSTNAME_STARTS_WITH_HYPHEN);
        }

        @Test
        void whenFirstNameEndsWithHyphen_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserProfile.FIRSTNAME_ENDS_WITH_HYPHEN);
        }

        @Test
        void whenFirstNameDoubleHyphen_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserProfile.FIRSTNAME_DOUBLE_HYPHEN);
        }

        @Test
        void whenFirstNameDoubleSpace_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserProfile.FIRSTNAME_DOUBLE_SPACE);
        }

        @Test
        void whenLastNameTooShort_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserProfile.LASTNAME_TOO_SHORT);
        }

        @Test
        void whenLastNameTooLong_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserProfile.LASTNAME_TOO_LONG);
        }

        @Test
        void whenLastNameWithDigits_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserProfile.LASTNAME_WITH_DIGITS);
        }

        @Test
        void whenLastNameWithSpecialChars_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserProfile.LASTNAME_WITH_SPECIAL_CHARS);
        }

        @Test
        void whenLastNameStartsWithHyphen_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserProfile.LASTNAME_STARTS_WITH_HYPHEN);
        }

        @Test
        void whenLastNameEndsWithHyphen_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserProfile.LASTNAME_ENDS_WITH_HYPHEN);
        }

        @Test
        void whenLastNameDoubleHyphen_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserProfile.LASTNAME_DOUBLE_HYPHEN);
        }

        @Test
        void whenCityTooShort_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserProfile.CITY_TOO_SHORT);
        }

        @Test
        void whenCityTooLong_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserProfile.CITY_TOO_LONG);
        }

        @Test
        void whenCityWithDigits_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserProfile.CITY_WITH_DIGITS);
        }

        @Test
        void whenCityWithSpecialChars_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserProfile.CITY_WITH_SPECIAL_CHARS);
        }

        @Test
        void whenCityStartsWithHyphen_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserProfile.CITY_STARTS_WITH_HYPHEN);
        }

        @Test
        void whenCityEndsWithHyphen_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserProfile.CITY_ENDS_WITH_HYPHEN);
        }

        @Test
        void whenPhoneNumberInvalidFormat_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserProfile.PHONE_INVALID_FORMAT);
        }

        @Test
        void whenPhoneNumberWithLetters_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserProfile.PHONE_WITH_LETTERS);
        }

        @Test
        void whenPhoneNumberTooShort_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserProfile.PHONE_TOO_SHORT);
        }

        @Test
        void whenPhoneNumberTooLong_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserProfile.PHONE_TOO_LONG);
        }

        @Test
        void whenPhoneNumberWrongCountryCode_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserProfile.PHONE_WRONG_COUNTRY_CODE);
        }

        @Test
        void whenPhoneNumberWithSpaces_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserProfile.PHONE_WITH_SPACES);
        }

        @Test
        void whenPhoneNumberWithDashes_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserProfile.PHONE_WITH_DASHES);
        }

        private void expect400WithInvalidBody(TestUserProfile profile) throws Exception {
            UpdateProfileDto request = buildUpdateProfileDto(profile);

            mockMvc.perform(patch(URL, user1)
                            .header(HttpHeaders.AUTHORIZATION, auth(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/users/{id}")
    class DeleteUserByIdTest {
        private static final String URL = "/api/v1/users/{id}";

        @Test
        void whenExists_thenStatus204() throws Exception {
            mockMvc.perform(delete(URL, user1)
                            .header(HttpHeaders.AUTHORIZATION, auth(token)))
                    .andExpect(status().isNoContent());
        }

        @Test
        void whenDoesntExist_thenStatus404() throws Exception {
            mockMvc.perform(delete(URL, NON_EXISTING_ID)
                            .header(HttpHeaders.AUTHORIZATION, auth(token)))
                    .andExpect(status().isNotFound());
        }

        @Test
        void whenRequestMissingToken_thenStatus401() throws Exception {
            mockMvc.perform(delete(URL, user1))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void whenRequestHasInvalidToken_thenStatus401() throws Exception {
            mockMvc.perform(delete(URL, user1)
                            .header(HttpHeaders.AUTHORIZATION, auth("invalid_token")))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void whenIdIsNotInteger_thenStatus404() throws Exception {
            var request = delete(URL, "not_integer")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.AUTHORIZATION, auth(token));

            mockMvc.perform(request)
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/users/me")
    class GetCurrentUserTest {
        private static final String URL = "/api/v1/users/me";

        private User currentUser;
        private Phone phone;

        @BeforeEach
        void setup() {
            currentUser = userRepository
                    .findByEmail(TestAuthHelper.TEST_COMPONENT_EMAIL)
                    .orElseThrow();

            phone = phoneRepository.save(Phone.builder()
                    .name("iPhone 15")
                    .description("Test phone")
                    .price(BigDecimal.valueOf(999.99))
                    .brand("Apple")
                    .releaseYear(2023)
                    .sku("TEST-SKU-001")
                    .stock(10)
                    .status(ProductStatus.IN_STOCK)
                    .phoneCharacteristics(PhoneCharacteristics.builder()
                            .cpu("A17 Pro")
                            .coresNumber(6)
                            .screenSize("6.1\"")
                            .frontCamera("12MP")
                            .mainCamera("48MP")
                            .batteryCapacity("3274mAh")
                            .phoneColors(Set.of(PhoneColor.BLACK))
                            .storageCapacities(Set.of(StorageCapacity.CAPACITY_128GB))
                            .build())
                    .build());
        }

        @Test
        void whenNoToken_thenStatus401() throws Exception {
            mockMvc.perform(get(URL))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void whenInvalidToken_thenStatus401() throws Exception {
            mockMvc.perform(get(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth("invalid_token")))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void whenValidToken_thenStatus200() throws Exception {
            mockMvc.perform(get(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth(token)))
                    .andExpect(status().isOk());
        }

        @Test
        void whenValidToken_thenReturnsCorrectUserFields() throws Exception {
            mockMvc.perform(get(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth(token)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(currentUser.getId()))
                    .andExpect(jsonPath("$.email").value(currentUser.getEmail()))
                    .andExpect(jsonPath("$.role.name").isNotEmpty());
        }

        @Test
        void whenValidToken_thenCartIsPresent() throws Exception {
            mockMvc.perform(get(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth(token)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.cart").isNotEmpty())
                    .andExpect(jsonPath("$.cart.totalPrice").value(0));
        }

        @Test
        void whenUserHasNoOrders_thenOrdersIsEmpty() throws Exception {
            mockMvc.perform(get(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth(token)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.orders").isArray())
                    .andExpect(jsonPath("$.orders", hasSize(0)));
        }

        @Test
        void whenUserHasOrders_thenOrdersReturned() throws Exception {
            orderRepository.save(buildOrder(currentUser, phone));

            mockMvc.perform(get(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth(token)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.orders").isArray())
                    .andExpect(jsonPath("$.orders", hasSize(1)));
        }

        @Test
        void whenUserHasNoFavorites_thenFavoritesIsEmpty() throws Exception {
            mockMvc.perform(get(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth(token)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.favorites").isArray())
                    .andExpect(jsonPath("$.favorites", hasSize(0)));
        }

        @Test
        void whenUserHasFavorites_thenFavoritesReturned() throws Exception {
            favoriteRepository.save(new Favorite(currentUser, phone));

            mockMvc.perform(get(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth(token)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.favorites").isArray())
                    .andExpect(jsonPath("$.favorites", hasSize(1)));
        }

        private Order buildOrder(User user, Phone phone) {
            Order order = Order.builder()
                    .user(user)
                    .customerEmail(user.getEmail())
                    .customerFirstName("John")
                    .customerLastName("Doe")
                    .customerPhoneNumber("+380991234567")
                    .status(OrderStatus.NEW)
                    .paymentMethod(PaymentMethod.CARD)
                    .deliveryMethod(DeliveryMethod.PICKUP)
                    .paymentDetails(new PaymentDetails(PaymentStatus.PENDING, null))
                    .total(BigDecimal.valueOf(999.99))
                    .build();

            OrderItem item = OrderItem.builder()
                    .productName(phone.getName())
                    .sku(phone.getSku())
                    .unitPrice(phone.getPrice())
                    .selectedColor(PhoneColor.BLACK)
                    .selectedStorage(StorageCapacity.CAPACITY_128GB)
                    .quantity(1)
                    .totalPrice(phone.getPrice())
                    .build();

            order.addItem(item);
            return order;
        }
    }



    static class TestResources {
        static final long NON_EXISTING_ID = 99_999L;

        static String auth(String token) {
            return "Bearer " + token;
        }

        static CreateUserDto buildCreateUserDto(TestUserCredentials credentials) {
            return new CreateUserDto(
                    credentials.email,
                    credentials.password
            );
        }

        static UpdateProfileDto buildUpdateProfileDto(TestUserProfile profile) {
            return new UpdateProfileDto(
                    profile.firstName,
                    profile.lastName,
                    profile.city,
                    profile.phoneNumber
            );
        }
    }

    enum TestUserCredentials {
        USER_1("user1@example.com", "Pass123!word"),
        USER_2("user2@example.com", "Pass456!word"),
        USER_3("user3@example.com", "Pass789!word"),

        VALID_USER("valid.user@example.com", "Valid123!Pass"),
        VALID_USER_BOUNDARY_MIN("abc@def.gh", "Aa1!bcde"),
        VALID_USER_BOUNDARY_MAX("userxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx@example.com", "Valid1!" + "p".repeat(43)),

        // Invalid email
        EMAIL_IS_NULL(null, "Valid123!Pass"),
        EMAIL_IS_BLANK("", "Valid123!Pass"),
        EMAIL_IS_TOO_SHORT("a@b.c", "Valid123!Pass"),
        EMAIL_IS_TOO_LONG("a".repeat(91) + "@example.com", "Valid123!Pass"),
        EMAIL_IS_NOT_VALID("invalid-email", "Valid123!Pass"),
        EMAIL_INVALID_PATTERN("ab@cd.e", "Valid123!Pass"),

        // Invalid password
        PASSWORD_IS_NULL("test@example.com", null),
        PASSWORD_IS_BLANK("test@example.com", ""),
        PASSWORD_IS_TOO_SHORT("test@example.com", "Pa1!swd"),
        PASSWORD_IS_TOO_LONG("test@example.com", "Valid1!" + "p".repeat(44)),
        PASSWORD_IS_MISSING_CAPITAL_LETTER("test@example.com", "password123!"),
        PASSWORD_IS_MISSING_SMALL_LETTER("test@example.com", "PASSWORD123!"),
        PASSWORD_IS_MISSING_DIGIT("test@example.com", "Password!"),
        PASSWORD_IS_MISSING_SPECIAL_SYMBOL("test@example.com", "Password123");

        private final String email;
        private final String password;

        TestUserCredentials(String email, String password) {
            this.email = email;
            this.password = password;
        }
    }

    enum TestUserProfile {
        // Valid profiles
        VALID_PROFILE("Іван", "Петренко", "Київ", "+380123456789"),
        VALID_PROFILE_BOUNDARY_MIN("Абв", "Где", "Жзі", "+380123456789"),
        VALID_PROFILE_BOUNDARY_MAX("А".repeat(255), "Б".repeat(255), "В".repeat(255), "+380999999999"),
        VALID_PROFILE_WITH_HYPHEN("Анна-Марія", "Коваль-Петренко", "Івано-Франківськ", "+380123456789"),
        VALID_PROFILE_WITH_APOSTROPHE("О'Брайен", "Д'Артаньян", "Сен-Тропе", "+380123456789"),
        VALID_PROFILE_WITH_SPACE("Жан Клод", "Ван Дамм", "Лос Анджелес", "+380123456789"),
        VALID_PROFILE_LATIN("John", "Smith", "London", "+380123456789"),
        VALID_PROFILE_CYRILLIC("Олександр", "Шевченко", "Дніпро", "+380123456789"),
        VALID_PROFILE_WITHOUT_PHONE("Іван", "Петренко", "Київ", null),
        VALID_PROFILE_PHONE_WITHOUT_PLUS("Іван", "Петренко", "Київ", "380123456789"),

        // Invalid firstName - size
        FIRSTNAME_TOO_SHORT("Аб", "Петренко", "Київ", "+380123456789"),
        FIRSTNAME_TOO_LONG("А".repeat(256), "Петренко", "Київ", "+380123456789"),

        // Invalid firstName - pattern
        FIRSTNAME_WITH_DIGITS("Іван123", "Петренко", "Київ", "+380123456789"),
        FIRSTNAME_WITH_SPECIAL_CHARS("Іван@!", "Петренко", "Київ", "+380123456789"),
        FIRSTNAME_STARTS_WITH_HYPHEN("-Іван", "Петренко", "Київ", "+380123456789"),
        FIRSTNAME_ENDS_WITH_HYPHEN("Іван-", "Петренко", "Київ", "+380123456789"),
        FIRSTNAME_DOUBLE_HYPHEN("Іван--Петро", "Петренко", "Київ", "+380123456789"),
        FIRSTNAME_DOUBLE_SPACE("Іван  Петро", "Петренко", "Київ", "+380123456789"),

        // Invalid lastName - size
        LASTNAME_TOO_SHORT("Іван", "Пе", "Київ", "+380123456789"),
        LASTNAME_TOO_LONG("Іван", "П".repeat(256), "Київ", "+380123456789"),

        // Invalid lastName - pattern
        LASTNAME_WITH_DIGITS("Іван", "Петренко123", "Київ", "+380123456789"),
        LASTNAME_WITH_SPECIAL_CHARS("Іван", "Петренко@#", "Київ", "+380123456789"),
        LASTNAME_STARTS_WITH_HYPHEN("Іван", "-Петренко", "Київ", "+380123456789"),
        LASTNAME_ENDS_WITH_HYPHEN("Іван", "Петренко-", "Київ", "+380123456789"),
        LASTNAME_DOUBLE_HYPHEN("Іван", "Петренко--Іваненко", "Київ", "+380123456789"),

        // Invalid city - size
        CITY_TOO_SHORT("Іван", "Петренко", "Кї", "+380123456789"),
        CITY_TOO_LONG("Іван", "Петренко", "К".repeat(256), "+380123456789"),

        // Invalid city - pattern
        CITY_WITH_DIGITS("Іван", "Петренко", "Київ123", "+380123456789"),
        CITY_WITH_SPECIAL_CHARS("Іван", "Петренко", "Київ@#", "+380123456789"),
        CITY_STARTS_WITH_HYPHEN("Іван", "Петренко", "-Київ", "+380123456789"),
        CITY_ENDS_WITH_HYPHEN("Іван", "Петренко", "Київ-", "+380123456789"),

        // Invalid phoneNumber - pattern
        PHONE_INVALID_FORMAT("Іван", "Петренко", "Київ", "0501234567"),
        PHONE_WITH_LETTERS("Іван", "Петренко", "Київ", "+380abc456789"),
        PHONE_TOO_SHORT("Іван", "Петренко", "Київ", "+38012345678"),
        PHONE_TOO_LONG("Іван", "Петренко", "Київ", "+3801234567890"),
        PHONE_WRONG_COUNTRY_CODE("Іван", "Петренко", "Київ", "+1234567890"),
        PHONE_WITH_SPACES("Іван", "Петренко", "Київ", "+380 12 345 6789"),
        PHONE_WITH_DASHES("Іван", "Петренко", "Київ", "+380-12-345-6789");

        private final String firstName;
        private final String lastName;
        private final String city;
        private final String phoneNumber;

        TestUserProfile(String firstName, String lastName, String city, String phoneNumber) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.city = city;
            this.phoneNumber = phoneNumber;
        }
    }
}