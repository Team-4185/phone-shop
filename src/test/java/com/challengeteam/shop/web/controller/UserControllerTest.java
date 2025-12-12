package com.challengeteam.shop.web.controller;

import com.challengeteam.shop.dto.user.CreateUserDto;
import com.challengeteam.shop.dto.user.UpdateProfileDto;
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

import static com.challengeteam.shop.web.controller.UserControllerTest.TestResources.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(ContainerExtension.class)
class UserControllerTest {
    @Autowired private TestAuthHelper testAuthHelper;
    @Autowired private UserRepository userRepository;
    @Autowired private UserService userService;
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

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
        void whenRequestMissingToken_thenStatus403() throws Exception {
            mockMvc.perform(get(URL))
                    .andExpect(status().isForbidden());
        }

        @Test
        void whenRequestHasInvalidToken_thenStatus403() throws Exception {
            mockMvc.perform(get(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth("invalid_token")))
                    .andExpect(status().isForbidden());
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
        void whenRequestMissingToken_thenStatus403() throws Exception {
            mockMvc.perform(get(URL, user1))
                    .andExpect(status().isForbidden());
        }

        @Test
        void whenRequestHasInvalidToken_thenStatus403() throws Exception {
            mockMvc.perform(get(URL, user1)
                            .header(HttpHeaders.AUTHORIZATION, auth("invalid_token")))
                    .andExpect(status().isForbidden());
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
        void whenRequestMissingToken_thenStatus403() throws Exception {
            CreateUserDto request = buildCreateUserDto(TestUserCredentials.VALID_USER);

            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        void whenRequestHasInvalidToken_thenStatus403() throws Exception {
            CreateUserDto request = buildCreateUserDto(TestUserCredentials.VALID_USER);

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth("invalid_token"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
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
        void whenRequestMissingToken_thenStatus403() throws Exception {
            UpdateProfileDto request = buildUpdateProfileDto(TestUserProfile.VALID_PROFILE);

            mockMvc.perform(patch(URL, user1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        void whenRequestHasInvalidToken_thenStatus403() throws Exception {
            UpdateProfileDto request = buildUpdateProfileDto(TestUserProfile.VALID_PROFILE);

            mockMvc.perform(patch(URL, user1)
                            .header(HttpHeaders.AUTHORIZATION, auth("invalid_token"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
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
        void whenNewFirstNameTooShort_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserProfile.NEW_FIRSTNAME_TOO_SHORT);
        }

        @Test
        void whenNewFirstNameTooLong_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserProfile.NEW_FIRSTNAME_TOO_LONG);
        }

        @Test
        void whenNewFirstNameWithDigits_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserProfile.NEW_FIRSTNAME_WITH_DIGITS);
        }

        @Test
        void whenNewFirstNameWithSpecialChars_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserProfile.NEW_FIRSTNAME_WITH_SPECIAL_CHARS);
        }

        @Test
        void whenNewFirstNameStartsWithHyphen_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserProfile.NEW_FIRSTNAME_STARTS_WITH_HYPHEN);
        }

        @Test
        void whenNewFirstNameEndsWithHyphen_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserProfile.NEW_FIRSTNAME_ENDS_WITH_HYPHEN);
        }

        @Test
        void whenNewFirstNameDoubleHyphen_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserProfile.NEW_FIRSTNAME_DOUBLE_HYPHEN);
        }

        @Test
        void whenNewFirstNameDoubleSpace_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserProfile.NEW_FIRSTNAME_DOUBLE_SPACE);
        }

        @Test
        void whenNewLastNameTooShort_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserProfile.NEW_LASTNAME_TOO_SHORT);
        }

        @Test
        void whenNewLastNameTooLong_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserProfile.NEW_LASTNAME_TOO_LONG);
        }

        @Test
        void whenNewLastNameWithDigits_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserProfile.NEW_LASTNAME_WITH_DIGITS);
        }

        @Test
        void whenNewLastNameWithSpecialChars_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserProfile.NEW_LASTNAME_WITH_SPECIAL_CHARS);
        }

        @Test
        void whenNewLastNameStartsWithHyphen_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserProfile.NEW_LASTNAME_STARTS_WITH_HYPHEN);
        }

        @Test
        void whenNewLastNameEndsWithHyphen_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserProfile.NEW_LASTNAME_ENDS_WITH_HYPHEN);
        }

        @Test
        void whenNewLastNameDoubleHyphen_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserProfile.NEW_LASTNAME_DOUBLE_HYPHEN);
        }

        @Test
        void whenNewCityTooShort_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserProfile.NEW_CITY_TOO_SHORT);
        }

        @Test
        void whenNewCityTooLong_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserProfile.NEW_CITY_TOO_LONG);
        }

        @Test
        void whenNewCityWithDigits_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserProfile.NEW_CITY_WITH_DIGITS);
        }

        @Test
        void whenNewCityWithSpecialChars_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserProfile.NEW_CITY_WITH_SPECIAL_CHARS);
        }

        @Test
        void whenNewCityStartsWithHyphen_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserProfile.NEW_CITY_STARTS_WITH_HYPHEN);
        }

        @Test
        void whenNewCityEndsWithHyphen_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserProfile.NEW_CITY_ENDS_WITH_HYPHEN);
        }

        @Test
        void whenNewPhoneNumberInvalidFormat_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserProfile.NEW_PHONE_INVALID_FORMAT);
        }

        @Test
        void whenNewPhoneNumberWithLetters_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserProfile.NEW_PHONE_WITH_LETTERS);
        }

        @Test
        void whenNewPhoneNumberTooShort_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserProfile.NEW_PHONE_TOO_SHORT);
        }

        @Test
        void whenNewPhoneNumberTooLong_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserProfile.NEW_PHONE_TOO_LONG);
        }

        @Test
        void whenNewPhoneNumberWrongCountryCode_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserProfile.NEW_PHONE_WRONG_COUNTRY_CODE);
        }

        @Test
        void whenNewPhoneNumberWithSpaces_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserProfile.NEW_PHONE_WITH_SPACES);
        }

        @Test
        void whenNewPhoneNumberWithDashes_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserProfile.NEW_PHONE_WITH_DASHES);
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
        void whenRequestMissingToken_thenStatus403() throws Exception {
            mockMvc.perform(delete(URL, user1))
                    .andExpect(status().isForbidden());
        }

        @Test
        void whenRequestHasInvalidToken_thenStatus403() throws Exception {
            mockMvc.perform(delete(URL, user1)
                            .header(HttpHeaders.AUTHORIZATION, auth("invalid_token")))
                    .andExpect(status().isForbidden());
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
                    profile.newFirstname,
                    profile.newLastname,
                    profile.newCity,
                    profile.newPhoneNumber
            );
        }
    }

    enum TestUserCredentials {
        USER_1("user1@example.com", "Pass123!word"),
        USER_2("user2@example.com", "Pass456!word"),
        USER_3("user3@example.com", "Pass789!word"),

        VALID_USER("newvalid.user@example.com", "Valid123!Pass"),
        VALID_USER_BOUNDARY_MIN("abc@def.gh", "Aa1!bcde"),
        VALID_USER_BOUNDARY_MAX("newuserxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx@example.com", "Valid1!" + "p".repeat(43)),

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

        // Invalid newFirstname - size
        NEW_FIRSTNAME_TOO_SHORT("Аб", "Петренко", "Київ", "+380123456789"),
        NEW_FIRSTNAME_TOO_LONG("А".repeat(256), "Петренко", "Київ", "+380123456789"),

        // Invalid newFirstname - pattern
        NEW_FIRSTNAME_WITH_DIGITS("Іван123", "Петренко", "Київ", "+380123456789"),
        NEW_FIRSTNAME_WITH_SPECIAL_CHARS("Іван@!", "Петренко", "Київ", "+380123456789"),
        NEW_FIRSTNAME_STARTS_WITH_HYPHEN("-Іван", "Петренко", "Київ", "+380123456789"),
        NEW_FIRSTNAME_ENDS_WITH_HYPHEN("Іван-", "Петренко", "Київ", "+380123456789"),
        NEW_FIRSTNAME_DOUBLE_HYPHEN("Іван--Петро", "Петренко", "Київ", "+380123456789"),
        NEW_FIRSTNAME_DOUBLE_SPACE("Іван  Петро", "Петренко", "Київ", "+380123456789"),

        // Invalid newLastname - size
        NEW_LASTNAME_TOO_SHORT("Іван", "Пе", "Київ", "+380123456789"),
        NEW_LASTNAME_TOO_LONG("Іван", "П".repeat(256), "Київ", "+380123456789"),

        // Invalid newLastname - pattern
        NEW_LASTNAME_WITH_DIGITS("Іван", "Петренко123", "Київ", "+380123456789"),
        NEW_LASTNAME_WITH_SPECIAL_CHARS("Іван", "Петренко@#", "Київ", "+380123456789"),
        NEW_LASTNAME_STARTS_WITH_HYPHEN("Іван", "-Петренко", "Київ", "+380123456789"),
        NEW_LASTNAME_ENDS_WITH_HYPHEN("Іван", "Петренко-", "Київ", "+380123456789"),
        NEW_LASTNAME_DOUBLE_HYPHEN("Іван", "Петренко--Іваненко", "Київ", "+380123456789"),

        // Invalid newCity - size
        NEW_CITY_TOO_SHORT("Іван", "Петренко", "Кї", "+380123456789"),
        NEW_CITY_TOO_LONG("Іван", "Петренко", "К".repeat(256), "+380123456789"),

        // Invalid newCity - pattern
        NEW_CITY_WITH_DIGITS("Іван", "Петренко", "Київ123", "+380123456789"),
        NEW_CITY_WITH_SPECIAL_CHARS("Іван", "Петренко", "Київ@#", "+380123456789"),
        NEW_CITY_STARTS_WITH_HYPHEN("Іван", "Петренко", "-Київ", "+380123456789"),
        NEW_CITY_ENDS_WITH_HYPHEN("Іван", "Петренко", "Київ-", "+380123456789"),

        // Invalid newPhoneNumber - pattern
        NEW_PHONE_INVALID_FORMAT("Іван", "Петренко", "Київ", "0501234567"),
        NEW_PHONE_WITH_LETTERS("Іван", "Петренко", "Київ", "+380abc456789"),
        NEW_PHONE_TOO_SHORT("Іван", "Петренко", "Київ", "+38012345678"),
        NEW_PHONE_TOO_LONG("Іван", "Петренко", "Київ", "+3801234567890"),
        NEW_PHONE_WRONG_COUNTRY_CODE("Іван", "Петренко", "Київ", "+1234567890"),
        NEW_PHONE_WITH_SPACES("Іван", "Петренко", "Київ", "+380 12 345 6789"),
        NEW_PHONE_WITH_DASHES("Іван", "Петренко", "Київ", "+380-12-345-6789");

        private final String newFirstname;
        private final String newLastname;
        private final String newCity;
        private final String newPhoneNumber;

        TestUserProfile(String newFirstname, String newLastname, String newCity, String newPhoneNumber) {
            this.newFirstname = newFirstname;
            this.newLastname = newLastname;
            this.newCity = newCity;
            this.newPhoneNumber = newPhoneNumber;
        }
    }
}