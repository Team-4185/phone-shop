package com.challengeteam.shop.web.controller;

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
import com.challengeteam.shop.entity.phone.PhoneCharacteristics;
import com.challengeteam.shop.entity.phone.ProductStatus;
import com.challengeteam.shop.entity.user.Role;
import com.challengeteam.shop.entity.user.User;
import com.challengeteam.shop.persistence.repository.OrderRepository;
import com.challengeteam.shop.persistence.repository.PhoneRepository;
import com.challengeteam.shop.persistence.repository.RoleRepository;
import com.challengeteam.shop.persistence.repository.UserRepository;
import com.challengeteam.shop.service.JwtService;
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
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
    private OrderRepository orderRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;
    private String userToken;
    private User customer;
    private Phone phone;

    @DynamicPropertySource
    static void loadPropertiesForTest(DynamicPropertyRegistry propertyRegistry) {
        TestContextConfigurator.initRequiredProperties(propertyRegistry);
    }

    @BeforeEach
    void setup() {
        orderRepository.deleteAll();
        phoneRepository.deleteAll();
        userRepository.deleteAll();

        adminToken = createAdminAccessToken();
        userToken = testAuthHelper.authorizeAsNewUser("customer.ntt46@valid.com", "Password123!");
        customer = userRepository.findByEmail("customer.ntt46@valid.com").orElseThrow();
        phone = phoneRepository.save(phone("Dashboard Phone", "DashBrand", "DASH-001", 20, ProductStatus.IN_STOCK));
    }

    @Test
    void whenUserRequestsCustomers_thenStatus403() throws Exception {
        mockMvc.perform(get(ADMIN_CUSTOMERS_URL).header(HttpHeaders.AUTHORIZATION, auth(userToken)))
                .andExpect(status().isForbidden());
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
    void whenDashboardDataExists_thenReturnSummaryRecentOrdersAndLowStockAlerts() throws Exception {
        createOrder(OrderStatus.DELIVERED, PaymentStatus.PAID, "799.99", 1);
        phoneRepository.save(phone("Low Stock Phone", "DashBrand", "LOW-001", 3, ProductStatus.LOW_STOCK));

        mockMvc.perform(get(ADMIN_DASHBOARD_URL + "/summary").header(HttpHeaders.AUTHORIZATION, auth(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRevenue").value(799.99))
                .andExpect(jsonPath("$.totalOrders").value(1))
                .andExpect(jsonPath("$.totalCustomers").value(1))
                .andExpect(jsonPath("$.lowStockProducts").value(1));

        mockMvc.perform(get(ADMIN_DASHBOARD_URL + "/recent-orders").header(HttpHeaders.AUTHORIZATION, auth(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].customerEmail").value(customer.getEmail()))
                .andExpect(jsonPath("$[0].paymentStatus").value("PAID"));

        mockMvc.perform(get(ADMIN_DASHBOARD_URL + "/low-stock-alerts").header(HttpHeaders.AUTHORIZATION, auth(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].sku").value("LOW-001"));
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

    private Order createOrder(OrderStatus status, PaymentStatus paymentStatus, String total, int quantity) {
        Order order = Order.builder()
                .user(customer)
                .customerEmail(customer.getEmail())
                .customerFirstName(customer.getFirstName())
                .customerLastName(customer.getLastName())
                .customerPhoneNumber(customer.getPhoneNumber())
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
                .productName(phone.getName())
                .sku(phone.getSku())
                .unitPrice(phone.getPrice())
                .quantity(quantity)
                .totalPrice(phone.getPrice().multiply(BigDecimal.valueOf(quantity)))
                .build());

        return orderRepository.save(order);
    }

    private static String auth(String token) {
        return "Bearer " + token;
    }
}
