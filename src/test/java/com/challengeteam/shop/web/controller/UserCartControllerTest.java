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

import static org.hamcrest.Matchers.*;
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
        cartRepository.deleteAll();
        phoneRepository.deleteAll();

        testPhone1 = createTestPhone("iPhone 15", "Latest Apple smartphone",
                new BigDecimal("999.99"), "Apple", 2024);
        testPhone2 = createTestPhone("Samsung Galaxy S24", "Flagship Samsung phone",
                new BigDecimal("899.99"), "Samsung", 2024);
        testPhone3 = createTestPhone("Google Pixel 8", "Pure Android experience",
                new BigDecimal("699.99"), "Google", 2023);

        token = testAuthHelper.authorizeLikeTestUser();
        userId = testAuthHelper.authorizeAndReturnTokens().userId();
    }

    private Phone createTestPhone(String name, String description, BigDecimal price,
                                  String brand, Integer releaseYear) {
        Phone phone = new Phone();
        phone.setName(name);
        phone.setDescription(description);
        phone.setPrice(price);
        phone.setBrand(brand);
        phone.setReleaseYear(releaseYear);
        return phoneRepository.save(phone);
    }

    private String authHeader(String token) {
        return "Bearer " + token;
    }

    @Nested
    @DisplayName("GET /api/v1/me/cart - Get User Cart")
    class GetUserCartTest {
        private static final String URL = "/api/v1/me/cart";

        @Test
        @DisplayName("Should return 200 and cart with items when cart exists")
        void whenCartExists_thenStatus200AndReturnCart() throws Exception {
            CartItemAddRequestDto addRequest = new CartItemAddRequestDto(testPhone1.getId(), 2);
            userCartService.putItemToUserCart(userId, addRequest);

            mockMvc.perform(get(URL)
                            .header(HttpHeaders.AUTHORIZATION, authHeader(token)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.totalPrice").exists())
                    .andExpect(jsonPath("$.cartItems").isArray())
                    .andExpect(jsonPath("$.cartItems", hasSize(1)))
                    .andExpect(jsonPath("$.cartItems[0].id").exists())
                    .andExpect(jsonPath("$.cartItems[0].phoneId").value(testPhone1.getId()))
                    .andExpect(jsonPath("$.cartItems[0].amount").value(2));
        }

        @Test
        @DisplayName("Should return 200 and empty cart when cart has no items")
        void whenCartIsEmpty_thenStatus200AndReturnEmptyCart() throws Exception {
            mockMvc.perform(get(URL)
                            .header(HttpHeaders.AUTHORIZATION, authHeader(token)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.totalPrice").exists())
                    .andExpect(jsonPath("$.cartItems").isArray())
                    .andExpect(jsonPath("$.cartItems", hasSize(0)));
        }

        @Test
        @DisplayName("Should return 200 and all items when cart has multiple items")
        void whenCartHasMultipleItems_thenStatus200AndReturnAllItems() throws Exception {
            userCartService.putItemToUserCart(userId, new CartItemAddRequestDto(testPhone1.getId(), 2));
            userCartService.putItemToUserCart(userId, new CartItemAddRequestDto(testPhone2.getId(), 1));
            userCartService.putItemToUserCart(userId, new CartItemAddRequestDto(testPhone3.getId(), 3));

            mockMvc.perform(get(URL)
                            .header(HttpHeaders.AUTHORIZATION, authHeader(token)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.cartItems").isArray())
                    .andExpect(jsonPath("$.cartItems", hasSize(3)))
                    .andExpect(jsonPath("$.totalPrice").exists());
        }

        @Test
        @DisplayName("Should return 403 when authorization token is missing")
        void whenRequestMissingToken_thenStatus403() throws Exception {
            mockMvc.perform(get(URL))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 403 when authorization token is invalid")
        void whenRequestHasInvalidToken_thenStatus403() throws Exception {
            mockMvc.perform(get(URL)
                            .header(HttpHeaders.AUTHORIZATION, authHeader("invalid_token")))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/me/cart/put - Add Item to Cart")
    class PutItemToUserCartTest {
        private static final String URL = "/api/v1/me/cart/put";

        @Test
        @DisplayName("Should return 200 and add item when request is valid")
        void whenValidRequest_thenStatus200AndAddItem() throws Exception {
            CartItemAddRequestDto request = new CartItemAddRequestDto(testPhone1.getId(), 3);

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, authHeader(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.cartItems").isArray())
                    .andExpect(jsonPath("$.cartItems", hasSize(1)))
                    .andExpect(jsonPath("$.cartItems[0].phoneId").value(testPhone1.getId()))
                    .andExpect(jsonPath("$.cartItems[0].amount").value(3));
        }

        @Test
        @DisplayName("Should increase amount when item already exists in cart")
        void whenItemAlreadyExists_thenIncreaseAmount() throws Exception {
            userCartService.putItemToUserCart(userId, new CartItemAddRequestDto(testPhone1.getId(), 2));

            CartItemAddRequestDto request = new CartItemAddRequestDto(testPhone1.getId(), 3);

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, authHeader(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.cartItems").isArray())
                    .andExpect(jsonPath("$.cartItems", hasSize(1)))
                    .andExpect(jsonPath("$.cartItems[0].amount").value(5));
        }

        @Test
        @DisplayName("Should add all items when multiple products are added")
        void whenAddMultipleProducts_thenAllAdded() throws Exception {
            CartItemAddRequestDto request1 = new CartItemAddRequestDto(testPhone1.getId(), 1);
            CartItemAddRequestDto request2 = new CartItemAddRequestDto(testPhone2.getId(), 2);

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, authHeader(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request1)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.cartItems", hasSize(1)));

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, authHeader(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request2)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.cartItems", hasSize(2)));
        }

        @Test
        @DisplayName("Should return 200 when amount is minimum boundary value (1)")
        void whenAmountIsMinBoundary_thenStatus200() throws Exception {
            CartItemAddRequestDto request = new CartItemAddRequestDto(testPhone1.getId(), 1);

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, authHeader(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.cartItems[0].amount").value(1));
        }

        @Test
        @DisplayName("Should return 200 when amount is maximum boundary value (20)")
        void whenAmountIsMaxBoundary_thenStatus200() throws Exception {
            CartItemAddRequestDto request = new CartItemAddRequestDto(testPhone1.getId(), 20);

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, authHeader(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.cartItems[0].amount").value(20));
        }

        @Test
        @DisplayName("Should return 400 when phoneId is null")
        void whenPhoneIdIsNull_thenStatus400() throws Exception {
            String jsonRequest = "{\"phoneId\":null,\"amount\":1}";

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, authHeader(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when amount is null")
        void whenAmountIsNull_thenStatus400() throws Exception {
            String jsonRequest = String.format("{\"phoneId\":%d,\"amount\":null}", testPhone1.getId());

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, authHeader(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when amount is zero")
        void whenAmountIsZero_thenStatus400() throws Exception {
            String jsonRequest = String.format("{\"phoneId\":%d,\"amount\":0}", testPhone1.getId());

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, authHeader(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when amount is negative")
        void whenAmountIsNegative_thenStatus400() throws Exception {
            String jsonRequest = String.format("{\"phoneId\":%d,\"amount\":-1}", testPhone1.getId());

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, authHeader(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when amount exceeds maximum (21)")
        void whenAmountExceedsMax_thenStatus400() throws Exception {
            String jsonRequest = String.format("{\"phoneId\":%d,\"amount\":21}", testPhone1.getId());

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, authHeader(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 404 when phone does not exist")
        void whenPhoneDoesntExist_thenStatus404() throws Exception {
            CartItemAddRequestDto request = new CartItemAddRequestDto(99999L, 1);

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, authHeader(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 403 when authorization token is missing")
        void whenRequestMissingToken_thenStatus403() throws Exception {
            CartItemAddRequestDto request = new CartItemAddRequestDto(testPhone1.getId(), 1);

            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 403 when authorization token is invalid")
        void whenRequestHasInvalidToken_thenStatus403() throws Exception {
            CartItemAddRequestDto request = new CartItemAddRequestDto(testPhone1.getId(), 1);

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, authHeader("invalid_token"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/me/cart/remove - Remove Item from Cart")
    class RemoveItemFromUserCartTest {
        private static final String URL = "/api/v1/me/cart/remove";

        @BeforeEach
        void addItemsToCart() {
            userCartService.putItemToUserCart(userId, new CartItemAddRequestDto(testPhone1.getId(), 5));
            userCartService.putItemToUserCart(userId, new CartItemAddRequestDto(testPhone2.getId(), 3));
        }

        @Test
        @DisplayName("Should return 200 and decrease amount when request is valid")
        void whenValidRequest_thenStatus200AndDecreaseAmount() throws Exception {
            CartItemRemoveRequestDto request = new CartItemRemoveRequestDto(testPhone1.getId(), 2);

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, authHeader(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.cartItems").isArray())
                    .andExpect(jsonPath("$.cartItems", hasSize(2)))
                    .andExpect(jsonPath("$.cartItems[?(@.phoneId == " + testPhone1.getId() + ")].amount").value(3));
        }

        @Test
        @DisplayName("Should remove item completely when remove amount equals quantity")
        void whenRemoveAmountEqualsQuantity_thenRemoveItemCompletely() throws Exception {
            CartItemRemoveRequestDto request = new CartItemRemoveRequestDto(testPhone2.getId(), 3);

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, authHeader(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.cartItems").isArray())
                    .andExpect(jsonPath("$.cartItems", hasSize(1)))
                    .andExpect(jsonPath("$.cartItems[0].phoneId").value(testPhone1.getId()));
        }

        @Test
        @DisplayName("Should have empty cart when last item is removed")
        void whenRemoveLastItem_thenCartBecomesEmpty() throws Exception {
            CartItemRemoveRequestDto request1 = new CartItemRemoveRequestDto(testPhone1.getId(), 5);
            CartItemRemoveRequestDto request2 = new CartItemRemoveRequestDto(testPhone2.getId(), 3);

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, authHeader(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request1)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.cartItems", hasSize(1)));

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, authHeader(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request2)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.cartItems", hasSize(0)));
        }

        @Test
        @DisplayName("Should return 200 when amount is minimum boundary value (1)")
        void whenAmountIsMinBoundary_thenStatus200() throws Exception {
            CartItemRemoveRequestDto request = new CartItemRemoveRequestDto(testPhone1.getId(), 1);

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, authHeader(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.cartItems[?(@.phoneId == " + testPhone1.getId() + ")].amount").value(4));
        }

        @Test
        @DisplayName("Should return 400 when phoneId is null")
        void whenPhoneIdIsNull_thenStatus400() throws Exception {
            String jsonRequest = "{\"phoneId\":null,\"amount\":1}";

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, authHeader(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when amount is null")
        void whenAmountIsNull_thenStatus400() throws Exception {
            String jsonRequest = String.format("{\"phoneId\":%d,\"amount\":null}", testPhone1.getId());

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, authHeader(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when amount is zero")
        void whenAmountIsZero_thenStatus400() throws Exception {
            String jsonRequest = String.format("{\"phoneId\":%d,\"amount\":0}", testPhone1.getId());

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, authHeader(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when amount is negative")
        void whenAmountIsNegative_thenStatus400() throws Exception {
            String jsonRequest = String.format("{\"phoneId\":%d,\"amount\":-1}", testPhone1.getId());

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, authHeader(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when amount exceeds maximum (21)")
        void whenAmountExceedsMax_thenStatus400() throws Exception {
            String jsonRequest = String.format("{\"phoneId\":%d,\"amount\":21}", testPhone1.getId());

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, authHeader(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 404 when phone is not in cart")
        void whenPhoneNotInCart_thenStatus404() throws Exception {
            CartItemRemoveRequestDto request = new CartItemRemoveRequestDto(testPhone3.getId(), 1);

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, authHeader(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 404 when phone does not exist")
        void whenPhoneDoesntExist_thenStatus404() throws Exception {
            CartItemRemoveRequestDto request = new CartItemRemoveRequestDto(99999L, 1);

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, authHeader(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 403 when authorization token is missing")
        void whenRequestMissingToken_thenStatus403() throws Exception {
            CartItemRemoveRequestDto request = new CartItemRemoveRequestDto(testPhone1.getId(), 1);

            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 403 when authorization token is invalid")
        void whenRequestHasInvalidToken_thenStatus403() throws Exception {
            CartItemRemoveRequestDto request = new CartItemRemoveRequestDto(testPhone1.getId(), 1);

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, authHeader("invalid_token"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/me/cart/clear - Clear Cart")
    class ClearCartTest {
        private static final String URL = "/api/v1/me/cart/clear";

        @BeforeEach
        void addItemsToCart() {
            userCartService.putItemToUserCart(userId, new CartItemAddRequestDto(testPhone1.getId(), 2));
            userCartService.putItemToUserCart(userId, new CartItemAddRequestDto(testPhone2.getId(), 1));
            userCartService.putItemToUserCart(userId, new CartItemAddRequestDto(testPhone3.getId(), 3));
        }

        @Test
        @DisplayName("Should return 200 and clear all items when cart has items")
        void whenCartHasItems_thenStatus200AndClearAll() throws Exception {
            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, authHeader(token)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.totalPrice").exists())
                    .andExpect(jsonPath("$.cartItems").isArray())
                    .andExpect(jsonPath("$.cartItems", hasSize(0)));
        }

        @Test
        @DisplayName("Should return 200 when cart is already empty")
        void whenCartIsAlreadyEmpty_thenStatus200() throws Exception {
            userCartService.clearUserCart(userId);

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, authHeader(token)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.cartItems", hasSize(0)));
        }

        @Test
        @DisplayName("Should verify cart is empty after clearing")
        void whenClearCart_thenVerifyItIsEmpty() throws Exception {
            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, authHeader(token)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.cartItems", hasSize(0)));

            mockMvc.perform(get("/api/v1/me/cart")
                            .header(HttpHeaders.AUTHORIZATION, authHeader(token)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.cartItems", hasSize(0)))
                    .andExpect(jsonPath("$.totalPrice").exists());
        }

        @Test
        @DisplayName("Should return 403 when authorization token is missing")
        void whenRequestMissingToken_thenStatus403() throws Exception {
            mockMvc.perform(post(URL))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 403 when authorization token is invalid")
        void whenRequestHasInvalidToken_thenStatus403() throws Exception {
            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, authHeader("invalid_token")))
                    .andExpect(status().isForbidden());
        }
    }
}