package com.challengeteam.shop.web.controller;

import com.challengeteam.shop.dto.admin.product.AdminProductCreateRequestDto;
import com.challengeteam.shop.dto.admin.product.AdminProductUpdateRequestDto;
import com.challengeteam.shop.dto.phone.PhoneCreateRequestDto;
import com.challengeteam.shop.entity.image.Image;
import com.challengeteam.shop.entity.order.DeliveryMethod;
import com.challengeteam.shop.entity.order.Order;
import com.challengeteam.shop.entity.order.OrderItem;
import com.challengeteam.shop.entity.order.OrderStatus;
import com.challengeteam.shop.entity.order.payment.PaymentDetails;
import com.challengeteam.shop.entity.order.payment.PaymentMethod;
import com.challengeteam.shop.entity.order.payment.PaymentStatus;
import com.challengeteam.shop.entity.order.shipping.LogisticsCompany;
import com.challengeteam.shop.entity.order.shipping.ShippingAddress;
import com.challengeteam.shop.entity.phone.Phone;
import com.challengeteam.shop.entity.phone.ProductStatus;
import com.challengeteam.shop.entity.user.Role;
import com.challengeteam.shop.entity.user.User;
import com.challengeteam.shop.persistence.repository.*;
import com.challengeteam.shop.service.PhoneService;
import com.challengeteam.shop.service.security.auth.jwt.JwtService;
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
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(ContainerExtension.class)
class AdminControllerTest {
    private static final String ADMIN_ROOT_URL = "/api/v1/admin";
    private static final String ADMIN_PRODUCTS_URL = "/api/v1/admin/products";
    private static final String ADMIN_ORDERS_URL = "/api/v1/admin/orders";
    private static final String ADMIN_EMAIL = "admin.test@valid.com";
    private static final String ADMIN_PASSWORD = "AdminPassword123!";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestAuthHelper testAuthHelper;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PhoneRepository phoneRepository;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PhoneService phoneService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String userToken;
    private String adminToken;

    @DynamicPropertySource
    static void loadPropertiesForTest(DynamicPropertyRegistry propertyRegistry) {
        TestContextConfigurator.initRequiredProperties(propertyRegistry);
    }

    @BeforeEach
    void setup() {
        orderRepository.deleteAll();
        imageRepository.deleteAll();
        phoneRepository.deleteAll();
        userRepository.deleteAll();

        userToken = testAuthHelper.authorizeLikeTestUser();
        adminToken = createAdminAccessToken();

        phoneService.create(buildAdminTestPhoneCreateRequestDto(), new ArrayList<>());
    }

    @Nested
    @DisplayName("Admin product mutations")
    class AdminProductMutationsTest {

        @Test
        void whenAdminCreatesProductWithImage_thenStatus201AndProductCanBeFetched() throws Exception {
            AdminProductCreateRequestDto request =
                    buildAdminProductCreateRequestDto("Created Admin Product", "CREATED-ADMIN-001");

            mockMvc
                    .perform(
                            multipart(ADMIN_PRODUCTS_URL)
                                    .file(jsonPart("product", request))
                                    .file(imagePart("images", "image_1.jpg"))
                                    .header(HttpHeaders.AUTHORIZATION, auth(adminToken)))
                    .andExpect(status().isCreated())
                    .andExpect(header().string(HttpHeaders.LOCATION, containsString(ADMIN_PRODUCTS_URL)));

            Phone created = findPhoneBySku("CREATED-ADMIN-001");

            mockMvc
                    .perform(
                            get(ADMIN_PRODUCTS_URL + "/{id}", created.getId())
                                    .header(HttpHeaders.AUTHORIZATION, auth(adminToken)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Created Admin Product"))
                    .andExpect(jsonPath("$.sku").value("CREATED-ADMIN-001"))
                    .andExpect(jsonPath("$.images", hasSize(1)));
        }

        @Test
        void whenAdminUpdatesProduct_thenStatus204AndDetailsReflectChanges() throws Exception {
            Phone phone = phoneRepository.findAll().getFirst();
            AdminProductUpdateRequestDto request =
                    new AdminProductUpdateRequestDto(
                            "Updated Admin Phone",
                            "Updated admin description",
                            new BigDecimal("899.99"),
                            "UpdatedBrand",
                            2025,
                            "UPDATED-ADMIN-001",
                            7,
                            ProductStatus.LOW_STOCK,
                            "Updated Chip",
                            10,
                            "6.8\"",
                            "16 MP",
                            "108 MP",
                            "5000 mAh");

            mockMvc
                    .perform(
                            put(ADMIN_PRODUCTS_URL + "/{id}", phone.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsBytes(request))
                                    .header(HttpHeaders.AUTHORIZATION, auth(adminToken)))
                    .andExpect(status().isNoContent());

            mockMvc
                    .perform(
                            get(ADMIN_PRODUCTS_URL + "/{id}", phone.getId())
                                    .header(HttpHeaders.AUTHORIZATION, auth(adminToken)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Updated Admin Phone"))
                    .andExpect(jsonPath("$.sku").value("UPDATED-ADMIN-001"))
                    .andExpect(jsonPath("$.brand").value("UpdatedBrand"))
                    .andExpect(jsonPath("$.stock").value(7))
                    .andExpect(jsonPath("$.status").value("LOW_STOCK"))
                    .andExpect(jsonPath("$.cpu").value("Updated Chip"));
        }

        @Test
        void whenAdminDeletesProduct_thenStatus204AndProductIsGone() throws Exception {
            Phone phone = phoneRepository.findAll().getFirst();

            mockMvc
                    .perform(
                            delete(ADMIN_PRODUCTS_URL + "/{id}", phone.getId())
                                    .header(HttpHeaders.AUTHORIZATION, auth(adminToken)))
                    .andExpect(status().isNoContent());

            mockMvc
                    .perform(
                            get(ADMIN_PRODUCTS_URL + "/{id}", phone.getId())
                                    .header(HttpHeaders.AUTHORIZATION, auth(adminToken)))
                    .andExpect(status().isNotFound());
        }

        @Test
        void whenAdminAddsProductImage_thenImageIsVisibleInAdminImagesEndpoint() throws Exception {
            Phone phone = phoneRepository.findAll().getFirst();

            mockMvc
                    .perform(
                            multipart(ADMIN_PRODUCTS_URL + "/{id}/images", phone.getId())
                                    .file(imagePart("images", "image_1.jpg"))
                                    .header(HttpHeaders.AUTHORIZATION, auth(adminToken)))
                    .andExpect(status().isNoContent());

            mockMvc
                    .perform(
                            get(ADMIN_PRODUCTS_URL + "/{id}/images", phone.getId())
                                    .header(HttpHeaders.AUTHORIZATION, auth(adminToken)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].name").value("image_1.jpg"));
        }

        @Test
        void whenAdminDeletesProductImage_thenImageIsRemovedFromProduct() throws Exception {
            Phone phone = phoneRepository.findAll().getFirst();
            mockMvc
                    .perform(
                            multipart(ADMIN_PRODUCTS_URL + "/{id}/images", phone.getId())
                                    .file(imagePart("images", "image_1.jpg"))
                                    .header(HttpHeaders.AUTHORIZATION, auth(adminToken)))
                    .andExpect(status().isNoContent());
            Image image = imageRepository.getImagesByPhone_Id(phone.getId()).getFirst();

            mockMvc
                    .perform(
                            delete(ADMIN_PRODUCTS_URL + "/{id}/images/{imageId}", phone.getId(), image.getId())
                                    .header(HttpHeaders.AUTHORIZATION, auth(adminToken)))
                    .andExpect(status().isNoContent());

            mockMvc
                    .perform(
                            get(ADMIN_PRODUCTS_URL + "/{id}/images", phone.getId())
                                    .header(HttpHeaders.AUTHORIZATION, auth(adminToken)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/admin")
    class GetAdminEntryPointTest {

        @Test
        void whenRequestMissingToken_thenStatus401() throws Exception {
            mockMvc.perform(get(ADMIN_ROOT_URL)).andExpect(status().isUnauthorized());
        }

        @Test
        void whenAuthenticatedUserHasNoAdminRole_thenStatus403() throws Exception {
            mockMvc
                    .perform(get(ADMIN_ROOT_URL).header(HttpHeaders.AUTHORIZATION, auth(userToken)))
                    .andExpect(status().isForbidden());
        }

        @Test
        void whenAuthenticatedUserIsAdmin_thenStatus200AndReturnSections() throws Exception {
            mockMvc
                    .perform(get(ADMIN_ROOT_URL).header(HttpHeaders.AUTHORIZATION, auth(adminToken)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.sections").isArray())
                    .andExpect(jsonPath("$.sections", hasSize(2)))
                    .andExpect(jsonPath("$.sections[0].name").value("products"))
                    .andExpect(jsonPath("$.sections[0].path").value("/api/v1/admin/products"))
                    .andExpect(jsonPath("$.sections[0].implemented").value(true))
                    .andExpect(jsonPath("$.sections[1].name").value("orders"))
                    .andExpect(jsonPath("$.sections[1].path").value("/api/v1/admin/orders"))
                    .andExpect(jsonPath("$.sections[1].implemented").value(true));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/admin/products")
    class GetAdminProductsTest {

        @Test
        void whenAuthenticatedUserHasNoAdminRole_thenStatus403() throws Exception {
            mockMvc
                    .perform(get(ADMIN_PRODUCTS_URL).header(HttpHeaders.AUTHORIZATION, auth(userToken)))
                    .andExpect(status().isForbidden());
        }

        @Test
        void whenAuthenticatedUserIsAdmin_thenStatus200AndReturnAdminProductList() throws Exception {
            mockMvc
                    .perform(get(ADMIN_PRODUCTS_URL).header(HttpHeaders.AUTHORIZATION, auth(adminToken)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].id").isNumber())
                    .andExpect(jsonPath("$.content[0].name").value("Admin Test Phone"))
                    .andExpect(jsonPath("$.content[0].sku").value("ADMIN-TEST-001"))
                    .andExpect(jsonPath("$.content[0].brand").value("AdminBrand"))
                    .andExpect(jsonPath("$.content[0].price").value(799.99))
                    .andExpect(jsonPath("$.content[0].stock").value(45))
                    .andExpect(jsonPath("$.content[0].status").value("IN_STOCK"))
                    .andExpect(jsonPath("$.content[0].previewImage").doesNotExist());
        }

        @Test
        void whenSearchMatchesProduct_thenReturnFilteredList() throws Exception {
            phoneService.create(buildOtherPhoneCreateRequestDto(), new ArrayList<>());

            mockMvc
                    .perform(
                            get(ADMIN_PRODUCTS_URL)
                                    .param("search", "admin")
                                    .header(HttpHeaders.AUTHORIZATION, auth(adminToken)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].name").value("Admin Test Phone"));
        }

        @Test
        void whenBrandFilterMatchesProduct_thenReturnFilteredList() throws Exception {
            phoneService.create(buildOtherPhoneCreateRequestDto(), new ArrayList<>());

            mockMvc
                    .perform(
                            get(ADMIN_PRODUCTS_URL)
                                    .param("brand", "AdminBrand")
                                    .header(HttpHeaders.AUTHORIZATION, auth(adminToken)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].brand").value("AdminBrand"));
        }

        @Test
        void whenSortByPriceDesc_thenReturnProductsInSortedOrder() throws Exception {
            phoneService.create(buildMoreExpensivePhoneCreateRequestDto(), new ArrayList<>());

            mockMvc
                    .perform(
                            get(ADMIN_PRODUCTS_URL)
                                    .param("sort", "price_desc")
                                    .header(HttpHeaders.AUTHORIZATION, auth(adminToken)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content", hasSize(2)))
                    .andExpect(jsonPath("$.content[0].name").value("Premium Test Phone"))
                    .andExpect(jsonPath("$.content[0].price").value(1499.99))
                    .andExpect(jsonPath("$.content[1].name").value("Admin Test Phone"))
                    .andExpect(jsonPath("$.content[1].price").value(799.99));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/admin/products/{id}")
    class GetAdminProductDetailsTest {

        @Test
        void whenProductExists_thenReturnFullAdminDetails() throws Exception {
            Phone phone = phoneRepository.findAll().getFirst();

            mockMvc
                    .perform(
                            get(ADMIN_PRODUCTS_URL + "/{id}", phone.getId())
                                    .header(HttpHeaders.AUTHORIZATION, auth(adminToken)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(phone.getId()))
                    .andExpect(jsonPath("$.name").value("Admin Test Phone"))
                    .andExpect(jsonPath("$.sku").value("ADMIN-TEST-001"))
                    .andExpect(jsonPath("$.description").value("Phone prepared for admin controller tests"))
                    .andExpect(jsonPath("$.brand").value("AdminBrand"))
                    .andExpect(jsonPath("$.price").value(799.99))
                    .andExpect(jsonPath("$.releaseYear").value(2024))
                    .andExpect(jsonPath("$.stock").value(45))
                    .andExpect(jsonPath("$.status").value("IN_STOCK"))
                    .andExpect(jsonPath("$.cpu").value("Admin Chip"))
                    .andExpect(jsonPath("$.coresNumber").value(8))
                    .andExpect(jsonPath("$.screenSize").value("6.5\""))
                    .andExpect(jsonPath("$.frontCamera").value("12 MP"))
                    .andExpect(jsonPath("$.mainCamera").value("50 MP"))
                    .andExpect(jsonPath("$.batteryCapacity").value("4500 mAh"))
                    .andExpect(jsonPath("$.images").isArray())
                    .andExpect(jsonPath("$.images", hasSize(0)));
        }

        @Test
        void whenProductDoesNotExist_thenReturn404() throws Exception {
            mockMvc
                    .perform(
                            get(ADMIN_PRODUCTS_URL + "/{id}", 999999L)
                                    .header(HttpHeaders.AUTHORIZATION, auth(adminToken)))
                    .andExpect(status().isNotFound());
        }

        @Test
        void whenAuthenticatedUserHasNoAdminRole_thenStatus403() throws Exception {
            Phone phone = phoneRepository.findAll().getFirst();

            mockMvc
                    .perform(
                            get(ADMIN_PRODUCTS_URL + "/{id}", phone.getId())
                                    .header(HttpHeaders.AUTHORIZATION, auth(userToken)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/admin/orders")
    class GetAdminOrdersTest {

        @Test
        void whenAuthenticatedUserHasNoAdminRole_thenStatus403() throws Exception {
            mockMvc
                    .perform(get(ADMIN_ORDERS_URL).header(HttpHeaders.AUTHORIZATION, auth(userToken)))
                    .andExpect(status().isForbidden());
        }

        @Test
        void whenAuthenticatedUserIsAdmin_thenStatus200AndReturnAdminOrderList() throws Exception {
            createOrder(OrderStatus.NEW, "2499.98", 2);

            mockMvc
                    .perform(get(ADMIN_ORDERS_URL).header(HttpHeaders.AUTHORIZATION, auth(adminToken)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].id").isNumber())
                    .andExpect(jsonPath("$.content[0].customerEmail").exists())
                    .andExpect(jsonPath("$.content[0].status").value("NEW"))
                    .andExpect(jsonPath("$.content[0].paymentMethod").value("CARD"))
                    .andExpect(jsonPath("$.content[0].paymentStatus").value("PENDING"))
                    .andExpect(jsonPath("$.content[0].deliveryMethod").value("COURIER"))
                    .andExpect(jsonPath("$.content[0].total").value(2499.98))
                    .andExpect(jsonPath("$.content[0].itemsCount").value(2));
        }

        @Test
        void whenStatusFilterMatchesOrder_thenReturnFilteredList() throws Exception {
            createOrder(OrderStatus.NEW, "799.99", 1);
            createOrder(OrderStatus.SHIPPED, "1499.99", 1);

            mockMvc
                    .perform(
                            get(ADMIN_ORDERS_URL)
                                    .param("status", "SHIPPED")
                                    .header(HttpHeaders.AUTHORIZATION, auth(adminToken)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].status").value("SHIPPED"))
                    .andExpect(jsonPath("$.content[0].total").value(1499.99));
        }

        @Test
        void whenPaymentStatusFilterMatchesOrder_thenReturnFilteredList() throws Exception {
            createOrder(OrderStatus.NEW, PaymentStatus.PENDING, "799.99", 1);
            createOrder(OrderStatus.CONFIRMED, PaymentStatus.PAID, "1499.99", 1);

            mockMvc
                    .perform(
                            get(ADMIN_ORDERS_URL)
                                    .param("paymentStatus", "PAID")
                                    .header(HttpHeaders.AUTHORIZATION, auth(adminToken)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].paymentStatus").value("PAID"))
                    .andExpect(jsonPath("$.content[0].total").value(1499.99));
        }

        @Test
        void whenSortByTotalDesc_thenReturnOrdersInSortedOrder() throws Exception {
            createOrder(OrderStatus.NEW, "799.99", 1);
            createOrder(OrderStatus.CONFIRMED, "1499.99", 1);

            mockMvc
                    .perform(
                            get(ADMIN_ORDERS_URL)
                                    .param("sort", "total_desc")
                                    .header(HttpHeaders.AUTHORIZATION, auth(adminToken)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(2)))
                    .andExpect(jsonPath("$.content[0].total").value(1499.99))
                    .andExpect(jsonPath("$.content[1].total").value(799.99));
        }

        @Test
        void whenTotalRangeIsReversed_thenStatus400() throws Exception {
            mockMvc
                    .perform(
                            get(ADMIN_ORDERS_URL)
                                    .param("minTotal", "1000")
                                    .param("maxTotal", "100")
                                    .header(HttpHeaders.AUTHORIZATION, auth(adminToken)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/admin/orders/{id}")
    class GetAdminOrderDetailsTest {

        @Test
        void whenOrderExists_thenReturnFullAdminDetails() throws Exception {
            Order order = createOrder(OrderStatus.NEW, "2499.98", 2);

            mockMvc
                    .perform(
                            get(ADMIN_ORDERS_URL + "/{id}", order.getId())
                                    .header(HttpHeaders.AUTHORIZATION, auth(adminToken)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(order.getId()))
                    .andExpect(jsonPath("$.customerEmail").exists())
                    .andExpect(jsonPath("$.status").value("NEW"))
                    .andExpect(jsonPath("$.paymentMethod").value("CARD"))
                    .andExpect(jsonPath("$.paymentStatus").value("PENDING"))
                    .andExpect(jsonPath("$.deliveryMethod").value("COURIER"))
                    .andExpect(jsonPath("$.total").value(2499.98))
                    .andExpect(jsonPath("$.availableActions", hasSize(2)))
                    .andExpect(jsonPath("$.items", hasSize(1)))
                    .andExpect(jsonPath("$.items[0].productName").value("Admin Test Phone"))
                    .andExpect(jsonPath("$.items[0].sku").value("ADMIN-TEST-001"))
                    .andExpect(jsonPath("$.items[0].quantity").value(2));
        }

        @Test
        void whenOrderDoesNotExist_thenReturn404() throws Exception {
            mockMvc
                    .perform(
                            get(ADMIN_ORDERS_URL + "/{id}", 999999L)
                                    .header(HttpHeaders.AUTHORIZATION, auth(adminToken)))
                    .andExpect(status().isNotFound());
        }
        @Test
        void whenPickupOrderExists_thenReturnDetailsWithNullCustomerCity() throws Exception {
            Order order = createPickupOrder(OrderStatus.NEW, "799.99", 1);

            mockMvc
                    .perform(
                            get(ADMIN_ORDERS_URL + "/{id}", order.getId())
                                    .header(HttpHeaders.AUTHORIZATION, auth(adminToken)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(order.getId()))
                    .andExpect(jsonPath("$.customerEmail").exists())
                    .andExpect(jsonPath("$.status").value("NEW"))
                    .andExpect(jsonPath("$.deliveryMethod").value("PICKUP"))
                    .andExpect(jsonPath("$.customerCity").doesNotExist());
        }
        private Order createPickupOrder(OrderStatus status, String total, int quantity) {
            Phone phone = findPhoneBySku("ADMIN-TEST-001");
            User customer =
                    userRepository.findAll().stream()
                            .filter(user -> !ADMIN_EMAIL.equals(user.getEmail()))
                            .findFirst()
                            .orElseThrow();

            Order order =
                    Order.builder()
                            .user(customer)
                            .customerEmail(customer.getEmail())
                            .customerFirstName(customer.getFirstName())
                            .customerLastName(customer.getLastName())
                            .customerPhoneNumber(customer.getPhoneNumber())
                            .status(status)
                            .paymentMethod(PaymentMethod.CARD)
                            .paymentDetails(
                                    new PaymentDetails(PaymentStatus.PENDING,
                                            UUID.randomUUID().toString()))
                            .deliveryMethod(DeliveryMethod.PICKUP)
                            .shippingAddress(null)
                            .total(new BigDecimal(total))
                            .build();
            order.addItem(
                    OrderItem.builder()
                            .phone(phone)
                            .productName(phone.getName())
                            .sku(phone.getSku())
                            .unitPrice(phone.getPrice())
                            .quantity(quantity)
                            .totalPrice(phone.getPrice().multiply(BigDecimal.valueOf(quantity)))
                            .build());

            return orderRepository.save(order);
        }
    }

    @Nested
    @DisplayName("POST /api/v1/admin/orders/{id}/actions")
    class AdminOrderActionsTest {

        @Test
        void whenConfirmNewOrder_thenStatusChangesToConfirmed() throws Exception {
            Order order = createOrder(OrderStatus.NEW, "799.99", 1);

            mockMvc
                    .perform(
                            post(ADMIN_ORDERS_URL + "/{id}/confirm", order.getId())
                                    .header(HttpHeaders.AUTHORIZATION, auth(adminToken)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("CONFIRMED"))
                    .andExpect(jsonPath("$.availableActions", hasSize(2)));
        }

        @Test
        void whenUnsupportedActionForCurrentStatus_thenStatus400() throws Exception {
            Order order = createOrder(OrderStatus.NEW, "799.99", 1);

            mockMvc
                    .perform(
                            post(ADMIN_ORDERS_URL + "/{id}/deliver", order.getId())
                                    .header(HttpHeaders.AUTHORIZATION, auth(adminToken)))
                    .andExpect(status().isBadRequest());
        }
    }

    private String createAdminAccessToken() {
        Role adminRole =
                roleRepository
                        .findByName("ADMIN")
                        .orElseThrow(() -> new IllegalStateException("Admin role is missing in test database"));

        User adminUser =
                User.builder()
                        .email(ADMIN_EMAIL)
                        .password(passwordEncoder.encode(ADMIN_PASSWORD))
                        .role(adminRole)
                        .build();

        User savedAdmin = userRepository.save(adminUser);
        return jwtService.createAccessToken(savedAdmin);
    }

    private static String auth(String token) {
        return "Bearer " + token;
    }

    private static PhoneCreateRequestDto buildAdminTestPhoneCreateRequestDto() {
        return new PhoneCreateRequestDto(
                "Admin Test Phone",
                "Phone prepared for admin controller tests",
                new BigDecimal("799.99"),
                "AdminBrand",
                2024,
                "ADMIN-TEST-001",
                45,
                ProductStatus.IN_STOCK,
                "Admin Chip",
                8,
                "6.5\"",
                "12 MP",
                "50 MP",
                "4500 mAh");
    }

    private static PhoneCreateRequestDto buildOtherPhoneCreateRequestDto() {
        return new PhoneCreateRequestDto(
                "Other Device",
                "Another phone for search and filter tests",
                new BigDecimal("499.99"),
                "OtherBrand",
                2023,
                "OTHER-DEVICE-001",
                12,
                ProductStatus.LOW_STOCK,
                "Other CPU",
                6,
                "6.1\"",
                "10 MP",
                "30 MP",
                "4000 mAh");
    }

    private static PhoneCreateRequestDto buildMoreExpensivePhoneCreateRequestDto() {
        return new PhoneCreateRequestDto(
                "Premium Test Phone",
                "More expensive phone for sorting tests",
                new BigDecimal("1499.99"),
                "PremiumBrand",
                2025,
                "PREMIUM-TEST-001",
                100,
                ProductStatus.IN_STOCK,
                "Premium CPU",
                10,
                "6.8\"",
                "16 MP",
                "108 MP",
                "5000 mAh");
    }

    private AdminProductCreateRequestDto buildAdminProductCreateRequestDto(String name, String sku) {
        return new AdminProductCreateRequestDto(
                name,
                "Created through admin product management",
                new BigDecimal("699.99"),
                "CreatedBrand",
                2024,
                sku,
                15,
                ProductStatus.IN_STOCK,
                "Created Chip",
                8,
                "6.4\"",
                "12 MP",
                "64 MP",
                "4300 mAh");
    }

    private MockMultipartFile jsonPart(String name, Object value) throws Exception {
        return new MockMultipartFile(
                name, "", MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsBytes(value));
    }

    private MockMultipartFile imagePart(String name, String filename) throws Exception {
        ClassPathResource resource =
                new ClassPathResource("web/controller/imageController/" + filename);
        return new MockMultipartFile(
                name, filename, MediaType.IMAGE_JPEG_VALUE, resource.getInputStream());
    }

    private Phone findPhoneBySku(String sku) {
        return phoneRepository.findAll().stream()
                .filter(phone -> phone.getSku().equals(sku))
                .findFirst()
                .orElseThrow();
    }

    private Order createOrder(OrderStatus status, String total, int quantity) {
        return createOrder(status, PaymentStatus.PENDING, total, quantity);
    }

    private Order createOrder(
            OrderStatus status, PaymentStatus paymentStatus, String total, int quantity) {
        Phone phone = findPhoneBySku("ADMIN-TEST-001");
        User customer =
                userRepository.findAll().stream()
                        .filter(user -> !ADMIN_EMAIL.equals(user.getEmail()))
                        .findFirst()
                        .orElseThrow();

        Order order =
                Order.builder()
                        .user(customer)
                        .customerEmail(customer.getEmail())
                        .customerFirstName(customer.getFirstName())
                        .customerLastName(customer.getLastName())
                        .customerPhoneNumber(customer.getPhoneNumber())
                        .status(status)
                        .paymentMethod(PaymentMethod.CARD)
                        .paymentDetails(
                                new PaymentDetails(paymentStatus,
                                        UUID.randomUUID().toString())
                        )
                        .deliveryMethod(DeliveryMethod.COURIER)
                        .shippingAddress(ShippingAddress.builder()
                                .houseNumber("10A")
                                .street("Main Street")
                                .city("Kyiv")
                                .region("Kyiv Region")
                                .country("Ukraine")
                                .zipCode("01001")
                                .logisticsCompany(LogisticsCompany.NOVA_POSHTA)
                                .build())
                        .total(new BigDecimal(total))
                        .build();
        order.addItem(
                OrderItem.builder()
                        .phone(phone)
                        .productName(phone.getName())
                        .sku(phone.getSku())
                        .unitPrice(phone.getPrice())
                        .quantity(quantity)
                        .totalPrice(phone.getPrice().multiply(BigDecimal.valueOf(quantity)))
                        .build());

        return orderRepository.save(order);
    }

    @Test
    void whenSearchMatchesSku_thenReturnFilteredList() throws Exception {
        phoneService.create(buildOtherPhoneCreateRequestDto(), new ArrayList<>());

        mockMvc
                .perform(
                        get(ADMIN_PRODUCTS_URL)
                                .param("search", "ADMIN-TEST-001")
                                .header(HttpHeaders.AUTHORIZATION, auth(adminToken)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].sku").value("ADMIN-TEST-001"));
    }

    @Test
    void whenStatusFilterMatchesProduct_thenReturnFilteredList() throws Exception {
        phoneService.create(buildOtherPhoneCreateRequestDto(), new ArrayList<>());

        mockMvc
                .perform(
                        get(ADMIN_PRODUCTS_URL)
                                .param("status", "IN_STOCK")
                                .header(HttpHeaders.AUTHORIZATION, auth(adminToken)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].status").value("IN_STOCK"))
                .andExpect(jsonPath("$.content[0].name").value("Admin Test Phone"));
    }

    @Test
    void whenSortByStockDesc_thenReturnProductsInSortedOrder() throws Exception {
        phoneService.create(buildOtherPhoneCreateRequestDto(), new ArrayList<>());

        mockMvc
                .perform(
                        get(ADMIN_PRODUCTS_URL)
                                .param("sort", "stock_desc")
                                .header(HttpHeaders.AUTHORIZATION, auth(adminToken)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].sku").value("ADMIN-TEST-001"))
                .andExpect(jsonPath("$.content[0].stock").value(45))
                .andExpect(jsonPath("$.content[1].sku").value("OTHER-DEVICE-001"))
                .andExpect(jsonPath("$.content[1].stock").value(12));
    }

    @Test
    void whenSortBySkuDesc_thenReturnProductsInSortedOrder() throws Exception {
        phoneService.create(buildOtherPhoneCreateRequestDto(), new ArrayList<>());

        mockMvc
                .perform(
                        get(ADMIN_PRODUCTS_URL)
                                .param("sort", "sku_desc")
                                .header(HttpHeaders.AUTHORIZATION, auth(adminToken)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].sku").value("OTHER-DEVICE-001"))
                .andExpect(jsonPath("$.content[1].sku").value("ADMIN-TEST-001"));
    }
}
