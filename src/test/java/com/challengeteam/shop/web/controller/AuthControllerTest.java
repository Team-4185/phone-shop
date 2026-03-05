package com.challengeteam.shop.web.controller;

import com.challengeteam.shop.dto.jwt.JwtRefreshRequestDto;
import com.challengeteam.shop.dto.jwt.JwtResponseDto;
import com.challengeteam.shop.dto.user.CreateUserDto;
import com.challengeteam.shop.dto.user.UserLoginRequestDto;
import com.challengeteam.shop.dto.user.UserRegisterRequestDto;
import com.challengeteam.shop.entity.user.Role;
import com.challengeteam.shop.entity.user.User;
import com.challengeteam.shop.persistence.repository.UserRepository;
import com.challengeteam.shop.properties.JwtProperties;
import com.challengeteam.shop.service.UserService;
import com.challengeteam.shop.service.impl.JwtServiceImpl;
import com.challengeteam.shop.service.impl.UserServiceImpl;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static com.challengeteam.shop.web.controller.AuthControllerTest.TestResources.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(ContainerExtension.class)
@AutoConfigureMockMvc
@SpringBootTest
public class AuthControllerTest {
    @Autowired private TestAuthHelper testAuthHelper;
    @Autowired private MockMvc mockMvc;
    @Autowired private UserService userService;
    @Autowired private UserRepository userRepository;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private JwtProperties jwtProperties;
    private JwtResponseDto token;


    @DynamicPropertySource
    static void loadPropertiesForTest(DynamicPropertyRegistry propertyRegistry) {
        TestContextConfigurator.initRequiredProperties(propertyRegistry);
    }

    @BeforeEach
    public void init() {
        // clean up
        userRepository.deleteAll();

        // init test data
        userService.createDefaultUser(buildCreateUserDto(TestUserCredentials.EXISTING_CREDENTIALS));

        // get token
        token = testAuthHelper.authorizeAndReturnTokens();
    }

    @Nested
    @DisplayName("POST /api/auth/register")
    class RegisterTest {
        private static final String URL = "/api/auth/register";

        @Test
        void whenEmailIsFree_thenRegisterAndReturnToken() throws Exception {
            UserRegisterRequestDto body = buildUserRegisterRequestDto(TestUserCredentials.NOT_EXISTING_CREDENTIALS);

            var request = post(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body))
                    .accept(MediaType.APPLICATION_JSON);

            mockMvc.perform(request)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userId").isNotEmpty())
                    .andExpect(jsonPath("$.email").value(TestUserCredentials.NOT_EXISTING_CREDENTIALS.email))
                    .andExpect(jsonPath("$.accessToken").isNotEmpty())
                    .andExpect(jsonPath("$.refreshToken").isNotEmpty());
        }

        @Test
        void whenPasswordsHaveDifference_thenReturn400() throws Exception {
            UserRegisterRequestDto body = buildUserRegisterRequestDto(TestUserCredentials.PASSWORDS_HAVE_DIFFERENCE);

            var request = post(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body))
                    .accept(MediaType.APPLICATION_JSON);

            mockMvc.perform(request)
                    .andExpect(status().isBadRequest());
        }

        @Test
        void whenEmailIsTaken_thenReturn400() throws Exception {
            UserRegisterRequestDto body = buildUserRegisterRequestDto(TestUserCredentials.EXISTING_CREDENTIALS);

            var request = post(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body))
                    .accept(MediaType.APPLICATION_JSON);

            mockMvc.perform(request)
                    .andExpect(status().isBadRequest());
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
        void whenEmailIsNotValid_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserCredentials.EMAIL_IS_NOT_VALID);
        }

        @Test
        void whenEmailHasWhitespaces_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserCredentials.EMAIL_HAS_WHITESPACES);
        }

        @Test
        void whenEmailIsNotEnglish_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserCredentials.EMAIL_IS_NOT_ENGLISH);
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
        void whenPasswordIsNull_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserCredentials.PASSWORD_IS_NULL);
        }

        @Test
        void whenPasswordIsBlank_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserCredentials.PASSWORD_IS_BLANK);
        }

        @Test
        void whenPasswordHasWhitespaces_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserCredentials.PASSWORD_HAS_WHITESPACES);
        }

        @Test
        void whenPasswordIsNotEnglish_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserCredentials.PASSWORD_IS_NOT_ENGLISH);
        }

        @Test
        void whenPasswordIsMissingCapitalLetter_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserCredentials.PASSWORD_IS_MISSING_CAPITAL_LETTER);
        }

        @Test
        void whenPasswordIsMissingSmallLetter_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserCredentials.PASSWORD_IS_MISSING_SMALL_LETTER);
        }

        @Test
        void whenPasswordIsMissingDigit_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserCredentials.PASSWORD_IS_MISSING_DIGIT);
        }

        @Test
        void whenPasswordIsMissingSpecialSymbol_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserCredentials.PASSWORD_IS_MISSING_SPECIAL_SYMBOL);
        }

        @Test
        void whenPasswordIsTooShort_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserCredentials.PASSWORD_IS_TOO_SHORT);
        }

        @Test
        void whenPasswordIsTooLong_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserCredentials.PASSWORD_IS_TOO_LONG);
        }

        @Test
        void whenConfirmationPasswordIsNull_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserCredentials.CONFIRMATION_PASSWORD_IS_NULL);
        }

        @Test
        void whenConfirmationPasswordIsBlank_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserCredentials.CONFIRMATION_PASSWORD_IS_BLANK);
        }

        @Test
        void whenConfirmationPasswordHasWhitespaces_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserCredentials.CONFIRMATION_PASSWORD_HAS_WHITESPACES);
        }

        @Test
        void whenConfirmationPasswordIsNotEnglish_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserCredentials.CONFIRMATION_PASSWORD_IS_NOT_ENGLISH);
        }

        @Test
        void whenConfirmationPasswordIsMissingCapitalLetter_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserCredentials.CONFIRMATION_PASSWORD_IS_MISSING_CAPITAL_LETTER);
        }

        @Test
        void whenConfirmationPasswordIsMissingSmallLetter_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserCredentials.CONFIRMATION_PASSWORD_IS_MISSING_SMALL_LETTER);
        }

        @Test
        void whenConfirmationPasswordIsMissingDigit_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserCredentials.CONFIRMATION_PASSWORD_IS_MISSING_DIGIT);
        }

        @Test
        void whenConfirmationPasswordIsMissingSpecialSymbol_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserCredentials.CONFIRMATION_PASSWORD_IS_MISSING_SPECIAL_SYMBOL);
        }

        @Test
        void whenConfirmationPasswordIsTooShort_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserCredentials.CONFIRMATION_PASSWORD_IS_TOO_SHORT);
        }

        @Test
        void whenConfirmationPasswordIsTooLong_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserCredentials.CONFIRMATION_PASSWORD_IS_TOO_LONG);
        }

        private void expect400WithInvalidBody(TestUserCredentials credentials) throws Exception {
            UserRegisterRequestDto body = buildUserRegisterRequestDto(credentials);

            var request = post(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body))
                    .accept(MediaType.APPLICATION_JSON);

            mockMvc.perform(request)
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.validationDetails.parameter").value("userRegisterRequestDto"))
                    .andExpect(jsonPath("$.validationDetails.validationProblems").isArray());
        }

    }

    @Nested
    @DisplayName("POST /api/auth/login")
    class LoginTest {
        private static final String URL = "/api/auth/login";

        @Test
        void whenAllRight_thenLoginAndReturnToken() throws Exception {
            UserLoginRequestDto body = buildUserLoginRequestDto(TestUserCredentials.EXISTING_CREDENTIALS);

            var request = post(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body))
                    .accept(MediaType.APPLICATION_JSON);

            mockMvc.perform(request)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userId").isNotEmpty())
                    .andExpect(jsonPath("$.email").value(TestUserCredentials.EXISTING_CREDENTIALS.email))
                    .andExpect(jsonPath("$.accessToken").isNotEmpty())
                    .andExpect(jsonPath("$.refreshToken").isNotEmpty());
        }

        @Test
        void whenEmailIsNotRegistered_thenReturn401() throws Exception {
            UserLoginRequestDto body = buildUserLoginRequestDto(TestUserCredentials.NOT_EXISTING_CREDENTIALS);

            var request = post(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body))
                    .accept(MediaType.APPLICATION_JSON);

            mockMvc.perform(request)
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void whenPasswordsIsWrong_thenReturn401() throws Exception {
            UserLoginRequestDto body = buildUserLoginRequestDto(TestUserCredentials.EXISTING_CREDENTIALS_WITH_WRONG_PASSWORD);

            var request = post(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body))
                    .accept(MediaType.APPLICATION_JSON);

            mockMvc.perform(request)
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
        void whenEmailIsNotValid_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserCredentials.EMAIL_IS_NOT_VALID);
        }

        @Test
        void whenEmailHasWhitespaces_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserCredentials.EMAIL_HAS_WHITESPACES);
        }

        @Test
        void whenEmailIsNotEnglish_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserCredentials.EMAIL_IS_NOT_ENGLISH);
        }

        @Test
        void whenEmailIsTooShort_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserCredentials.EMAIL_IS_TOO_SHORT);
        }

        @Test
        void whenEmailIsTooLong_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserCredentials.EMAIL_IS_TOO_LONG);
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
        void whenPasswordHasWhitespaces_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserCredentials.PASSWORD_HAS_WHITESPACES);
        }

        @Test
        void whenPasswordIsNotEnglish_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserCredentials.PASSWORD_IS_NOT_ENGLISH);
        }

        @Test
        void whenPasswordIsMissingCapitalLetter_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserCredentials.PASSWORD_IS_MISSING_CAPITAL_LETTER);
        }

        @Test
        void whenPasswordIsMissingSmallLetter_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserCredentials.PASSWORD_IS_MISSING_SMALL_LETTER);
        }

        @Test
        void whenPasswordIsMissingDigit_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserCredentials.PASSWORD_IS_MISSING_DIGIT);
        }

        @Test
        void whenPasswordIsMissingSpecialSymbol_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserCredentials.PASSWORD_IS_MISSING_SPECIAL_SYMBOL);
        }

        @Test
        void whenPasswordIsTooShort_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserCredentials.PASSWORD_IS_TOO_SHORT);
        }

        @Test
        void whenPasswordIsTooLong_thenReturn400() throws Exception {
            expect400WithInvalidBody(TestUserCredentials.PASSWORD_IS_TOO_LONG);
        }

        private void expect400WithInvalidBody(TestUserCredentials credentials) throws Exception {
            UserLoginRequestDto body = buildUserLoginRequestDto(credentials);

            var request = post(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body))
                    .accept(MediaType.APPLICATION_JSON);

            mockMvc.perform(request)
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.validationDetails.parameter").value("userLoginRequestDto"))
                    .andExpect(jsonPath("$.validationDetails.validationProblems").isArray());
        }

    }

    @Nested
    @DisplayName("POST /api/auth/refresh-token")
    class RefreshTokenTest {
        private static final String URL = "/api/auth/refresh-token";

        @Test
        void whenRefreshTokenIsValid_thenReturnNewTokens() throws Exception {
            var body = buildJwtRefreshRequestDto(token.refreshToken());

            var request = post(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body))
                    .accept(MediaType.APPLICATION_JSON);

            mockMvc.perform(request)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userId").isNotEmpty())
                    .andExpect(jsonPath("$.email").isNotEmpty())
                    .andExpect(jsonPath("$.accessToken").isNotEmpty())
                    .andExpect(jsonPath("$.refreshToken").isNotEmpty());
        }

        @Test
        void whenRefreshTokenIsExpired_thenReturn401() throws Exception {
            // get new properties with: refresh_token_expiration = 0
            var properties = JwtProperties.copyOf(jwtProperties);
            properties.setRefreshTokenExpiration(0);
            var jwtService = new JwtServiceImpl(properties);
            jwtService.init();

            // create instantly expired refresh token
            var role = Role.builder()
                    .name(UserServiceImpl.DEFAULT_ROLE_NAME_FOR_CREATED_USER)
                    .build();

            var user = User.builder()
                    .email("test@gmail.com")
                    .password("testPassword")
                    .role(role)
                    .build();
            String expiredRefreshToken = jwtService.createRefreshToken(user);

            expect401WhenTokenIsNotRight(expiredRefreshToken);
        }

        @Test
        void whenRefreshTokenHasInvalidSignature_thenReturn401() throws Exception {
            String[] parts = token.refreshToken().split("\\.");
            String invalidSignatureToken = parts[0] + "." + parts[1] + "." + "invalidSignature1234";

            expect401WhenTokenIsNotRight(invalidSignatureToken);
        }

        @Test
        void whenAccessTokenInsteadRefresh_thenReturn401() throws Exception {
            expect401WhenTokenIsNotRight(token.accessToken());
        }

        @Test
        void whenRefreshTokenIsNull_thenReturn400() throws Exception {
            expect400WithInvalidBody(null);
        }

        @Test
        void whenRefreshTokenIsBlank_thenReturn400() throws Exception {
            expect400WithInvalidBody("          ");
        }

        @Test
        void whenRefreshTokenIsInvalid_thenReturn400() throws Exception {
            expect400WithInvalidBody("1. unfortunately it doesn't look like token");
        }

        @Test
        void whenRefreshTokenHasWhitespaces_thenReturn400() throws Exception {
            String[] parts = token.refreshToken().split("\\.");
            expect400WithInvalidBody(String.join(". ", parts));
        }

        @Test
        void whenRefreshTokenHasNotEnglishSymbols_thenReturn400() throws Exception {
            expect400WithInvalidBody(token.refreshToken() + "українськіЛітери");
        }

        private void expect401WhenTokenIsNotRight(String token) throws Exception {
            var body = buildJwtRefreshRequestDto(token);

            var request = post(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body))
                    .accept(MediaType.APPLICATION_JSON);

            mockMvc.perform(request)
                    .andExpect(status().isUnauthorized());
        }

        private void expect400WithInvalidBody(String token) throws Exception {
            var body = buildJwtRefreshRequestDto(token);

            var request = post(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body))
                    .accept(MediaType.APPLICATION_JSON);

            mockMvc.perform(request)
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.validationDetails.parameter").value("jwtRefreshRequestDto"))
                    .andExpect(jsonPath("$.validationDetails.validationProblems").isArray());
        }

    }

    static class TestResources {

        public static CreateUserDto buildCreateUserDto(TestUserCredentials credentials) {
            return new CreateUserDto(
                    credentials.email,
                    credentials.password
            );
        }

        public static UserRegisterRequestDto buildUserRegisterRequestDto(TestUserCredentials credentials) {
            return new UserRegisterRequestDto(
                    credentials.email,
                    credentials.password,
                    credentials.passwordConfirmation
            );
        }

        public static UserLoginRequestDto buildUserLoginRequestDto(TestUserCredentials credentials) {
            return new UserLoginRequestDto(
                    credentials.email,
                    credentials.password
            );
        }

        public static JwtRefreshRequestDto buildJwtRefreshRequestDto(String refreshToken) {
            return new JwtRefreshRequestDto(refreshToken);
        }

    }

    enum TestUserCredentials {
        NOT_EXISTING_CREDENTIALS("unique.email@valid.com", "Password123!", "Password123!"),
        PASSWORDS_HAVE_DIFFERENCE("unique.email@valid.com", "Password123!", "Password1234!"),
        EXISTING_CREDENTIALS("existing.email@valid.com", "Password123!", "Password123!"),
        EXISTING_CREDENTIALS_WITH_WRONG_PASSWORD("existing.email@valid.com", "WrongPassword123!", null),

        // email
        EMAIL_IS_NULL(null, "Password123!", "Password123!"),
        EMAIL_IS_BLANK("               ", "Password123!", "Password123!"),
        EMAIL_IS_NOT_VALID("i_like_ice_cream", "Password123!", "Password123!"),
        EMAIL_HAS_WHITESPACES("unique email@valid.com", "Password123!", "Password123!"),
        EMAIL_IS_NOT_ENGLISH("мояПоштоваСкринька@valid.com", "Password123!", "Password123!"),
        EMAIL_IS_TOO_SHORT("a@ss.ua", "Password123!", "Password123!"),
        EMAIL_IS_TOO_LONG("a".repeat(91) + "@valid.com", "Password123!", "Password123!"),

        // password
        PASSWORD_IS_NULL("unique.email@valid.com", null, "Password123!"),
        PASSWORD_IS_BLANK("unique.email@valid.com", "        ", "Password123!"),
        PASSWORD_HAS_WHITESPACES("unique.email@valid.com", "Password 123!", "Password123!"),
        PASSWORD_IS_NOT_ENGLISH("unique.email@valid.com", "PМійПароль123!", "Password123!"),
        PASSWORD_IS_MISSING_CAPITAL_LETTER("unique.email@valid.com", "password123!", "Password123!"),
        PASSWORD_IS_MISSING_SMALL_LETTER("unique.email@valid.com", "PASSWORD123!", "Password123!"),
        PASSWORD_IS_MISSING_DIGIT("unique.email@valid.com", "Password!", "Password123!"),
        PASSWORD_IS_MISSING_SPECIAL_SYMBOL("unique.email@valid.com", "Password123", "Password123!"),
        PASSWORD_IS_TOO_SHORT("unique.email@valid.com", "Pas123!", "Password123!"),
        PASSWORD_IS_TOO_LONG("unique.email@valid.com", "Password123!" + "a".repeat(90), "Password123!"),

        // confirmation password
        CONFIRMATION_PASSWORD_IS_NULL("unique.email@valid.com", "Password123!", null),
        CONFIRMATION_PASSWORD_IS_BLANK("unique.email@valid.com", "Password123!", "        "),
        CONFIRMATION_PASSWORD_HAS_WHITESPACES("unique.email@valid.com", "Password123!", "Password 123!"),
        CONFIRMATION_PASSWORD_IS_NOT_ENGLISH("unique.email@valid.com", "Password123!", "PМійПароль123!"),
        CONFIRMATION_PASSWORD_IS_MISSING_CAPITAL_LETTER("unique.email@valid.com", "Password123!", "password123!"),
        CONFIRMATION_PASSWORD_IS_MISSING_SMALL_LETTER("unique.email@valid.com", "Password123!", "PASSWORD123!"),
        CONFIRMATION_PASSWORD_IS_MISSING_DIGIT("unique.email@valid.com", "Password123!", "Password!"),
        CONFIRMATION_PASSWORD_IS_MISSING_SPECIAL_SYMBOL("unique.email@valid.com", "Password123!", "Password123"),
        CONFIRMATION_PASSWORD_IS_TOO_SHORT("unique.email@valid.com", "Password123!", "Pas123!"),
        CONFIRMATION_PASSWORD_IS_TOO_LONG("unique.email@valid.com", "Password123!", "Password123!" + "a".repeat(90));

        private final String email;
        private final String password;
        private final String passwordConfirmation;

        TestUserCredentials(String email, String password, String passwordConfirmation) {
            this.email = email;
            this.password = password;
            this.passwordConfirmation = passwordConfirmation;
        }

    }

}
