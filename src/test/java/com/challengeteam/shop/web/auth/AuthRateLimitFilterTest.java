package com.challengeteam.shop.web.auth;

import com.challengeteam.shop.dto.auth.UserLoginRequestDto;
import com.challengeteam.shop.testContainer.ContainerExtension;
import com.challengeteam.shop.testContainer.TestContextConfigurator;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(ContainerExtension.class)
@AutoConfigureMockMvc
@SpringBootTest(properties = {
        "security.rate-limit.requests-per-window=1",
        "security.rate-limit.window=1m"
})
class AuthRateLimitFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @DynamicPropertySource
    static void loadPropertiesForTest(DynamicPropertyRegistry propertyRegistry) {
        TestContextConfigurator.initRequiredProperties(propertyRegistry);
    }

    @Test
    void whenAuthEndpointExceedsLimit_thenReturn429() throws Exception {
        UserLoginRequestDto request = new UserLoginRequestDto(
                "missing.user@valid.com",
                "Password123!",
                false);

        performLogin(request).andExpect(status().isUnauthorized());

        performLogin(request)
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists(HttpHeaders.RETRY_AFTER));
    }

    private org.springframework.test.web.servlet.ResultActions performLogin(
            UserLoginRequestDto request) throws Exception {
        return mockMvc.perform(post("/api/auth/login")
                .header("X-Forwarded-For", "203.0.113.10")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }
}
