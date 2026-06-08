package com.challengeteam.shop.web.controller;

import com.challengeteam.shop.dto.phone.request.ProductReviewRequestDto;
import com.challengeteam.shop.entity.favorite.Favorite;
import com.challengeteam.shop.entity.phone.Phone;
import com.challengeteam.shop.entity.phone.PhoneCharacteristics;
import com.challengeteam.shop.entity.phone.PhoneColor;
import com.challengeteam.shop.entity.phone.ProductStatus;
import com.challengeteam.shop.entity.phone.StorageCapacity;
import com.challengeteam.shop.persistence.repository.FavoriteRepository;
import com.challengeteam.shop.persistence.repository.OrderRepository;
import com.challengeteam.shop.persistence.repository.PhoneRepository;
import com.challengeteam.shop.persistence.repository.ProductReviewRepository;
import com.challengeteam.shop.testContainer.ContainerExtension;
import com.challengeteam.shop.testContainer.TestContextConfigurator;
import com.challengeteam.shop.web.TestAuthHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.Set;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(ContainerExtension.class)
class ProductReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private PhoneRepository phoneRepository;
    @Autowired
    private ProductReviewRepository productReviewRepository;
    @Autowired
    private FavoriteRepository favoriteRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private TestAuthHelper testAuthHelper;

    private Long phoneId;
    private String token;

    @DynamicPropertySource
    static void loadProperties(DynamicPropertyRegistry registry) {
        TestContextConfigurator.initRequiredProperties(registry);
    }

    @BeforeEach
    void setUp() {
        productReviewRepository.deleteAll();
        favoriteRepository.deleteAll();
        orderRepository.deleteAll();
        phoneRepository.deleteAll();

        phoneId = phoneRepository.save(buildPhone()).getId();
        token = testAuthHelper.authorizeAsNewUser("review-" + UUID.randomUUID() + "@gmail.com", "Password1");
    }

    @Test
    void shouldManageOwnProductReviewAndExposeRatingSummary() throws Exception {
        mockMvc.perform(get("/api/v1/phones/{phoneId}/reviews", phoneId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));

        String createdReview = mockMvc.perform(post("/api/v1/phones/{phoneId}/reviews", phoneId)
                        .header(HttpHeaders.AUTHORIZATION, auth(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(new ProductReviewRequestDto(5, "Great phone"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phoneId").value(phoneId))
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.comment").value("Great phone"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long reviewId = objectMapper.readTree(createdReview).get("id").asLong();

        mockMvc.perform(get("/api/v1/phones/{phoneId}", phoneId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageRating").value(5.0))
                .andExpect(jsonPath("$.reviewsCount").value(1));

        mockMvc.perform(post("/api/v1/phones/{phoneId}/reviews", phoneId)
                        .header(HttpHeaders.AUTHORIZATION, auth(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(new ProductReviewRequestDto(4, "Still good"))))
                .andExpect(status().isBadRequest());

        mockMvc.perform(patch("/api/v1/phones/{phoneId}/reviews/{reviewId}", phoneId, reviewId)
                        .header(HttpHeaders.AUTHORIZATION, auth(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(new ProductReviewRequestDto(3, "Updated"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(3))
                .andExpect(jsonPath("$.comment").value("Updated"));

        mockMvc.perform(delete("/api/v1/phones/{phoneId}/reviews/{reviewId}", phoneId, reviewId)
                        .header(HttpHeaders.AUTHORIZATION, auth(token)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/phones/{phoneId}/reviews", phoneId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    void shouldRejectReviewWriteWithoutAuthentication() throws Exception {
        mockMvc.perform(post("/api/v1/phones/{phoneId}/reviews", phoneId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(new ProductReviewRequestDto(5, null))))
                .andExpect(status().isUnauthorized());
    }

    private Phone buildPhone() {
        return Phone.builder()
                .name("Review Phone")
                .description("Reviewable phone")
                .price(new BigDecimal("999.00"))
                .brand("Apple")
                .releaseYear(2024)
                .sku("REVIEW-" + UUID.randomUUID())
                .stock(10)
                .status(ProductStatus.IN_STOCK)
                .phoneCharacteristics(PhoneCharacteristics.builder()
                        .cpu("A18")
                        .coresNumber(8)
                        .screenSize("6.7")
                        .frontCamera("12MP")
                        .mainCamera("48MP")
                        .batteryCapacity("5000")
                        .phoneColors(Set.of(PhoneColor.BLUE))
                        .storageCapacities(Set.of(StorageCapacity.CAPACITY_256GB))
                        .build())
                .build();
    }

    private String auth(String token) {
        return "Bearer " + token;
    }
}
