package com.challengeteam.shop.web.auth;

import com.challengeteam.shop.dto.security.jwt.JwtResponseDto;
import com.challengeteam.shop.dto.user.request.CreateUserDto;
import com.challengeteam.shop.persistence.repository.UserRepository;
import com.challengeteam.shop.service.UserService;
import com.challengeteam.shop.testContainer.ContainerExtension;
import com.challengeteam.shop.testContainer.TestContextConfigurator;
import com.challengeteam.shop.web.TestAuthHelper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(ContainerExtension.class)
@AutoConfigureMockMvc
@SpringBootTest
@DisplayName("POST /api/v1/logout")
class LogoutControllerTest {

    private static final String URL = "/api/v1/logout";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private TestAuthHelper testAuthHelper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;

    private JwtResponseDto tokens;

    @DynamicPropertySource
    static void loadPropertiesForTest(DynamicPropertyRegistry propertyRegistry) {
        TestContextConfigurator.initRequiredProperties(propertyRegistry);
    }

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        userService.createDefaultUser(new CreateUserDto(
                TestAuthHelper.TEST_COMPONENT_EMAIL,
                TestAuthHelper.TEST_COMPONENT_PASSWORD
        ));
        tokens = testAuthHelper.authorizeAndReturnTokens();
    }

    @Nested
    class SuccessLogout {

        @Test
        void whenValidTokens_thenReturn204AndClearCookie() throws Exception {
            mockMvc.perform(post(URL)
                            .header("Authorization", "Bearer " + tokens.accessToken())
                            .cookie(new Cookie("refreshToken", tokens.refreshToken())))
                    .andExpect(status().isNoContent())
                    .andExpect(cookie().maxAge("refreshToken", 0));
        }

        @Test
        void whenLoggedOut_thenAccessTokenIsRevoked() throws Exception {
            mockMvc.perform(post(URL)
                            .header("Authorization", "Bearer " + tokens.accessToken())
                            .cookie(new Cookie("refreshToken", tokens.refreshToken())))
                    .andExpect(status().isNoContent());

            // повторный запрос с тем же access токеном должен вернуть 401
            mockMvc.perform(post(URL)
                            .header("Authorization", "Bearer " + tokens.accessToken())
                            .cookie(new Cookie("refreshToken", tokens.refreshToken())))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class FailedLogout {

        @Test
        void whenNoAuthorizationHeader_thenReturn401() throws Exception {
            mockMvc.perform(post(URL)
                            .cookie(new Cookie("refreshToken", tokens.refreshToken())))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void whenNoRefreshTokenCookie_thenReturn400() throws Exception {
            mockMvc.perform(post(URL)
                            .header("Authorization", "Bearer " + tokens.accessToken()))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void whenAccessTokenIsInvalid_thenReturn401() throws Exception {
            mockMvc.perform(post(URL)
                            .header("Authorization", "Bearer " + "invalid.access.token")
                            .cookie(new Cookie("refreshToken", tokens.refreshToken())))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void whenRefreshTokenIsInvalid_thenReturn401() throws Exception {
            mockMvc.perform(post(URL)
                            .header("Authorization", "Bearer " + tokens.accessToken())
                            .cookie(new Cookie("refreshToken", "invalid.refresh.token")))
                    .andExpect(status().isUnauthorized());
        }
    }
}