package com.challengeteam.shop.web.controller;

import com.challengeteam.shop.dto.auth.UserLoginRequestDto;
import com.challengeteam.shop.dto.user.request.ChangeEmailRequestDto;
import com.challengeteam.shop.dto.user.request.ChangePasswordRequestDto;
import com.challengeteam.shop.persistence.repository.FavoriteRepository;
import com.challengeteam.shop.persistence.repository.OrderRepository;
import com.challengeteam.shop.persistence.repository.PhoneRepository;
import com.challengeteam.shop.persistence.repository.UserRepository;
import com.challengeteam.shop.testContainer.ContainerExtension;
import com.challengeteam.shop.testContainer.TestContextConfigurator;
import com.challengeteam.shop.web.TestAuthHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(ContainerExtension.class)
class UserCredentialsControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private TestAuthHelper testAuthHelper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private FavoriteRepository favoriteRepository;
    @Autowired
    private PhoneRepository phoneRepository;

    @DynamicPropertySource
    static void loadPropertiesForTest(DynamicPropertyRegistry propertyRegistry) {
        TestContextConfigurator.initRequiredProperties(propertyRegistry);
    }

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        favoriteRepository.deleteAll();
        phoneRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Nested
    class ChangePasswordTests {
        private static final String URL = "/api/v1/users/me/password";
        private static final String EMAIL = "password.change@example.com";
        private static final String OLD_PASSWORD = "Password123!";
        private static final String NEW_PASSWORD = "NewPassword123!";

        @Test
        void whenRequestIsValid_thenPasswordChangesAndOldTokenIsRejected() throws Exception {
            String token = testAuthHelper.authorizeAsNewUser(EMAIL, OLD_PASSWORD);
            ChangePasswordRequestDto request = new ChangePasswordRequestDto(
                    OLD_PASSWORD,
                    NEW_PASSWORD,
                    NEW_PASSWORD
            );

            mockMvc.perform(patch(URL)
                            .header(HttpHeaders.AUTHORIZATION, bearer(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(content().string(""));

            mockMvc.perform(get("/api/v1/users/me")
                            .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                    .andExpect(status().isUnauthorized());

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    new UserLoginRequestDto(EMAIL, OLD_PASSWORD, false))))
                    .andExpect(status().isUnauthorized());

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    new UserLoginRequestDto(EMAIL, NEW_PASSWORD, false))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value(EMAIL));
        }

        @Test
        void whenCurrentPasswordIsWrong_thenReturns400() throws Exception {
            String token = testAuthHelper.authorizeAsNewUser(EMAIL, OLD_PASSWORD);
            ChangePasswordRequestDto request = new ChangePasswordRequestDto(
                    "WrongPassword123!",
                    NEW_PASSWORD,
                    NEW_PASSWORD
            );

            mockMvc.perform(patch(URL)
                            .header(HttpHeaders.AUTHORIZATION, bearer(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").value("Current password is incorrect"));
        }

        @Test
        void whenPasswordsDoNotMatch_thenReturns400() throws Exception {
            String token = testAuthHelper.authorizeAsNewUser(EMAIL, OLD_PASSWORD);
            ChangePasswordRequestDto request = new ChangePasswordRequestDto(
                    OLD_PASSWORD,
                    NEW_PASSWORD,
                    "DifferentPassword123!"
            );

            mockMvc.perform(patch(URL)
                            .header(HttpHeaders.AUTHORIZATION, bearer(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").value("New password and confirmation do not match"));
        }

        @Test
        void whenNewPasswordIsSameAsCurrent_thenReturns400() throws Exception {
            String token = testAuthHelper.authorizeAsNewUser(EMAIL, OLD_PASSWORD);
            ChangePasswordRequestDto request = new ChangePasswordRequestDto(
                    OLD_PASSWORD,
                    OLD_PASSWORD,
                    OLD_PASSWORD
            );

            mockMvc.perform(patch(URL)
                            .header(HttpHeaders.AUTHORIZATION, bearer(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").value("New password must differ from current password"));
        }

        @Test
        void whenNewPasswordViolatesPolicy_thenReturns400() throws Exception {
            String token = testAuthHelper.authorizeAsNewUser(EMAIL, OLD_PASSWORD);
            ChangePasswordRequestDto request = new ChangePasswordRequestDto(
                    OLD_PASSWORD,
                    "weakpass",
                    "weakpass"
            );

            mockMvc.perform(patch(URL)
                            .header(HttpHeaders.AUTHORIZATION, bearer(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void whenUserIsUnauthenticated_thenReturns401() throws Exception {
            ChangePasswordRequestDto request = new ChangePasswordRequestDto(
                    OLD_PASSWORD,
                    NEW_PASSWORD,
                    NEW_PASSWORD
            );

            mockMvc.perform(patch(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class ChangeEmailTests {
        private static final String URL = "/api/v1/users/me/email";
        private static final String EMAIL = "email.change@example.com";
        private static final String NEW_EMAIL = "email.changed@example.com";
        private static final String PASSWORD = "Password123!";

        @Test
        void whenRequestIsValid_thenEmailChangesAndOldTokenIsRejected() throws Exception {
            String token = testAuthHelper.authorizeAsNewUser(EMAIL, PASSWORD);
            ChangeEmailRequestDto request = new ChangeEmailRequestDto(NEW_EMAIL, PASSWORD);

            mockMvc.perform(patch(URL)
                            .header(HttpHeaders.AUTHORIZATION, bearer(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(content().string(""));

            mockMvc.perform(get("/api/v1/users/me")
                            .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                    .andExpect(status().isUnauthorized());

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    new UserLoginRequestDto(NEW_EMAIL, PASSWORD, false))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value(NEW_EMAIL));
        }

        @Test
        void whenCurrentPasswordIsWrong_thenReturns400() throws Exception {
            String token = testAuthHelper.authorizeAsNewUser(EMAIL, PASSWORD);
            ChangeEmailRequestDto request = new ChangeEmailRequestDto(NEW_EMAIL, "WrongPassword123!");

            mockMvc.perform(patch(URL)
                            .header(HttpHeaders.AUTHORIZATION, bearer(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").value("Current password is incorrect"));
        }

        @Test
        void whenEmailIsInvalid_thenReturns400() throws Exception {
            String token = testAuthHelper.authorizeAsNewUser(EMAIL, PASSWORD);
            ChangeEmailRequestDto request = new ChangeEmailRequestDto("plaintext", PASSWORD);

            mockMvc.perform(patch(URL)
                            .header(HttpHeaders.AUTHORIZATION, bearer(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void whenEmailIsAlreadyInUse_thenReturns409() throws Exception {
            String token = testAuthHelper.authorizeAsNewUser(EMAIL, PASSWORD);
            testAuthHelper.authorizeAsNewUser(NEW_EMAIL, PASSWORD);
            ChangeEmailRequestDto request = new ChangeEmailRequestDto(NEW_EMAIL, PASSWORD);

            mockMvc.perform(patch(URL)
                            .header(HttpHeaders.AUTHORIZATION, bearer(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.detail").value("Email is already in use"));
        }

        @Test
        void whenEmailIsSameAsCurrent_thenReturns400() throws Exception {
            String token = testAuthHelper.authorizeAsNewUser(EMAIL, PASSWORD);
            ChangeEmailRequestDto request = new ChangeEmailRequestDto(EMAIL, PASSWORD);

            mockMvc.perform(patch(URL)
                            .header(HttpHeaders.AUTHORIZATION, bearer(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").value("New email must differ from current email"));
        }

        @Test
        void whenUserIsUnauthenticated_thenReturns401() throws Exception {
            ChangeEmailRequestDto request = new ChangeEmailRequestDto(NEW_EMAIL, PASSWORD);

            mockMvc.perform(patch(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
