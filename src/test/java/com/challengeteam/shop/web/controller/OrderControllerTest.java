package com.challengeteam.shop.web.controller;

import com.challengeteam.shop.dto.payment.TransactionResult;
import com.challengeteam.shop.entity.order.payment.PaymentStatus;
import com.challengeteam.shop.entity.phone.*;
import com.challengeteam.shop.persistence.repository.OrderRepository;
import com.challengeteam.shop.persistence.repository.PhoneRepository;
import com.challengeteam.shop.service.mock.PaymentMockService;
import com.challengeteam.shop.service.notification.EmailNotificationSenderService;
import com.challengeteam.shop.testContainer.ContainerExtension;
import com.challengeteam.shop.testContainer.TestContextConfigurator;
import com.challengeteam.shop.web.TestAuthHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(ContainerExtension.class)
class OrderControllerTest {

    private static final String ORDER_URL = "/api/v1/orders";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private PhoneRepository phoneRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private TestAuthHelper testAuthHelper;

    @MockitoBean
    private PaymentMockService paymentMockService;
    @MockitoBean
    private EmailNotificationSenderService emailNotificationSenderService;

    private Phone iphone;
    private Phone samsung;
    private String userToken;

    @DynamicPropertySource
    static void loadProperties(DynamicPropertyRegistry registry) {
        TestContextConfigurator.initRequiredProperties(registry);
    }

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        phoneRepository.deleteAll();

        iphone = phoneRepository.save(phone("iPhone 15 Pro", "Apple", new BigDecimal("999.00"), 10, ProductStatus.IN_STOCK));
        samsung = phoneRepository.save(phone("Samsung Galaxy S24", "Samsung", new BigDecimal("799.00"), 5, ProductStatus.IN_STOCK));

        userToken = testAuthHelper.authorizeLikeTestUser();
    }

    @AfterEach
    void tearDown() {
        orderRepository.deleteAll();
        phoneRepository.deleteAll();
    }

    private Phone phone(String name, String brand, BigDecimal price, int stock, ProductStatus status) {
        Phone p = new Phone();
        p.setName(name);
        p.setBrand(brand);
        p.setPrice(price);
        p.setStock(stock);
        p.setStatus(status);
        p.setSku(name.replace(" ", "-").toLowerCase());
        p.setReleaseYear(2024);

        p.setPhoneCharacteristics(PhoneCharacteristics.builder()
                .cpu("Apple A17 Pro")
                .coresNumber(6)
                .screenSize("6.1")
                .frontCamera("12MP")
                .mainCamera("48MP")
                .batteryCapacity("3274mAh")
                .phoneColors(Set.of(PhoneColor.WHITE, PhoneColor.GOLD, PhoneColor.BLACK, PhoneColor.SILVER, PhoneColor.YELLOW))
                .storageCapacities(Set.of(StorageCapacity.CAPACITY_64GB, StorageCapacity.CAPACITY_128GB, StorageCapacity.CAPACITY_512GB))
                .build());

        return p;
    }

    private Map<String, Object> cardDetails() {
        return Map.of(
                "cardHoldName", "JOHN DOE",
                "cardNumber", "4111111111111111",
                "cardMonthExpiration", 12,
                "cardYearExpiration", 2026,
                "cardCvv", "123"
        );
    }

    private Map<String, Object> courierAddress() {
        return Map.of(
                "houseNumber", "10A",
                "street", "Main Street",
                "city", "Kyiv",
                "region", "Kyiv Region",
                "country", "Ukraine",
                "zipCode", "01001",
                "logisticsCompany", "NOVA_POSHTA"
        );
    }

    private Map<String, Object> postOfficeAddress() {
        return Map.of(
                "logisticsCompany", "NOVA_POSHTA",
                "logisticPostOffice", "Branch #5",
                "city", "Lviv",
                "region", "Lviv Region",
                "country", "Ukraine",
                "zipCode", "79000"
        );
    }


    private String cardCourierBody(Long phoneId, int quantity) throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "customerEmail", "customer@example.com",
                "customerFirstName", "John",
                "customerLastName", "Doe",
                "customerPhoneNumber", "+380991234567",
                "paymentMethod", "CARD",
                "paymentDetails", cardDetails(),
                "deliveryMethod", "COURIER",
                "shippingAddress", courierAddress(),
                "items", List.of(item(phoneId, quantity))
        ));
    }

    private String cashPickupBody(Long phoneId, int quantity) throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "customerEmail", "customer@example.com",
                "customerFirstName", "John",
                "customerLastName", "Doe",
                "customerPhoneNumber", "+380991234567",
                "paymentMethod", "CASH_ON_DELIVERY",
                "deliveryMethod", "PICKUP",
                "items", List.of(item(phoneId, quantity))
        ));
    }

    private String postOfficeBody(Long phoneId, int quantity) throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "customerEmail", "customer@example.com",
                "customerFirstName", "John",
                "customerLastName", "Doe",
                "customerPhoneNumber", "+380991234567",
                "paymentMethod", "CASH_ON_DELIVERY",
                "deliveryMethod", "POST_OFFICE",
                "shippingAddress", postOfficeAddress(),
                "items", List.of(item(phoneId, quantity))
        ));
    }

    private Map<String, Object> item(Long phoneId, int quantity) {
        return Map.of(
                "phoneId", phoneId,
                "quantity", quantity,
                "color", "BLACK",
                "storage", "CAPACITY_128GB"
        );
    }


    private TransactionResult paid() {
        return new TransactionResult(PaymentStatus.PAID, "tx-123", null);
    }

    private TransactionResult failed() {
        return new TransactionResult(PaymentStatus.FAILED, null, "Insufficient funds");
    }

    @Nested
    @DisplayName("Successful order creation")
            // Cases:
            //   - guest CARD + COURIER → 201, status NEW, payment PAID, transactionId stored
            //   - guest CASH_ON_DELIVERY + PICKUP → 201, payment PENDING
            //   - registered user → 201, user linked in response
            //   - multiple items → total = sum of (price * quantity)
            //   - stock decreases by ordered quantity
            //   - stock = 0 → phone status OUT_OF_STOCK
            //   - stock between 1 and 9 → phone status LOW_STOCK
            //   - POST_OFFICE delivery → 201, logistics company in response
            // -------------------------------------------------------------------------
    class SuccessfulOrderCreationTests {

        @Test
        @DisplayName("Guest CARD + COURIER → 201, status NEW, payment PAID")
        void guestCardCourier_returns201_withNewStatusAndPaidPayment() throws Exception {
            when(paymentMockService.pay(any(), any())).thenReturn(paid());

            mockMvc.perform(post(ORDER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(cardCourierBody(iphone.getId(), 1)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value("NEW"))
                    .andExpect(jsonPath("$.paymentDetails.paymentStatus").value("PAID"))
                    .andExpect(jsonPath("$.paymentDetails.transactionId").value("tx-123"))
                    .andExpect(jsonPath("$.customerEmail").value("customer@example.com"))
                    .andExpect(jsonPath("$.user").doesNotExist());
        }

        @Test
        @DisplayName("Guest CASH_ON_DELIVERY + PICKUP → 201, payment PENDING")
        void guestCashPickup_returns201_withPendingPaymentStatus() throws Exception {
            mockMvc.perform(post(ORDER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(cashPickupBody(iphone.getId(), 1)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.paymentDetails.paymentStatus").value("PENDING"))
                    .andExpect(jsonPath("$.deliveryMethod").value("PICKUP"));
        }

        @Test
        @DisplayName("Registered user → 201, user linked in response")
        void registeredUser_returns201_withLinkedUser() throws Exception {
            when(paymentMockService.pay(any(), any())).thenReturn(paid());

            mockMvc.perform(post(ORDER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + userToken)
                            .content(cardCourierBody(iphone.getId(), 1)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.user").exists())
                    .andExpect(jsonPath("$.user.email").value(TestAuthHelper.TEST_COMPONENT_EMAIL));
        }

        @Test
        @DisplayName("Multiple items → total = 999*1 + 799*2 = 2597")
        void multipleItems_calculatesCorrectTotal() throws Exception {
            when(paymentMockService.pay(any(), any())).thenReturn(paid());

            mockMvc.perform(post(ORDER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of(
                                    "customerEmail", "customer@example.com",
                                    "customerFirstName", "John",
                                    "customerLastName", "Doe",
                                    "customerPhoneNumber", "+380991234567",
                                    "paymentMethod", "CARD",
                                    "paymentDetails", cardDetails(),
                                    "deliveryMethod", "COURIER",
                                    "shippingAddress", courierAddress(),
                                    "items", List.of(
                                            Map.of(
                                                    "phoneId", iphone.getId(),
                                                    "quantity", 1,
                                                    "color", "BLACK",
                                                    "storage", "CAPACITY_128GB"
                                            ),
                                            Map.of(
                                                    "phoneId", samsung.getId(),
                                                    "quantity", 2,
                                                    "color", "BLACK",
                                                    "storage", "CAPACITY_128GB"
                                            )
                                    )
                            ))))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.total").value(2597.00))
                    .andExpect(jsonPath("$.items.length()").value(2));
        }

        @Test
        @DisplayName("Order created → stock decreases by ordered quantity")
        void orderCreated_decreasesPhoneStock() throws Exception {
            when(paymentMockService.pay(any(), any())).thenReturn(paid());

            mockMvc.perform(post(ORDER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(cardCourierBody(iphone.getId(), 3)))
                    .andExpect(status().isCreated());

            assertThat(phoneRepository.findById(iphone.getId())
                    .orElseThrow().getStock()).isEqualTo(7); // 10 - 3
        }

        @Test
        @DisplayName("Stock reaches 0 → phone status OUT_OF_STOCK")
        void stockReachesZero_setsOutOfStockStatus() throws Exception {
            when(paymentMockService.pay(any(), any())).thenReturn(paid());

            mockMvc.perform(post(ORDER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(cardCourierBody(iphone.getId(), 10))) // buy all 10
                    .andExpect(status().isCreated());

            Phone updated = phoneRepository.findById(iphone.getId()).orElseThrow();
            assertThat(updated.getStock()).isZero();
            assertThat(updated.getStatus()).isEqualTo(ProductStatus.OUT_OF_STOCK);
        }

        @Test
        @DisplayName("Remaining stock between 1 and 9 → phone status LOW_STOCK")
        void remainingStockLow_setsLowStockStatus() throws Exception {
            when(paymentMockService.pay(any(), any())).thenReturn(paid());

            mockMvc.perform(post(ORDER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(cardCourierBody(iphone.getId(), 6))) // 10 - 6 = 4
                    .andExpect(status().isCreated());

            assertThat(phoneRepository.findById(iphone.getId())
                    .orElseThrow().getStatus()).isEqualTo(ProductStatus.LOW_STOCK);
        }

        @Test
        @DisplayName("Remaining stock >= 10 → phone status IN_STOCK")
        void remainingStockTenOrMore_setsInStockStatus() throws Exception {
            when(paymentMockService.pay(any(), any())).thenReturn(paid());

            Phone highStockPhone = phoneRepository.save(
                    phone("Google Pixel 9", "Google", new BigDecimal("699.00"), 12, ProductStatus.LOW_STOCK));

            mockMvc.perform(post(ORDER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(cardCourierBody(highStockPhone.getId(), 2))) // 12 - 2 = 10
                    .andExpect(status().isCreated());

            assertThat(phoneRepository.findById(highStockPhone.getId())
                    .orElseThrow().getStatus()).isEqualTo(ProductStatus.IN_STOCK);
        }

        @Test
        @DisplayName("POST_OFFICE delivery → 201, logistics company in response")
        void postOfficeDelivery_returns201_withLogisticsCompany() throws Exception {
            mockMvc.perform(post(ORDER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(postOfficeBody(iphone.getId(), 1)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.deliveryMethod").value("POST_OFFICE"))
                    .andExpect(jsonPath("$.shippingAddress.logisticsCompany").value("NOVA_POSHTA"));
        }
    }

    @Nested
    @DisplayName("Payment failures")
            // Cases:
            //   - payment FAILED → 402, order NOT persisted in DB
            //   - payment FAILED → phone stock unchanged
            // -------------------------------------------------------------------------
    class PaymentFailureTests {

        @Test
        @DisplayName("Payment failed → 402, order not saved in DB")
        void paymentFailed_returns402_orderNotSaved() throws Exception {
            when(paymentMockService.pay(any(), any())).thenReturn(failed());
            long countBefore = orderRepository.count();

            mockMvc.perform(post(ORDER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(cardCourierBody(iphone.getId(), 1)))
                    .andExpect(status().isPaymentRequired());

            assertThat(orderRepository.count()).isEqualTo(countBefore);
        }

        @Test
        @DisplayName("Payment failed → phone stock unchanged")
        void paymentFailed_phoneStockUnchanged() throws Exception {
            when(paymentMockService.pay(any(), any())).thenReturn(failed());

            mockMvc.perform(post(ORDER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(cardCourierBody(iphone.getId(), 1)))
                    .andExpect(status().isPaymentRequired());

            assertThat(phoneRepository.findById(iphone.getId())
                    .orElseThrow().getStock()).isEqualTo(10);
        }
    }

    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("Phone validation errors")
            // Cases:
            //   - phone ID not found → 404
            //   - phone OUT_OF_STOCK → 422
            //   - quantity > available stock → 422
            // -------------------------------------------------------------------------
    class PhoneValidationTests {

        @Test
        @DisplayName("Phone ID not found → 404")
        void phoneNotFound_returns404() throws Exception {
            mockMvc.perform(post(ORDER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(cardCourierBody(99999L, 1)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Phone OUT_OF_STOCK → 422")
        void phoneOutOfStock_returns422() throws Exception {
            Phone outOfStock = phoneRepository.save(
                    phone("OnePlus 12", "OnePlus", new BigDecimal("599.00"), 0, ProductStatus.OUT_OF_STOCK));

            mockMvc.perform(post(ORDER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(cardCourierBody(outOfStock.getId(), 1)))
                    .andExpect(status().isUnprocessableEntity());
        }

        @Test
        @DisplayName("Quantity > available stock → 422")
        void quantityExceedsStock_returns422() throws Exception {
            mockMvc.perform(post(ORDER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(cardCourierBody(iphone.getId(), 999))) // stock is 10
                    .andExpect(status().isUnprocessableEntity());
        }
    }

    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("Request validation errors")
            // Cases:
            //   - empty items list → 400
            //   - invalid email format → 400
            //   - missing customer phone → 400
            //   - CARD without payment details → 400
            //   - CASH_ON_DELIVERY with payment details → 400
            //   - COURIER without shipping address → 400
            //   - PICKUP with shipping address → 400
            // -------------------------------------------------------------------------
    class RequestValidationTests {

        @Test
        @DisplayName("Empty items list → 400")
        void emptyItems_returns400() throws Exception {
            mockMvc.perform(post(ORDER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of(
                                    "customerEmail", "customer@example.com",
                                    "customerFirstName", "John",
                                    "customerLastName", "Doe",
                                    "customerPhoneNumber", "+380991234567",
                                    "paymentMethod", "CASH_ON_DELIVERY",
                                    "deliveryMethod", "PICKUP",
                                    "items", List.of()
                            ))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Invalid email format → 400")
        void invalidEmail_returns400() throws Exception {
            mockMvc.perform(post(ORDER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of(
                                    "customerEmail", "not-an-email",
                                    "customerFirstName", "John",
                                    "customerLastName", "Doe",
                                    "customerPhoneNumber", "+380991234567",
                                    "paymentMethod", "CASH_ON_DELIVERY",
                                    "deliveryMethod", "PICKUP",
                                    "items", List.of(Map.of("phoneId", iphone.getId(), "quantity", 1))
                            ))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Missing customer phone → 400")
        void missingPhone_returns400() throws Exception {
            mockMvc.perform(post(ORDER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of(
                                    "customerEmail", "customer@example.com",
                                    "customerFirstName", "John",
                                    "customerLastName", "Doe",
                                    "paymentMethod", "CASH_ON_DELIVERY",
                                    "deliveryMethod", "PICKUP",
                                    "items", List.of(Map.of("phoneId", iphone.getId(), "quantity", 1))
                            ))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("CARD without payment details → 400")
        void cardWithoutPaymentDetails_returns400() throws Exception {
            mockMvc.perform(post(ORDER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of(
                                    "customerEmail", "customer@example.com",
                                    "customerFirstName", "John",
                                    "customerLastName", "Doe",
                                    "customerPhoneNumber", "+380991234567",
                                    "paymentMethod", "CARD",
                                    "deliveryMethod", "COURIER",
                                    "shippingAddress", courierAddress(),
                                    "items", List.of(Map.of("phoneId", iphone.getId(), "quantity", 1))
                            ))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("CASH_ON_DELIVERY with payment details → 400")
        void cashOnDeliveryWithPaymentDetails_returns400() throws Exception {
            mockMvc.perform(post(ORDER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of(
                                    "customerEmail", "customer@example.com",
                                    "customerFirstName", "John",
                                    "customerLastName", "Doe",
                                    "customerPhoneNumber", "+380991234567",
                                    "paymentMethod", "CASH_ON_DELIVERY",
                                    "paymentDetails", cardDetails(),
                                    "deliveryMethod", "PICKUP",
                                    "items", List.of(Map.of("phoneId", iphone.getId(), "quantity", 1))
                            ))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("COURIER without shipping address → 400")
        void courierWithoutAddress_returns400() throws Exception {
            mockMvc.perform(post(ORDER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of(
                                    "customerEmail", "customer@example.com",
                                    "customerFirstName", "John",
                                    "customerLastName", "Doe",
                                    "customerPhoneNumber", "+380991234567",
                                    "paymentMethod", "CASH_ON_DELIVERY",
                                    "deliveryMethod", "COURIER",
                                    "items", List.of(Map.of("phoneId", iphone.getId(), "quantity", 1))
                            ))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("PICKUP with shipping address → 400")
        void pickupWithShippingAddress_returns400() throws Exception {
            mockMvc.perform(post(ORDER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of(
                                    "customerEmail", "customer@example.com",
                                    "customerFirstName", "John",
                                    "customerLastName", "Doe",
                                    "customerPhoneNumber", "+380991234567",
                                    "paymentMethod", "CASH_ON_DELIVERY",
                                    "deliveryMethod", "PICKUP",
                                    "shippingAddress", courierAddress(),
                                    "items", List.of(Map.of("phoneId", iphone.getId(), "quantity", 1))
                            ))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Missing deliveryMethod with shippingAddress → 400")
        void missingDeliveryMethodWithShippingAddress_returns400() throws Exception {
            mockMvc.perform(post(ORDER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of(
                                    "customerEmail", "customer@example.com",
                                    "customerFirstName", "John",
                                    "customerLastName", "Doe",
                                    "customerPhoneNumber", "+380991234567",
                                    "paymentMethod", "CASH_ON_DELIVERY",
                                    "shippingAddress", courierAddress(),
                                    "items", List.of(Map.of("phoneId", iphone.getId(), "quantity", 1))
                            ))))
                    .andExpect(status().isBadRequest());
        }
    }

    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("GET /api/v1/orders/my — get current user orders")
            // Cases:
            //   - authenticated user with orders → 200, returns only their orders
            //   - authenticated user with no orders → 200, empty page
            //   - unauthenticated request (no token) → 401
            //   - pagination works correctly
            // -------------------------------------------------------------------------
    class GetMyOrdersTests {

        @Test
        @DisplayName("Authenticated user with orders → 200, returns their orders")
        void authenticatedUser_returns200_withTheirOrders() throws Exception {
            when(paymentMockService.pay(any(), any())).thenReturn(paid());

            // create order for the test user
            mockMvc.perform(post(ORDER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + userToken)
                            .content(cardCourierBody(iphone.getId(), 1)))
                    .andExpect(status().isCreated());

            mockMvc.perform(get(ORDER_URL + "/my")
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].customerEmail")
                            .value("customer@example.com"))
                    .andExpect(jsonPath("$.totalElements").value(1));
        }

        @Test
        @DisplayName("Authenticated user with no orders → 200, empty page")
        void authenticatedUser_returns200_withEmptyPage() throws Exception {
            mockMvc.perform(get(ORDER_URL + "/my")
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content", hasSize(0)))
                    .andExpect(jsonPath("$.totalElements").value(0));
        }

        @Test
        @DisplayName("Unauthenticated request → 401")
        void unauthenticatedRequest_returns401() throws Exception {
            mockMvc.perform(get(ORDER_URL + "/my"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Pagination — page size respected")
        void pagination_pageSizeRespected() throws Exception {
            when(paymentMockService.pay(any(), any())).thenReturn(paid());

            // create 2 orders
            mockMvc.perform(post(ORDER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + userToken)
                            .content(cardCourierBody(iphone.getId(), 1)))
                    .andExpect(status().isCreated());

            mockMvc.perform(post(ORDER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + userToken)
                            .content(cardCourierBody(samsung.getId(), 1)))
                    .andExpect(status().isCreated());

            mockMvc.perform(get(ORDER_URL + "/my")
                            .header("Authorization", "Bearer " + userToken)
                            .param("page", "0")
                            .param("size", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.totalElements").value(2))
                    .andExpect(jsonPath("$.totalPages").value(2));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/orders/{id} — get order by ID")
    class GetOrderByIdTests {


        @Test
        @DisplayName("Owner fetches their order → 200, returns order details")
        void ownerFetchesOrder_returns200_withOrderDetails() throws Exception {
            when(paymentMockService.pay(any(), any())).thenReturn(paid());

            String responseBody = mockMvc.perform(post(ORDER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + userToken)
                            .content(cardCourierBody(iphone.getId(), 1)))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();

            Long orderId = objectMapper.readTree(responseBody).get("id").asLong();

            mockMvc.perform(get(ORDER_URL + "/{id}", orderId)
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(orderId))
                    .andExpect(jsonPath("$.status").value("NEW"))
                    .andExpect(jsonPath("$.customerEmail").value("customer@example.com"))
                    .andExpect(jsonPath("$.total").value(999.00));
        }

        @Test
        @DisplayName("Non-existing order ID → 404")
        void nonExistingOrderId_returns404() throws Exception {
            mockMvc.perform(get(ORDER_URL + "/{id}", 99999L)
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("No auth → 401")
        void noAuth_returns401() throws Exception {
            mockMvc.perform(get(ORDER_URL + "/{id}", 1L))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Another user tries to fetch order → 404")
        void anotherUser_cannotFetchOtherUsersOrder() throws Exception {
            when(paymentMockService.pay(any(), any())).thenReturn(paid());

            String responseBody = mockMvc.perform(post(ORDER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + userToken)
                            .content(cardCourierBody(iphone.getId(), 1)))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();

            Long orderId = objectMapper.readTree(responseBody).get("id").asLong();

            String otherToken = testAuthHelper.authorizeAsNewUser("other@gmail.com", "password");

            mockMvc.perform(get(ORDER_URL + "/{id}", orderId)
                            .header("Authorization", "Bearer " + otherToken))
                    .andExpect(status().isNotFound());
        }
    }

}
