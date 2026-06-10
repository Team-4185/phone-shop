package com.challengeteam.shop.web.controller;

import com.challengeteam.shop.entity.image.Image;
import com.challengeteam.shop.entity.image.MIMEType;
import com.challengeteam.shop.entity.order.DeliveryMethod;
import com.challengeteam.shop.entity.order.Order;
import com.challengeteam.shop.entity.order.OrderItem;
import com.challengeteam.shop.entity.order.OrderStatus;
import com.challengeteam.shop.entity.order.payment.PaymentDetails;
import com.challengeteam.shop.entity.order.payment.PaymentMethod;
import com.challengeteam.shop.entity.order.payment.PaymentStatus;
import com.challengeteam.shop.entity.order.shipping.LogisticsCompany;
import com.challengeteam.shop.entity.order.shipping.ShippingAddress;
import com.challengeteam.shop.entity.phone.*;
import com.challengeteam.shop.entity.user.Role;
import com.challengeteam.shop.entity.user.User;
import com.challengeteam.shop.persistence.repository.*;
import com.challengeteam.shop.service.security.auth.jwt.JwtService;
import com.challengeteam.shop.testContainer.ContainerExtension;
import com.challengeteam.shop.testContainer.TestContextConfigurator;
import com.challengeteam.shop.web.TestAuthHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.oneOf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(ContainerExtension.class)
class AdminCustomerDashboardControllerTest {

    private static final String ADMIN_CUSTOMERS_URL = "/api/v1/admin/customers";
    private static final String ADMIN_DASHBOARD_URL = "/api/v1/admin/dashboard";
    private static final String ADMIN_EMAIL = "admin.ntt46@valid.com";
    private static final String ADMIN_PASSWORD = "AdminPassword123!";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private TestAuthHelper testAuthHelper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PhoneRepository phoneRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private MIMETypeRepository mimeTypeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;
    private String userToken;
    private User customer;
    private Phone phone;
    private ProductVariant productVariant;

    @DynamicPropertySource
    static void loadPropertiesForTest(DynamicPropertyRegistry propertyRegistry) {
        TestContextConfigurator.initRequiredProperties(propertyRegistry);
    }

    @BeforeEach
    void setup() {
        orderRepository.deleteAll();
        imageRepository.deleteAll();
        productVariantRepository.deleteAll();
        phoneRepository.deleteAll();
        userRepository.deleteAll();

        adminToken = createAdminAccessToken();
        userToken = testAuthHelper.authorizeAsNewUser("customer.ntt46@valid.com", "Password123!");
        customer = userRepository.findByEmail("customer.ntt46@valid.com").orElseThrow();
        phone = savePhoneWithDefaultVariant(phone("Dashboard Phone", "DashBrand", "DASH-001", 20, ProductStatus.IN_STOCK));
        imageRepository.save(image("dashboard-phone.jpg", phone));
    }

    @Test
    void whenRequestCustomersWithoutToken_thenStatus401() throws Exception {
        mockMvc.perform(get(ADMIN_CUSTOMERS_URL))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void whenUserRequestsCustomers_thenStatus403() throws Exception {
        mockMvc.perform(get(ADMIN_CUSTOMERS_URL).header(HttpHeaders.AUTHORIZATION, auth(userToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    void whenRequestCustomerKpiWithoutToken_thenStatus401() throws Exception {
        mockMvc.perform(get(ADMIN_CUSTOMERS_URL + "/kpi"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void whenUserRequestsCustomerKpi_thenStatus403() throws Exception {
        mockMvc.perform(get(ADMIN_CUSTOMERS_URL + "/kpi").header(HttpHeaders.AUTHORIZATION, auth(userToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    void whenAdminRequestsCustomerKpi_thenReturnCustomerCardData() throws Exception {
        createOrder(OrderStatus.DELIVERED, PaymentStatus.PAID, "800.00", 1);

        User inactiveCustomer = createCustomer("inactive.ntt46@valid.com");
        inactiveCustomer.setCreatedAt(Instant.now().minus(120, ChronoUnit.DAYS));
        inactiveCustomer = userRepository.save(inactiveCustomer);
        createOrder(
                inactiveCustomer,
                OrderStatus.DELIVERED,
                PaymentStatus.PAID,
                "400.00",
                1,
                Instant.now().minus(120, ChronoUnit.DAYS));

        mockMvc.perform(get(ADMIN_CUSTOMERS_URL + "/kpi").header(HttpHeaders.AUTHORIZATION, auth(adminToken)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalClients").value(2))
                .andExpect(jsonPath("$.newCustomersThisMonth").value(1))
                .andExpect(jsonPath("$.inactiveCustomers").value(1))
                .andExpect(jsonPath("$.averageReceipt").value(600.00));
    }

    @Test
    void whenAdminRequestsCustomers_thenReturnCustomerPurchaseMetrics() throws Exception {
        createOrder(OrderStatus.DELIVERED, PaymentStatus.PAID, "799.99", 1);

        mockMvc.perform(get(ADMIN_CUSTOMERS_URL).header(HttpHeaders.AUTHORIZATION, auth(adminToken)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].email").value(customer.getEmail()))
                .andExpect(jsonPath("$.content[0].totalOrders").value(1))
                .andExpect(jsonPath("$.content[0].totalSpent").value(799.99))
                .andExpect(jsonPath("$.content[0].status").value("ACTIVE"));
    }

    @Test
    void whenAdminSearchesCustomerByEmail_thenReturnMatchingCustomer() throws Exception {
        createOrder(OrderStatus.DELIVERED, PaymentStatus.PAID, "799.99", 1);

        mockMvc.perform(get(ADMIN_CUSTOMERS_URL)
                        .param("search", "customer.ntt46")
                        .header(HttpHeaders.AUTHORIZATION, auth(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].email").value(customer.getEmail()));
    }

    @Test
    void whenAdminUsesUnsupportedCustomerSort_thenStatus400() throws Exception {
        mockMvc.perform(get(ADMIN_CUSTOMERS_URL)
                        .param("sort", "role_desc")
                        .header(HttpHeaders.AUTHORIZATION, auth(adminToken)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenAdminRequestsCustomerDetails_thenReturnRecentOrders() throws Exception {
        Order order = createOrder(OrderStatus.DELIVERED, PaymentStatus.PAID, "799.99", 1);

        mockMvc.perform(get(ADMIN_CUSTOMERS_URL + "/{id}", customer.getId())
                        .header(HttpHeaders.AUTHORIZATION, auth(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(customer.getId()))
                .andExpect(jsonPath("$.email").value(customer.getEmail()))
                .andExpect(jsonPath("$.recentOrders", hasSize(1)))
                .andExpect(jsonPath("$.recentOrders[0].id").value(order.getId()))
                .andExpect(jsonPath("$.recentOrders[0].paymentStatus").value("PAID"));
    }

    @Test
    void whenCustomerDoesNotExist_thenStatus404() throws Exception {
        mockMvc.perform(get(ADMIN_CUSTOMERS_URL + "/{id}", 999999L)
                        .header(HttpHeaders.AUTHORIZATION, auth(adminToken)))
                .andExpect(status().isNotFound());
    }

    @Test
    void whenRequestDashboardWithoutToken_thenStatus401() throws Exception {
        mockMvc.perform(get(ADMIN_DASHBOARD_URL + "/summary"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void whenUserRequestsDashboard_thenStatus403() throws Exception {
        mockMvc.perform(get(ADMIN_DASHBOARD_URL + "/summary")
                        .header(HttpHeaders.AUTHORIZATION, auth(userToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    void whenDashboardDataExists_thenReturnSummaryRecentOrdersAndLowStockAlerts() throws Exception {
        createOrder(OrderStatus.DELIVERED, PaymentStatus.PAID, "799.99", 1);
        savePhoneWithDefaultVariant(phone("Low Stock Phone", "DashBrand", "LOW-001", 3, ProductStatus.LOW_STOCK));

        mockMvc.perform(get(ADMIN_DASHBOARD_URL + "/summary").header(HttpHeaders.AUTHORIZATION, auth(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRevenue").value(799.99))
                .andExpect(jsonPath("$.totalRevenueChangePercent").value(100))
                .andExpect(jsonPath("$.totalOrders").value(1))
                .andExpect(jsonPath("$.totalOrdersChangePercent").value(100))
                .andExpect(jsonPath("$.processingOrders").value(0))
                .andExpect(jsonPath("$.itemsInStock").value(23))
                .andExpect(jsonPath("$.lowStockProducts").value(1))
                .andExpect(jsonPath("$.newClients").value(1))
                .andExpect(jsonPath("$.newClientsChangePercent").value(100));

        mockMvc.perform(get(ADMIN_DASHBOARD_URL + "/recent-orders").header(HttpHeaders.AUTHORIZATION, auth(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].customerEmail").value(customer.getEmail()))
                .andExpect(jsonPath("$[0].customerName").value(customer.getEmail()))
                .andExpect(jsonPath("$[0].productName").value("Dashboard Phone"))
                .andExpect(jsonPath("$[0].paymentStatus").value("PAID"));

        mockMvc.perform(get(ADMIN_DASHBOARD_URL + "/low-stock-alerts").header(HttpHeaders.AUTHORIZATION, auth(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].sku").value("LOW-001"))
                .andExpect(jsonPath("$[0].stock").value(3))
                .andExpect(jsonPath("$[0].threshold").value(10));
    }

    @Test
    void whenDashboardSalesWidgetsRequested_thenReturnDesignAlignedData() throws Exception {
        createOrder(OrderStatus.DELIVERED, PaymentStatus.PAID, "799.99", 2);

        mockMvc.perform(get(ADMIN_DASHBOARD_URL + "/sales-analytics")
                        .param("period", "month")
                        .header(HttpHeaders.AUTHORIZATION, auth(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].revenue").value(799.99))
                .andExpect(jsonPath("$[0].profit").value(799.99))
                .andExpect(jsonPath("$[0].salesCount").value(1))
                .andExpect(jsonPath("$[0].ordersCount").value(1));

        mockMvc.perform(get(ADMIN_DASHBOARD_URL + "/sales-by-brand")
                        .header(HttpHeaders.AUTHORIZATION, auth(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].brand").value("DashBrand"))
                .andExpect(jsonPath("$[0].revenue").value(1599.98))
                .andExpect(jsonPath("$[0].unitsSold").value(2))
                .andExpect(jsonPath("$[0].percentage").value(100));

        mockMvc.perform(get(ADMIN_DASHBOARD_URL + "/top-selling-products")
                        .header(HttpHeaders.AUTHORIZATION, auth(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Dashboard Phone"))
                .andExpect(jsonPath("$[0].brand").value("DashBrand"))
                .andExpect(jsonPath("$[0].unitsSold").value(2))
                .andExpect(jsonPath("$[0].revenue").value(1599.98))
                .andExpect(jsonPath("$[0].stock").value(20))
                .andExpect(jsonPath("$[0].status").value("IN_STOCK"))
                .andExpect(jsonPath("$[0].previewImage.name").value("dashboard-phone.jpg"))
                .andExpect(jsonPath("$[0].previewImage.url").value(
                        "http://localhost/api/v1/images/" + imageRepository.findAll().getFirst().getId()))
                .andExpect(jsonPath("$[0].previewImage.mimeType").value(
                        oneOf("image/jpeg", "image/jpg")));
    }

    @Test
    void whenTopSellingProductHasNoImage_thenPreviewImageIsNotReturned() throws Exception {
        imageRepository.deleteAll();
        createOrder(OrderStatus.DELIVERED, PaymentStatus.PAID, "799.99", 1);

        mockMvc.perform(get(ADMIN_DASHBOARD_URL + "/top-selling-products")
                        .header(HttpHeaders.AUTHORIZATION, auth(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].brand").value("DashBrand"))
                .andExpect(jsonPath("$[0].previewImage").doesNotExist());
    }

    @Test
    void whenSalesAnalyticsPeriodIsUnsupported_thenStatus400() throws Exception {
        mockMvc.perform(get(ADMIN_DASHBOARD_URL + "/sales-analytics")
                        .param("period", "quarter")
                        .header(HttpHeaders.AUTHORIZATION, auth(adminToken)))
                .andExpect(status().isBadRequest());
    }

    private String createAdminAccessToken() {
        Role adminRole = roleRepository
                .findByName("ADMIN")
                .orElseThrow(() -> new IllegalStateException("Admin role is missing in test database"));

        User adminUser = User.builder()
                .email(ADMIN_EMAIL)
                .password(passwordEncoder.encode(ADMIN_PASSWORD))
                .role(adminRole)
                .build();

        return jwtService.createAccessToken(userRepository.save(adminUser));
    }

    private User createCustomer(String email) {
        Role userRole = roleRepository
                .findByName("USER")
                .orElseThrow(() -> new IllegalStateException("User role is missing in test database"));

        return userRepository.save(User.builder()
                .email(email)
                .password(passwordEncoder.encode("Password123!"))
                .role(userRole)
                .build());
    }

    private Phone phone(String name, String brand, String sku, int stock, ProductStatus status) {
        return Phone.builder()
                .name(name)
                .description("Test phone")
                .brand(brand)
                .sku(sku)
                .price(new BigDecimal("799.99"))
                .releaseYear(2025)
                .stock(stock)
                .status(status)
                .phoneCharacteristics(PhoneCharacteristics.builder()
                        .cpu("Test CPU")
                        .coresNumber(8)
                        .screenSize("6.5")
                        .frontCamera("12 MP")
                        .mainCamera("50 MP")
                        .batteryCapacity("4500 mAh")
                        .build())
                .build();
    }

    private Phone savePhoneWithDefaultVariant(Phone phone) {
        Phone savedPhone = phoneRepository.save(phone);
        ProductVariant savedVariant = productVariantRepository.save(ProductVariant.builder()
                .phone(savedPhone)
                .sku(savedPhone.getSku() + "-GOLD-128")
                .color(PhoneColor.GOLD)
                .storageCapacity(StorageCapacity.CAPACITY_128GB)
                .price(savedPhone.getPrice())
                .stock(savedPhone.getStock())
                .status(savedPhone.getStatus())
                .build());
        if ("DASH-001".equals(savedPhone.getSku())) {
            productVariant = savedVariant;
        }
        return savedPhone;
    }

    private Image image(String name, Phone phone) {
        MIMEType mimeType = mimeTypeRepository.findByExtension("jpg")
                .orElseGet(() -> mimeTypeRepository.save(
                        MIMEType.builder()
                                .extension("jpg")
                                .type("image/jpeg")
                                .build()));

        return Image.builder()
                .name(name)
                .storageKey("dashboard/" + name)
                .size(1024L)
                .mimeType(mimeType)
                .phone(phone)
                .build();
    }

    private Order createOrder(OrderStatus status, PaymentStatus paymentStatus, String total, int quantity) {
        return createOrder(customer, status, paymentStatus, total, quantity, null);
    }

    private Order createOrder(
            User orderCustomer,
            OrderStatus status,
            PaymentStatus paymentStatus,
            String total,
            int quantity,
            Instant createdAt) {
        Order order = Order.builder()
                .user(orderCustomer)
                .customerEmail(orderCustomer.getEmail())
                .customerFirstName(orderCustomer.getFirstName())
                .customerLastName(orderCustomer.getLastName())
                .customerPhoneNumber(orderCustomer.getPhoneNumber())
                .status(status)
                .paymentMethod(PaymentMethod.CARD)
                .paymentDetails(new PaymentDetails(paymentStatus, UUID.randomUUID().toString()))
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

        order.addItem(OrderItem.builder()
                .phone(phone)
                .variant(productVariant)
                .productName(phone.getName())
                .sku(productVariant.getSku())
                .unitPrice(productVariant.getPrice())
                .quantity(quantity)
                .totalPrice(productVariant.getPrice().multiply(BigDecimal.valueOf(quantity)))
                .selectedColor(productVariant.getColor())
                .selectedStorage(productVariant.getStorageCapacity())
                .build());

        Order savedOrder = orderRepository.save(order);
        if (createdAt != null) {
            savedOrder.setCreatedAt(createdAt);
            savedOrder = orderRepository.save(savedOrder);
        }
        return savedOrder;
    }

    private static String auth(String token) {
        return "Bearer " + token;
    }
}
