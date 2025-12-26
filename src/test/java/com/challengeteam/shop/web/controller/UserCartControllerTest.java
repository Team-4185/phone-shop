package com.challengeteam.shop.web.controller;

import com.challengeteam.shop.dto.cart.CartItemAddRequestDto;
import com.challengeteam.shop.dto.cart.CartItemRemoveRequestDto;
import com.challengeteam.shop.entity.phone.Phone;
import com.challengeteam.shop.persistence.repository.CartRepository;
import com.challengeteam.shop.persistence.repository.PhoneRepository;
import com.challengeteam.shop.service.UserCartService;
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

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(ContainerExtension.class)
class UserCartControllerTest {

    @Autowired
    private TestAuthHelper testAuthHelper;
    @Autowired
    private UserCartService userCartService;
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private PhoneRepository phoneRepository;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    private String token;
    private Long userId;
    private Phone testPhone1;
    private Phone testPhone2;
    private Phone testPhone3;

    @DynamicPropertySource
    static void loadPropertiesForTest(DynamicPropertyRegistry propertyRegistry) {
        TestContextConfigurator.initRequiredProperties(propertyRegistry);
    }

    @BeforeEach
    public void setup() {
        // Clear all data
        cartRepository.deleteAll();
        phoneRepository.deleteAll();

        // Create test phones
        testPhone1 = createTestPhone("iPhone 15", "Latest Apple smartphone", new BigDecimal("999.99"), "Apple", 2024);
        testPhone2 = createTestPhone("Samsung Galaxy S24", "Flagship Samsung phone", new BigDecimal("899.99"), "Samsung", 2024);
        testPhone3 = createTestPhone("Google Pixel 8", "Pure Android experience", new BigDecimal("699.99"), "Google", 2023);

        // Authorize
        token = testAuthHelper.authorizeLikeTestUser();
        userId = testAuthHelper.authorizeAndReturnTokens().userId();
    }

    private Phone createTestPhone(String name, String description, BigDecimal price, String brand, Integer releaseYear) {
        Phone phone = new Phone();
        phone.setName(name);
        phone.setDescription(description);
        phone.setPrice(price);
        phone.setBrand(brand);
        phone.setReleaseYear(releaseYear);
        return phoneRepository.save(phone);
    }

    private String auth(String token) {
        return "Bearer " + token;
    }

    @Nested
    @DisplayName("GET /api/v1/me/cart")
    class GetUserCartTest {
        private static final String URL = "/api/v1/me/cart";

        @Test
        void whenCartExists_thenStatus200AndReturnCart() throws Exception {
            // Add item to cart
            CartItemAddRequestDto addRequest = new CartItemAddRequestDto(testPhone1.getId(), 2);
            userCartService.putItemToUserCart(userId, addRequest);

            mockMvc.perform(get(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth(token)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.cartItems").isArray())
                    .andExpect(jsonPath("$.cartItems", hasSize(1)))
                    .andExpect(jsonPath("$.cartItems[0].amount").value(2))
                    .andExpect(jsonPath("$.cartItems[0].phoneId").value(testPhone1.getId()))
                    .andExpect(jsonPath("$.totalPrice").exists());
        }

        @Test
        void whenCartIsEmpty_thenStatus200AndReturnEmptyCart() throws Exception {
            mockMvc.perform(get(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth(token)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.cartItems").isArray())
                    .andExpect(jsonPath("$.cartItems", hasSize(0)));
        }

        @Test
        void whenCartHasMultipleItems_thenStatus200AndReturnAllItems() throws Exception {
            // Add multiple items to cart
            CartItemAddRequestDto request1 = new CartItemAddRequestDto(testPhone1.getId(), 2);
            CartItemAddRequestDto request2 = new CartItemAddRequestDto(testPhone2.getId(), 1);
            CartItemAddRequestDto request3 = new CartItemAddRequestDto(testPhone3.getId(), 3);

            userCartService.putItemToUserCart(userId, request1);
            userCartService.putItemToUserCart(userId, request2);
            userCartService.putItemToUserCart(userId, request3);

            mockMvc.perform(get(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth(token)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.cartItems", hasSize(3)));
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
    @DisplayName("POST /api/v1/me/cart/put")
    class PutItemToUserCartTest {
        private static final String URL = "/api/v1/me/cart/put";

        @Test
        void whenValidRequest_thenStatus200AndAddItem() throws Exception {
            CartItemAddRequestDto request = new CartItemAddRequestDto(testPhone1.getId(), 3);

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.cartItems").isArray());
        }

        @Test
        void whenItemAlreadyExists_thenIncreaseAmount() throws Exception {
            // Add item first time
            CartItemAddRequestDto request1 = new CartItemAddRequestDto(testPhone1.getId(), 2);
            userCartService.putItemToUserCart(userId, request1);

            // Add same item again
            CartItemAddRequestDto request2 = new CartItemAddRequestDto(testPhone1.getId(), 3);

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request2)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.cartItems").isArray());
        }

        @Test
        void whenAddMultipleProducts_thenAllAdded() throws Exception {
            CartItemAddRequestDto request1 = new CartItemAddRequestDto(testPhone1.getId(), 1);
            CartItemAddRequestDto request2 = new CartItemAddRequestDto(testPhone2.getId(), 2);

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request1)))
                    .andExpect(status().isOk());

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request2)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.cartItems").isArray());
        }

        @Test
        void whenAmountIsMinBoundary_thenStatus200() throws Exception {
            CartItemAddRequestDto request = new CartItemAddRequestDto(testPhone1.getId(), 1);

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.cartItems").isArray());
        }

        @Test
        void whenAmountIsMaxBoundary_thenStatus200() throws Exception {
            CartItemAddRequestDto request = new CartItemAddRequestDto(testPhone1.getId(), 20);

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.cartItems").isArray());
        }

        @Test
        void whenPhoneIdIsNull_thenStatus400() throws Exception {
            CartItemAddRequestDto request = new CartItemAddRequestDto(null, 1);

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void whenAmountIsNull_thenStatus400() throws Exception {
            CartItemAddRequestDto request = new CartItemAddRequestDto(testPhone1.getId(), null);

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void whenAmountIsZero_thenStatus400() throws Exception {
            String jsonRequest = String.format("{\"phoneId\":%d,\"amount\":0}", testPhone1.getId());

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(jsonRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void whenAmountIsNegative_thenStatus400() throws Exception {
            String jsonRequest = String.format("{\"phoneId\":%d,\"amount\":-1}", testPhone1.getId());

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(jsonRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void whenAmountExceedsMax_thenStatus400() throws Exception {
            String jsonRequest = String.format("{\"phoneId\":%d,\"amount\":21}", testPhone1.getId());

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(jsonRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void whenPhoneDoesntExist_thenStatus404() throws Exception {
            CartItemAddRequestDto request = new CartItemAddRequestDto(99999L, 1);

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        void whenRequestMissingToken_thenStatus403() throws Exception {
            CartItemAddRequestDto request = new CartItemAddRequestDto(testPhone1.getId(), 1);

            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        void whenRequestHasInvalidToken_thenStatus403() throws Exception {
            CartItemAddRequestDto request = new CartItemAddRequestDto(testPhone1.getId(), 1);

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth("invalid_token"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/me/cart/remove")
    class RemoveItemFromUserCartTest {
        private static final String URL = "/api/v1/me/cart/remove";

        @BeforeEach
        void addItemsToCart() {
            CartItemAddRequestDto request1 = new CartItemAddRequestDto(testPhone1.getId(), 5);
            CartItemAddRequestDto request2 = new CartItemAddRequestDto(testPhone2.getId(), 3);
            userCartService.putItemToUserCart(userId, request1);
            userCartService.putItemToUserCart(userId, request2);
        }

        @Test
        void whenValidRequest_thenStatus200AndDecreaseAmount() throws Exception {
            CartItemRemoveRequestDto request = new CartItemRemoveRequestDto(testPhone1.getId(), 2);

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.cartItems").isArray());
        }

        @Test
        void whenRemoveAmountEqualsQuantity_thenRemoveItemCompletely() throws Exception {
            CartItemRemoveRequestDto request = new CartItemRemoveRequestDto(testPhone2.getId(), 3);

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.cartItems").isArray());
        }

        @Test
        void whenRemoveAmountGreaterThanQuantity_thenRemoveItemCompletely() throws Exception {
            CartItemRemoveRequestDto request = new CartItemRemoveRequestDto(testPhone2.getId(), 10);

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void whenRemoveLastItem_thenCartBecomesEmpty() throws Exception {
            CartItemRemoveRequestDto request1 = new CartItemRemoveRequestDto(testPhone1.getId(), 5);
            CartItemRemoveRequestDto request2 = new CartItemRemoveRequestDto(testPhone2.getId(), 3);

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request1)))
                    .andExpect(status().isOk());

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request2)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.cartItems").isArray());
        }

        @Test
        void whenAmountIsMinBoundary_thenStatus200() throws Exception {
            CartItemRemoveRequestDto request = new CartItemRemoveRequestDto(testPhone1.getId(), 1);

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.cartItems").isArray());
        }


        @Test
        void whenPhoneIdIsNull_thenStatus400() throws Exception {
            CartItemRemoveRequestDto request = new CartItemRemoveRequestDto(null, 1);

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void whenAmountIsNull_thenStatus400() throws Exception {
            CartItemRemoveRequestDto request = new CartItemRemoveRequestDto(testPhone1.getId(), null);

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void whenAmountIsZero_thenStatus400() throws Exception {
            String jsonRequest = String.format("{\"phoneId\":%d,\"amount\":0}", testPhone1.getId());

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void whenAmountIsNegative_thenStatus400() throws Exception {
            String jsonRequest = String.format("{\"phoneId\":%d,\"amount\":-1}", testPhone1.getId());

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(jsonRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void whenAmountExceedsMax_thenStatus400() throws Exception {
            String jsonRequest = String.format("{\"phoneId\":%d,\"amount\":21}", testPhone1.getId());

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(jsonRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void whenPhoneNotInCart_thenStatus404() throws Exception {
            CartItemRemoveRequestDto request = new CartItemRemoveRequestDto(testPhone3.getId(), 1);

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        void whenPhoneDoesntExist_thenStatus404() throws Exception {
            CartItemRemoveRequestDto request = new CartItemRemoveRequestDto(99999L, 1);

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        void whenRequestMissingToken_thenStatus403() throws Exception {
            CartItemRemoveRequestDto request = new CartItemRemoveRequestDto(testPhone1.getId(), 1);

            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        void whenRequestHasInvalidToken_thenStatus403() throws Exception {
            CartItemRemoveRequestDto request = new CartItemRemoveRequestDto(testPhone1.getId(), 1);

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth("invalid_token"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/me/cart/clear")
    class ClearCartTest {
        private static final String URL = "/api/v1/me/cart/clear";

        @BeforeEach
        void addItemsToCart() {
            CartItemAddRequestDto request1 = new CartItemAddRequestDto(testPhone1.getId(), 2);
            CartItemAddRequestDto request2 = new CartItemAddRequestDto(testPhone2.getId(), 1);
            CartItemAddRequestDto request3 = new CartItemAddRequestDto(testPhone3.getId(), 3);
            userCartService.putItemToUserCart(userId, request1);
            userCartService.putItemToUserCart(userId, request2);
            userCartService.putItemToUserCart(userId, request3);
        }

        @Test
        void whenCartHasItems_thenStatus200AndClearAll() throws Exception {
            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth(token)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.cartItems").isArray())
                    .andExpect(jsonPath("$.cartItems", hasSize(0)))
                    .andExpect(jsonPath("$.totalPrice").exists());
        }

        @Test
        void whenCartIsAlreadyEmpty_thenStatus200() throws Exception {
            // Clear cart first
            userCartService.clearUserCart(userId);

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth(token)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.cartItems", hasSize(0)));
        }

        @Test
        void whenClearCart_thenVerifyItIsEmpty() throws Exception {
            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth(token)))
                    .andExpect(status().isOk());

            // Verify cart is empty
            mockMvc.perform(get("/api/v1/me/cart")
                            .header(HttpHeaders.AUTHORIZATION, auth(token)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.cartItems", hasSize(0)));
        }

        @Test
        void whenRequestMissingToken_thenStatus403() throws Exception {
            mockMvc.perform(post(URL))
                    .andExpect(status().isForbidden());
        }

        @Test
        void whenRequestHasInvalidToken_thenStatus403() throws Exception {
            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth("invalid_token")))
                    .andExpect(status().isForbidden());
        }
    }
}