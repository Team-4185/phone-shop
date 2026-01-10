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

import static com.challengeteam.shop.web.controller.UserCartControllerTest.TestCartItem.*;
import static com.challengeteam.shop.web.controller.UserCartControllerTest.TestResources.*;
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
    private Long phoneId1;
    private Long phoneId2;
    private Long phoneId3;

    @DynamicPropertySource
    static void loadPropertiesForTest(DynamicPropertyRegistry propertyRegistry) {
        TestContextConfigurator.initRequiredProperties(propertyRegistry);
    }

    @BeforeEach
    public void setup() {
        // clear all
        cartRepository.deleteAll();
        phoneRepository.deleteAll();

        // add 3 phones
        phoneId1 = createPhone(TestPhone.PHONE_1);
        phoneId2 = createPhone(TestPhone.PHONE_2);
        phoneId3 = createPhone(TestPhone.PHONE_3);

        // authorize
        token = testAuthHelper.authorizeLikeTestUser();
        userId = testAuthHelper.authorizeAndReturnTokens().userId();
    }

    private Long createPhone(TestPhone testPhone) {
        Phone phone = new Phone();
        phone.setName(testPhone.name);
        phone.setDescription(testPhone.description);
        phone.setPrice(testPhone.price);
        phone.setBrand(testPhone.brand);
        phone.setReleaseYear(testPhone.releaseYear);
        return phoneRepository.save(phone).getId();
    }

    @Nested
    @DisplayName("GET /api/v1/me/cart")
    class GetUserCartTest {
        private final static String URL = "/api/v1/me/cart";

        @Test
        void whenValidRequest_thenStatus200AndReturnCart() throws Exception {
            userCartService.putItemToUserCart(userId, buildCartItemAddRequestDto(phoneId1, CART_ITEM_1.amount));

            mockMvc.perform(get(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth(token)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.totalPrice").exists())
                    .andExpect(jsonPath("$.totalAmount").exists())
                    .andExpect(jsonPath("$.cartItems").isArray())
                    .andExpect(jsonPath("$.cartItems", hasSize(1)))
                    .andExpect(jsonPath("$.cartItems[0].phoneId").value(phoneId1))
                    .andExpect(jsonPath("$.cartItems[0].amount").value(CART_ITEM_1.amount));
        }

        @Test
        void whenCartIsEmpty_thenReturnEmptyCart() throws Exception {
            mockMvc.perform(get(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth(token)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.totalPrice").exists())
                    .andExpect(jsonPath("$.totalAmount").exists())
                    .andExpect(jsonPath("$.cartItems").isArray())
                    .andExpect(jsonPath("$.cartItems", hasSize(0)));
        }

        @Test
        void whenCartHasMultipleItems_thenReturnAllItems() throws Exception {
            userCartService.putItemToUserCart(userId, buildCartItemAddRequestDto(phoneId1, CART_ITEM_1.amount));
            userCartService.putItemToUserCart(userId, buildCartItemAddRequestDto(phoneId2, CART_ITEM_2.amount));
            userCartService.putItemToUserCart(userId, buildCartItemAddRequestDto(phoneId3, CART_ITEM_3.amount));

            mockMvc.perform(get(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth(token)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalPrice").exists())
                    .andExpect(jsonPath("$.totalAmount").exists())
                    .andExpect(jsonPath("$.cartItems").isArray())
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
                            .header(HttpHeaders.AUTHORIZATION, auth("some_invalid_text")))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/me/cart/put")
    class PutItemToUserCartTest {
        private final static String URL = "/api/v1/me/cart/put";

        @Test
        void whenValidRequest_thenStatus200AndAddItem() throws Exception {
            CartItemAddRequestDto request = buildCartItemAddRequestDto(phoneId1, VALID_CART_ITEM.amount);

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.totalPrice").exists())
                    .andExpect(jsonPath("$.totalAmount").exists())
                    .andExpect(jsonPath("$.cartItems").isArray())
                    .andExpect(jsonPath("$.cartItems", hasSize(1)))
                    .andExpect(jsonPath("$.cartItems[0].phoneId").value(phoneId1))
                    .andExpect(jsonPath("$.cartItems[0].amount").value(VALID_CART_ITEM.amount));
        }

        @Test
        void whenItemAlreadyExists_thenIncreaseAmount() throws Exception {
            userCartService.putItemToUserCart(userId, buildCartItemAddRequestDto(phoneId1, CART_ITEM_1.amount));

            CartItemAddRequestDto request = buildCartItemAddRequestDto(phoneId1, CART_ITEM_2.amount);

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.totalPrice").exists())
                    .andExpect(jsonPath("$.totalAmount").exists())
                    .andExpect(jsonPath("$.cartItems").isArray())
                    .andExpect(jsonPath("$.cartItems", hasSize(1)))
                    .andExpect(jsonPath("$.cartItems[0].amount").value(CART_ITEM_1.amount + CART_ITEM_2.amount));
        }

        @Test
        void whenAddMultipleProducts_thenAllAdded() throws Exception {
            CartItemAddRequestDto request1 = buildCartItemAddRequestDto(phoneId1, CART_ITEM_1.amount);
            CartItemAddRequestDto request2 = buildCartItemAddRequestDto(phoneId2, CART_ITEM_2.amount);

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request1)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.totalPrice").exists())
                    .andExpect(jsonPath("$.totalAmount").exists())
                    .andExpect(jsonPath("$.cartItems", hasSize(1)));

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request2)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.totalPrice").exists())
                    .andExpect(jsonPath("$.totalAmount").exists())
                    .andExpect(jsonPath("$.cartItems", hasSize(2)));
        }

        @Test
        void whenValidRequestWithBoundaryMinValues_thenStatus200() throws Exception {
            CartItemAddRequestDto request = buildCartItemAddRequestDto(phoneId1, VALID_CART_ITEM_BOUNDARY_MIN.amount);

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.totalPrice").exists())
                    .andExpect(jsonPath("$.totalAmount").exists())
                    .andExpect(jsonPath("$.cartItems[0].amount").value(VALID_CART_ITEM_BOUNDARY_MIN.amount));
        }

        @Test
        void whenValidRequestWithBoundaryMaxValues_thenStatus200() throws Exception {
            CartItemAddRequestDto request = buildCartItemAddRequestDto(phoneId1, VALID_CART_ITEM_BOUNDARY_MAX.amount);

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.totalPrice").exists())
                    .andExpect(jsonPath("$.totalAmount").exists())
                    .andExpect(jsonPath("$.cartItems[0].amount").value(VALID_CART_ITEM_BOUNDARY_MAX.amount));
        }

        @Test
        void whenRequestMissingToken_thenStatus403() throws Exception {
            CartItemAddRequestDto request = buildCartItemAddRequestDto(phoneId1, VALID_CART_ITEM.amount);

            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        void whenRequestHasInvalidToken_thenStatus403() throws Exception {
            CartItemAddRequestDto request = buildCartItemAddRequestDto(phoneId1, VALID_CART_ITEM.amount);

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth("some_invalid_text"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        // Validation tests
        @Test
        void whenPhoneIdIsNull_thenStatus400() throws Exception {
            expect400WithInvalidBody(null, VALID_CART_ITEM.amount);
        }

        @Test
        void whenAmountIsNull_thenStatus400() throws Exception {
            expect400WithInvalidBody(phoneId1, INVALID_AMOUNT_NULL.amount);
        }

        @Test
        void whenAmountIsZero_thenStatus400() throws Exception {
            expect400WithInvalidBody(phoneId1, INVALID_AMOUNT_ZERO.amount);
        }

        @Test
        void whenAmountIsNegative_thenStatus400() throws Exception {
            expect400WithInvalidBody(phoneId1, INVALID_AMOUNT_NEGATIVE.amount);
        }

        @Test
        void whenAmountExceedsMax_thenStatus400() throws Exception {
            expect400WithInvalidBody(phoneId1, INVALID_AMOUNT_TOO_HIGH.amount);
        }

        @Test
        void whenPhoneDoesntExist_thenStatus404() throws Exception {
            CartItemAddRequestDto request = new CartItemAddRequestDto(NON_EXISTING_PHONE_ID, 1);

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        private void expect400WithInvalidBody(Long phoneId, Integer amount) throws Exception {
            CartItemAddRequestDto request = buildCartItemAddRequestDto(phoneId, amount);

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

    }

    @Nested
    @DisplayName("POST /api/v1/me/cart/remove")
    class RemoveItemFromUserCartTest {
        private final static String URL = "/api/v1/me/cart/remove";

        @BeforeEach
        void addItemsToCart() {
            userCartService.putItemToUserCart(userId, new CartItemAddRequestDto(phoneId1, 5));
            userCartService.putItemToUserCart(userId, new CartItemAddRequestDto(phoneId2, 3));
        }

        @Test
        void whenValidRequest_thenStatus200AndDecreaseAmount() throws Exception {
            CartItemRemoveRequestDto request = buildCartItemRemoveRequestDto(phoneId1, CART_ITEM_1.amount);

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.totalPrice").exists())
                    .andExpect(jsonPath("$.totalAmount").exists())
                    .andExpect(jsonPath("$.cartItems").isArray())
                    .andExpect(jsonPath("$.cartItems", hasSize(2)))
                    .andExpect(jsonPath("$.cartItems[?(@.phoneId == " + phoneId1 + ")].amount").value(3));
        }

        @Test
        void whenRemoveAmountEqualsQuantity_thenRemoveItemCompletely() throws Exception {
            CartItemRemoveRequestDto request = new CartItemRemoveRequestDto(phoneId2, 3);

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.totalPrice").exists())
                    .andExpect(jsonPath("$.totalAmount").exists())
                    .andExpect(jsonPath("$.cartItems").isArray())
                    .andExpect(jsonPath("$.cartItems", hasSize(1)))
                    .andExpect(jsonPath("$.cartItems[0].phoneId").value(phoneId1));
        }

        @Test
        void whenRemoveLastItem_thenCartBecomesEmpty() throws Exception {
            CartItemRemoveRequestDto request1 = new CartItemRemoveRequestDto(phoneId1, 5);
            CartItemRemoveRequestDto request2 = new CartItemRemoveRequestDto(phoneId2, 3);

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request1)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.totalPrice").exists())
                    .andExpect(jsonPath("$.totalAmount").exists())
                    .andExpect(jsonPath("$.cartItems", hasSize(1)));

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request2)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.totalPrice").exists())
                    .andExpect(jsonPath("$.totalAmount").exists())
                    .andExpect(jsonPath("$.cartItems", hasSize(0)));
        }

        @Test
        void whenValidRequestWithBoundaryMinValues_thenStatus200() throws Exception {
            CartItemRemoveRequestDto request = buildCartItemRemoveRequestDto(phoneId1, VALID_CART_ITEM_BOUNDARY_MIN.amount);

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.totalPrice").exists())
                    .andExpect(jsonPath("$.totalAmount").exists())
                    .andExpect(jsonPath("$.cartItems[?(@.phoneId == " + phoneId1 + ")].amount").value(4));
        }

        @Test
        void whenRequestMissingToken_thenStatus403() throws Exception {
            CartItemRemoveRequestDto request = buildCartItemRemoveRequestDto(phoneId1, VALID_CART_ITEM.amount);

            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        void whenRequestHasInvalidToken_thenStatus403() throws Exception {
            CartItemRemoveRequestDto request = buildCartItemRemoveRequestDto(phoneId1, VALID_CART_ITEM.amount);

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth("some_invalid_text"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        // Validation tests
        @Test
        void whenPhoneIdIsNull_thenStatus400() throws Exception {
            expect400WithInvalidBody(null, VALID_CART_ITEM.amount);
        }

        @Test
        void whenAmountIsNull_thenStatus400() throws Exception {
            expect400WithInvalidBody(phoneId1, INVALID_AMOUNT_NULL.amount);
        }

        @Test
        void whenAmountIsZero_thenStatus400() throws Exception {
            expect400WithInvalidBody(phoneId1, INVALID_AMOUNT_ZERO.amount);
        }

        @Test
        void whenAmountIsNegative_thenStatus400() throws Exception {
            expect400WithInvalidBody(phoneId1, INVALID_AMOUNT_NEGATIVE.amount);
        }

        @Test
        void whenAmountExceedsMax_thenStatus400() throws Exception {
            expect400WithInvalidBody(phoneId1, INVALID_AMOUNT_TOO_HIGH.amount);
        }

        @Test
        void whenPhoneNotInCart_thenStatus404() throws Exception {
            CartItemRemoveRequestDto request = new CartItemRemoveRequestDto(phoneId3, 1);

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        void whenPhoneDoesntExist_thenStatus404() throws Exception {
            CartItemRemoveRequestDto request = new CartItemRemoveRequestDto(NON_EXISTING_PHONE_ID, 1);

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        private void expect400WithInvalidBody(Long phoneId, Integer amount) throws Exception {
            CartItemAddRequestDto request = new CartItemAddRequestDto(phoneId, amount);

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

    }

    @Nested
    @DisplayName("POST /api/v1/me/cart/clear")
    class ClearCartTest {
        private final static String URL = "/api/v1/me/cart/clear";

        @BeforeEach
        void addItemsToCart() {
            userCartService.putItemToUserCart(userId, buildCartItemAddRequestDto(phoneId1, CART_ITEM_1.amount));
            userCartService.putItemToUserCart(userId, buildCartItemAddRequestDto(phoneId2, CART_ITEM_2.amount));
            userCartService.putItemToUserCart(userId, buildCartItemAddRequestDto(phoneId3, CART_ITEM_3.amount));
        }

        @Test
        void whenCartHasItems_thenStatus200AndClearAll() throws Exception {
            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth(token)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.totalPrice").exists())
                    .andExpect(jsonPath("$.totalAmount").exists())
                    .andExpect(jsonPath("$.cartItems").isArray())
                    .andExpect(jsonPath("$.cartItems", hasSize(0)));
        }

        @Test
        void whenCartIsAlreadyEmpty_thenStatus200() throws Exception {
            userCartService.clearUserCart(userId);

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth(token)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.totalPrice").exists())
                    .andExpect(jsonPath("$.totalAmount").exists())
                    .andExpect(jsonPath("$.cartItems", hasSize(0)));
        }

        @Test
        void whenClearCart_thenVerifyItIsEmpty() throws Exception {
            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth(token)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.totalPrice").exists())
                    .andExpect(jsonPath("$.totalAmount").exists())
                    .andExpect(jsonPath("$.cartItems", hasSize(0)));

            mockMvc.perform(get("/api/v1/me/cart")
                            .header(HttpHeaders.AUTHORIZATION, auth(token)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.totalPrice").exists())
                    .andExpect(jsonPath("$.totalAmount").exists())
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
                            .header(HttpHeaders.AUTHORIZATION, auth("some_invalid_text")))
                    .andExpect(status().isForbidden());
        }
    }

    static class TestResources {
        static final long NON_EXISTING_PHONE_ID = 99_999L;

        static String auth(String token) {
            return "Bearer " + token;
        }

        static CartItemAddRequestDto buildCartItemAddRequestDto(Long phoneId, Integer amount) {
            return new CartItemAddRequestDto(phoneId, amount);
        }

        static CartItemRemoveRequestDto buildCartItemRemoveRequestDto(Long phoneId, Integer amount) {
            return new CartItemRemoveRequestDto(phoneId, amount);
        }
    }

    enum TestPhone {
        PHONE_1("iPhone 15", "Latest Apple smartphone", new BigDecimal("999.99"), "Apple", 2024),
        PHONE_2("Samsung Galaxy S24", "Flagship Samsung phone", new BigDecimal("899.99"), "Samsung", 2024),
        PHONE_3("Google Pixel 8", "Pure Android experience", new BigDecimal("699.99"), "Google", 2023);

        final String name;
        final String description;
        final BigDecimal price;
        final String brand;
        final Integer releaseYear;

        TestPhone(String name, String description, BigDecimal price, String brand, Integer releaseYear) {
            this.name = name;
            this.description = description;
            this.price = price;
            this.brand = brand;
            this.releaseYear = releaseYear;
        }
    }

    enum TestCartItem {
        // Valid cart items
        CART_ITEM_1(2),
        CART_ITEM_2(1),
        CART_ITEM_3(3),

        VALID_CART_ITEM(3),
        VALID_CART_ITEM_BOUNDARY_MIN(1),
        VALID_CART_ITEM_BOUNDARY_MAX(20),

        // Invalid cart items
        INVALID_AMOUNT_NULL(null),
        INVALID_AMOUNT_ZERO(0),
        INVALID_AMOUNT_NEGATIVE(-1),
        INVALID_AMOUNT_TOO_HIGH(21);

        final Integer amount;

        TestCartItem(Integer amount) {
            this.amount = amount;
        }
    }
}