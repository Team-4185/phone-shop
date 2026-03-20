package com.challengeteam.shop.web.controller;

import com.challengeteam.shop.dto.phone.PhoneCreateRequestDto;
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

    phoneService.create(buildPhoneCreateRequestDto(), new ArrayList<>());
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
    void whenAuthenticatedUserIsAdmin_thenStatus200() throws Exception {
      mockMvc
          .perform(get(ADMIN_PRODUCTS_URL).header(HttpHeaders.AUTHORIZATION, auth(adminToken)))
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.content").isArray())
          .andExpect(jsonPath("$.content", hasSize(1)))
          .andExpect(jsonPath("$.content[0].name").value("Admin Test Phone"));
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

  private static PhoneCreateRequestDto buildPhoneCreateRequestDto() {
    return new PhoneCreateRequestDto(
        "Admin Test Phone",
        "Phone prepared for admin controller tests",
        new BigDecimal("799.99"),
        "AdminBrand",
        2024,
        "Admin Chip",
        8,
        "6.5\"",
        "12 MP",
        "50 MP",
        "4500 mAh");
  }
}
