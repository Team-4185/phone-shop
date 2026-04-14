package com.challengeteam.shop.web.controller;

import com.challengeteam.shop.dto.phone.PhoneCreateRequestDto;
import com.challengeteam.shop.entity.phone.Phone;
import com.challengeteam.shop.entity.phone.ProductStatus;
import com.challengeteam.shop.entity.user.Role;
import com.challengeteam.shop.entity.user.User;
import com.challengeteam.shop.persistence.repository.PhoneRepository;
import com.challengeteam.shop.persistence.repository.RoleRepository;
import com.challengeteam.shop.persistence.repository.UserRepository;
import com.challengeteam.shop.service.JwtService;
import com.challengeteam.shop.service.PhoneService;
import com.challengeteam.shop.testContainer.ContainerExtension;
import com.challengeteam.shop.testContainer.TestContextConfigurator;
import com.challengeteam.shop.web.TestAuthHelper;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(ContainerExtension.class)
class AdminControllerTest {
  private static final String ADMIN_ROOT_URL = "/api/v1/admin";
  private static final String ADMIN_PRODUCTS_URL = "/api/v1/admin/products";
  private static final String ADMIN_ORDERS_URL = "/api/v1/admin/orders";
  private static final String ADMIN_EMAIL = "admin.test@valid.com";
  private static final String ADMIN_PASSWORD = "AdminPassword123!";

  @Autowired private MockMvc mockMvc;

  @Autowired private TestAuthHelper testAuthHelper;

  @Autowired private JwtService jwtService;

  @Autowired private UserRepository userRepository;

  @Autowired private RoleRepository roleRepository;

  @Autowired private PhoneRepository phoneRepository;

  @Autowired private PhoneService phoneService;

  @Autowired private PasswordEncoder passwordEncoder;

  private String userToken;
  private String adminToken;

  @DynamicPropertySource
  static void loadPropertiesForTest(DynamicPropertyRegistry propertyRegistry) {
    TestContextConfigurator.initRequiredProperties(propertyRegistry);
  }

  @BeforeEach
  void setup() {
    phoneRepository.deleteAll();
    userRepository.deleteAll();

    userToken = testAuthHelper.authorizeLikeTestUser();
    adminToken = createAdminAccessToken();

    phoneService.create(buildAdminTestPhoneCreateRequestDto(), new ArrayList<>());
  }

  @Nested
  @DisplayName("GET /api/v1/admin")
  class GetAdminEntryPointTest {

    @Test
    void whenRequestMissingToken_thenStatus403() throws Exception {
      mockMvc.perform(get(ADMIN_ROOT_URL)).andExpect(status().isForbidden());
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
          .andExpect(jsonPath("$.sections[1].implemented").value(false));
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
    void whenAuthenticatedUserIsAdmin_thenStatus501() throws Exception {
      mockMvc
          .perform(get(ADMIN_ORDERS_URL).header(HttpHeaders.AUTHORIZATION, auth(adminToken)))
          .andExpect(status().isNotImplemented());
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
}
