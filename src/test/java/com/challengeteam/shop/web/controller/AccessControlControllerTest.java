package com.challengeteam.shop.web.controller;

import com.challengeteam.shop.testContainer.ContainerExtension;
import com.challengeteam.shop.testContainer.TestContextConfigurator;
import com.challengeteam.shop.web.TestAuthHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(ContainerExtension.class)
class AccessControlControllerTest {

    private static final String TEST_DATA_USERS_URL = "/api/v1/test-data/generate-users/0";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private TestAuthHelper testAuthHelper;

    @DynamicPropertySource
    static void loadPropertiesForTest(DynamicPropertyRegistry propertyRegistry) {
        TestContextConfigurator.initRequiredProperties(propertyRegistry);
    }

    @Test
    void anonymousCannotUseTestDataEndpoint() throws Exception {
        mockMvc.perform(post(TEST_DATA_USERS_URL))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void customerCannotUseTestDataEndpoint() throws Exception {
        mockMvc.perform(post(TEST_DATA_USERS_URL)
                        .header(HttpHeaders.AUTHORIZATION, auth(testAuthHelper.authorizeLikeTestUser())))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanUseTestDataEndpointInNonProdProfile() throws Exception {
        mockMvc.perform(post(TEST_DATA_USERS_URL)
                        .header(HttpHeaders.AUTHORIZATION, auth(testAuthHelper.authorizeAsAdmin("admin.test-data@valid.com"))))
                .andExpect(status().isOk());
    }

    private static String auth(String token) {
        return "Bearer " + token;
    }
}
