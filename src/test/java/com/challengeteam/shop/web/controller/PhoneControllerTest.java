package com.challengeteam.shop.web.controller;

import com.challengeteam.shop.dto.phone.PhoneCreateRequestDto;
import com.challengeteam.shop.dto.phone.PhoneUpdateRequestDto;
import com.challengeteam.shop.persistence.repository.PhoneRepository;
import com.challengeteam.shop.service.PhoneService;
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

import static com.challengeteam.shop.web.controller.PhoneControllerTest.TestPhone.VALID_PHONE_BOUNDARY_MAX;
import static com.challengeteam.shop.web.controller.PhoneControllerTest.TestPhone.VALID_PHONE_BOUNDARY_MIN;
import static com.challengeteam.shop.web.controller.PhoneControllerTest.TestResources.*;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(ContainerExtension.class)
class PhoneControllerTest {

    @Autowired
    private TestAuthHelper testAuthHelper;

    @Autowired
    private PhoneRepository phoneRepository;

    @Autowired
    private PhoneService phoneService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Long phoneId1;
    private Long phoneId2;
    private Long phoneId3;
    private String accessToken;

    @DynamicPropertySource
    static void loadPropertiesForTest(DynamicPropertyRegistry propertyRegistry) {
        TestContextConfigurator.initRequiredProperties(propertyRegistry);
    }

    @BeforeEach
    void setup() {
        phoneRepository.deleteAll();

        phoneId1 = phoneService.create(buildPhoneCreateRequestDto(TestPhone.PHONE_1));
        phoneId2 = phoneService.create(buildPhoneCreateRequestDto(TestPhone.PHONE_2));
        phoneId3 = phoneService.create(buildPhoneCreateRequestDto(TestPhone.PHONE_3));

        accessToken = testAuthHelper.authorizeLikeTestUser();
    }

    @Nested
    @DisplayName("GET /api/v1/phones")
    class GetPhonesTest {
        private final static String URL = "/api/v1/phones";

        @Test
        void whenValidRequest_thenStatus200AndReturnPagedPhones() throws Exception {
            mockMvc.perform(get(URL)
                            .param("page", "0")
                            .param("size", "10")
                            .header(HttpHeaders.AUTHORIZATION, auth(accessToken)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content", hasSize(3)))
                    .andExpect(jsonPath("$.totalElements").value(3))
                    .andExpect(jsonPath("$.totalPages").value(1))
                    .andExpect(jsonPath("$.size").value(10))
                    .andExpect(jsonPath("$.number").value(0))
                    .andExpect(jsonPath("$.content[0].id").exists())
                    .andExpect(jsonPath("$.content[0].name").exists())
                    .andExpect(jsonPath("$.content[0].description").exists())
                    .andExpect(jsonPath("$.content[0].price").exists())
                    .andExpect(jsonPath("$.content[0].brand").exists())
                    .andExpect(jsonPath("$.content[0].releaseYear").exists());
        }

        @Test
        void whenRequestWithPagination_thenReturnCorrectPage() throws Exception {
            mockMvc.perform(get(URL)
                            .param("page", "0")
                            .param("size", "2")
                            .header(HttpHeaders.AUTHORIZATION, auth(accessToken)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(2)))
                    .andExpect(jsonPath("$.totalElements").value(3))
                    .andExpect(jsonPath("$.totalPages").value(2))
                    .andExpect(jsonPath("$.size").value(2))
                    .andExpect(jsonPath("$.number").value(0));
        }

        @Test
        void whenRequestSecondPage_thenReturnCorrectPage() throws Exception {
            mockMvc.perform(get(URL)
                            .param("page", "1")
                            .param("size", "2")
                            .header(HttpHeaders.AUTHORIZATION, auth(accessToken)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.totalElements").value(3))
                    .andExpect(jsonPath("$.totalPages").value(2))
                    .andExpect(jsonPath("$.size").value(2))
                    .andExpect(jsonPath("$.number").value(1));
        }

        @Test
        void whenRequestEmptyPage_thenReturnEmptyContent() throws Exception {
            mockMvc.perform(get(URL)
                            .param("page", "10")
                            .param("size", "10")
                            .header(HttpHeaders.AUTHORIZATION, auth(accessToken)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(0)))
                    .andExpect(jsonPath("$.totalElements").value(3))
                    .andExpect(jsonPath("$.number").value(10));
        }

        @Test
        void whenRequestMissingToken_thenStatus403() throws Exception {
            mockMvc.perform(get(URL)
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isForbidden());
        }

        @Test
        void whenRequestHasInvalidToken_thenStatus403() throws Exception {
            mockMvc.perform(get(URL)
                            .param("page", "0")
                            .param("size", "10")
                            .header(HttpHeaders.AUTHORIZATION, auth("some_invalid_text")))
                    .andExpect(status().isForbidden());
        }

        @Test
        void whenPageIsNegative_thenStatus400() throws Exception {
            mockMvc.perform(get(URL)
                            .param("page", "-1")
                            .param("size", "10")
                            .header(HttpHeaders.AUTHORIZATION, auth(accessToken)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void whenSizeIsZero_thenStatus400() throws Exception {
            mockMvc.perform(get(URL)
                            .param("page", "0")
                            .param("size", "0")
                            .header(HttpHeaders.AUTHORIZATION, auth(accessToken)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void whenSizeIsNegative_thenStatus400() throws Exception {
            mockMvc.perform(get(URL)
                            .param("page", "0")
                            .param("size", "-5")
                            .header(HttpHeaders.AUTHORIZATION, auth(accessToken)))
                    .andExpect(status().isBadRequest());
        }

    }

    @Nested
    @DisplayName("GET /api/v1/phones/{phoneId}")
    class GetPhoneByIdTest {
        private final static String URL = "/api/v1/phones/{phoneId}";

        @Test
        void whenExists_thenStatus200AndReturnPhoneResponseDto() throws Exception {
            mockMvc.perform(get(URL, phoneId1)
                            .header(HttpHeaders.AUTHORIZATION, auth(accessToken)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.name").exists())
                    .andExpect(jsonPath("$.description").exists())
                    .andExpect(jsonPath("$.price").exists())
                    .andExpect(jsonPath("$.brand").exists())
                    .andExpect(jsonPath("$.releaseYear").exists());
        }

        @Test
        void whenDoesntExist_thenStatus404() throws Exception {
            mockMvc.perform(get(URL, NON_EXISTING_ID)
                            .header(HttpHeaders.AUTHORIZATION, auth(accessToken)))
                    .andExpect(status().isNotFound());
        }

        @Test
        void whenRequestMissingToken_thenStatus403() throws Exception {
            mockMvc.perform(get(URL, phoneId1))
                    .andExpect(status().isForbidden());
        }

        @Test
        void whenRequestHasInvalidToken_thenStatus403() throws Exception {
            mockMvc.perform(get(URL, phoneId1)
                            .header(HttpHeaders.AUTHORIZATION, auth("some_invalid_text")))
                    .andExpect(status().isForbidden());
        }

    }

    @Nested
    @DisplayName("POST /api/v1/phones")
    class CreatePhoneTest {
        private final static String URL = "/api/v1/phones";

        @Test
        void whenValidRequest_thenStatus201AndLocationHeader() throws Exception {
            PhoneCreateRequestDto request = buildPhoneCreateRequestDto(TestPhone.VALID_PHONE);

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth(accessToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(header().exists(HttpHeaders.LOCATION))
                    .andExpect(header().string(HttpHeaders.LOCATION, containsString("/api/v1/phones/")));
        }

        @Test
        void whenValidRequestWithNullDescription_thenStatus201() throws Exception {
            PhoneCreateRequestDto request = buildPhoneCreateRequestDto(TestPhone.VALID_PHONE_NULL_DESCRIPTION);

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth(accessToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(header().exists(HttpHeaders.LOCATION));
        }

        @Test
        void whenValidRequestWithBoundaryMinValues_thenStatus201() throws Exception {
            PhoneCreateRequestDto request = buildPhoneCreateRequestDto(VALID_PHONE_BOUNDARY_MIN);

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth(accessToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }

        @Test
        void whenValidRequestWithBoundaryMaxValues_thenStatus201() throws Exception {
            PhoneCreateRequestDto request = buildPhoneCreateRequestDto(VALID_PHONE_BOUNDARY_MAX);

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth(accessToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }

        @Test
        void whenRequestMissingToken_thenStatus403() throws Exception {
            PhoneCreateRequestDto request = buildPhoneCreateRequestDto(TestPhone.VALID_PHONE);

            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        void whenRequestHasInvalidToken_thenStatus403() throws Exception {
            PhoneCreateRequestDto request = buildPhoneCreateRequestDto(TestPhone.VALID_PHONE);

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth("some_invalid_text"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        // Validation tests
        @Test
        void whenNameIsNull_thenStatus400() throws Exception {
            PhoneCreateRequestDto request = buildPhoneCreateRequestDto(TestPhone.INVALID_NAME_NULL);

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth(accessToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void whenNameIsBlank_thenStatus400() throws Exception {
            PhoneCreateRequestDto request = buildPhoneCreateRequestDto(TestPhone.INVALID_NAME_BLANK);

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth(accessToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void whenNameTooShort_thenStatus400() throws Exception {
            PhoneCreateRequestDto request = buildPhoneCreateRequestDto(TestPhone.INVALID_NAME_TOO_SHORT);

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth(accessToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void whenNameTooLong_thenStatus400() throws Exception {
            PhoneCreateRequestDto request = buildPhoneCreateRequestDto(TestPhone.INVALID_NAME_TOO_LONG);

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth(accessToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void whenDescriptionTooLong_thenStatus400() throws Exception {
            PhoneCreateRequestDto request = buildPhoneCreateRequestDto(TestPhone.INVALID_DESCRIPTION_TOO_LONG);

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth(accessToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void whenPriceIsNull_thenStatus400() throws Exception {
            PhoneCreateRequestDto request = buildPhoneCreateRequestDto(TestPhone.INVALID_PRICE_NULL);

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth(accessToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void whenPriceIsNegative_thenStatus400() throws Exception {
            PhoneCreateRequestDto request = buildPhoneCreateRequestDto(TestPhone.INVALID_PRICE_NEGATIVE);

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth(accessToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void whenBrandIsNull_thenStatus400() throws Exception {
            PhoneCreateRequestDto request = buildPhoneCreateRequestDto(TestPhone.INVALID_BRAND_NULL);

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth(accessToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void whenBrandIsBlank_thenStatus400() throws Exception {
            PhoneCreateRequestDto request = buildPhoneCreateRequestDto(TestPhone.INVALID_BRAND_BLANK);

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth(accessToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void whenBrandTooShort_thenStatus400() throws Exception {
            PhoneCreateRequestDto request = buildPhoneCreateRequestDto(TestPhone.INVALID_BRAND_TOO_SHORT);

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth(accessToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void whenBrandTooLong_thenStatus400() throws Exception {
            PhoneCreateRequestDto request = buildPhoneCreateRequestDto(TestPhone.INVALID_BRAND_TOO_LONG);

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth(accessToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void whenReleaseYearIsNull_thenStatus400() throws Exception {
            PhoneCreateRequestDto request = buildPhoneCreateRequestDto(TestPhone.INVALID_YEAR_NULL);

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth(accessToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void whenReleaseYearTooEarly_thenStatus400() throws Exception {
            PhoneCreateRequestDto request = buildPhoneCreateRequestDto(TestPhone.INVALID_YEAR_TOO_EARLY);

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth(accessToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void whenReleaseYearInFuture_thenStatus400() throws Exception {
            PhoneCreateRequestDto request = buildPhoneCreateRequestDto(TestPhone.INVALID_YEAR_FUTURE);

            mockMvc.perform(post(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth(accessToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/phones/{phoneId}")
    class UpdatePhoneTest {
        private final static String URL = "/api/v1/phones/{phoneId}";

        @Test
        void whenValidRequest_thenStatus204() throws Exception {
            PhoneUpdateRequestDto request = buildPhoneUpdateRequestDto(TestPhone.VALID_PHONE);

            mockMvc.perform(put(URL, phoneId1)
                            .header(HttpHeaders.AUTHORIZATION, auth(accessToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNoContent());
        }

        @Test
        void whenValidRequestWithNullDescription_thenStatus204() throws Exception {
            PhoneUpdateRequestDto request = buildPhoneUpdateRequestDto(TestPhone.VALID_PHONE_NULL_DESCRIPTION);

            mockMvc.perform(put(URL, phoneId1)
                            .header(HttpHeaders.AUTHORIZATION, auth(accessToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNoContent());
        }

        @Test
        void whenValidRequestWithBoundaryMinValues_thenStatus204() throws Exception {
            PhoneUpdateRequestDto request = buildPhoneUpdateRequestDto(VALID_PHONE_BOUNDARY_MIN);

            mockMvc.perform(put(URL, phoneId1)
                            .header(HttpHeaders.AUTHORIZATION, auth(accessToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNoContent());
        }

        @Test
        void whenValidRequestWithBoundaryMaxValues_thenStatus204() throws Exception {
            PhoneUpdateRequestDto request = buildPhoneUpdateRequestDto(VALID_PHONE_BOUNDARY_MAX);

            mockMvc.perform(put(URL, phoneId1)
                            .header(HttpHeaders.AUTHORIZATION, auth(accessToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNoContent());
        }

        @Test
        void whenRequestMissingToken_thenStatus403() throws Exception {
            PhoneUpdateRequestDto request = buildPhoneUpdateRequestDto(TestPhone.VALID_PHONE);

            mockMvc.perform(put(URL, phoneId1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        void whenRequestHasInvalidToken_thenStatus403() throws Exception {
            PhoneUpdateRequestDto request = buildPhoneUpdateRequestDto(TestPhone.VALID_PHONE);

            mockMvc.perform(put(URL, phoneId1)
                            .header(HttpHeaders.AUTHORIZATION, auth("some_invalid_text"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        // Validation tests
        @Test
        void whenNameIsNull_thenStatus400() throws Exception {
            PhoneUpdateRequestDto request = buildPhoneUpdateRequestDto(TestPhone.INVALID_NAME_NULL);

            mockMvc.perform(put(URL, phoneId1)
                            .header(HttpHeaders.AUTHORIZATION, auth(accessToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void whenNameIsBlank_thenStatus400() throws Exception {
            PhoneUpdateRequestDto request = buildPhoneUpdateRequestDto(TestPhone.INVALID_NAME_BLANK);

            mockMvc.perform(put(URL, phoneId1)
                            .header(HttpHeaders.AUTHORIZATION, auth(accessToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void whenNameTooShort_thenStatus400() throws Exception {
            PhoneUpdateRequestDto request = buildPhoneUpdateRequestDto(TestPhone.INVALID_NAME_TOO_SHORT);

            mockMvc.perform(put(URL, phoneId1)
                            .header(HttpHeaders.AUTHORIZATION, auth(accessToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void whenNameTooLong_thenStatus400() throws Exception {
            PhoneUpdateRequestDto request = buildPhoneUpdateRequestDto(TestPhone.INVALID_NAME_TOO_LONG);

            mockMvc.perform(put(URL, phoneId1)
                            .header(HttpHeaders.AUTHORIZATION, auth(accessToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void whenDescriptionTooLong_thenStatus400() throws Exception {
            PhoneUpdateRequestDto request = buildPhoneUpdateRequestDto(TestPhone.INVALID_DESCRIPTION_TOO_LONG);

            mockMvc.perform(put(URL, phoneId1)
                            .header(HttpHeaders.AUTHORIZATION, auth(accessToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void whenPriceIsNull_thenStatus400() throws Exception {
            PhoneUpdateRequestDto request = buildPhoneUpdateRequestDto(TestPhone.INVALID_PRICE_NULL);

            mockMvc.perform(put(URL, phoneId1)
                            .header(HttpHeaders.AUTHORIZATION, auth(accessToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void whenPriceIsNegative_thenStatus400() throws Exception {
            PhoneUpdateRequestDto request = buildPhoneUpdateRequestDto(TestPhone.INVALID_PRICE_NEGATIVE);

            mockMvc.perform(put(URL, phoneId1)
                            .header(HttpHeaders.AUTHORIZATION, auth(accessToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void whenBrandIsNull_thenStatus400() throws Exception {
            PhoneUpdateRequestDto request = buildPhoneUpdateRequestDto(TestPhone.INVALID_BRAND_NULL);

            mockMvc.perform(put(URL, phoneId1)
                            .header(HttpHeaders.AUTHORIZATION, auth(accessToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void whenBrandIsBlank_thenStatus400() throws Exception {
            PhoneUpdateRequestDto request = buildPhoneUpdateRequestDto(TestPhone.INVALID_BRAND_BLANK);

            mockMvc.perform(put(URL, phoneId1)
                            .header(HttpHeaders.AUTHORIZATION, auth(accessToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void whenBrandTooShort_thenStatus400() throws Exception {
            PhoneUpdateRequestDto request = buildPhoneUpdateRequestDto(TestPhone.INVALID_BRAND_TOO_SHORT);

            mockMvc.perform(put(URL, phoneId1)
                            .header(HttpHeaders.AUTHORIZATION, auth(accessToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void whenBrandTooLong_thenStatus400() throws Exception {
            PhoneUpdateRequestDto request = buildPhoneUpdateRequestDto(TestPhone.INVALID_BRAND_TOO_LONG);

            mockMvc.perform(put(URL, phoneId1)
                            .header(HttpHeaders.AUTHORIZATION, auth(accessToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void whenReleaseYearIsNull_thenStatus400() throws Exception {
            PhoneUpdateRequestDto request = buildPhoneUpdateRequestDto(TestPhone.INVALID_YEAR_NULL);

            mockMvc.perform(put(URL, phoneId1)
                            .header(HttpHeaders.AUTHORIZATION, auth(accessToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void whenReleaseYearTooEarly_thenStatus400() throws Exception {
            PhoneUpdateRequestDto request = buildPhoneUpdateRequestDto(TestPhone.INVALID_YEAR_TOO_EARLY);

            mockMvc.perform(put(URL, phoneId1)
                            .header(HttpHeaders.AUTHORIZATION, auth(accessToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void whenReleaseYearInFuture_thenStatus400() throws Exception {
            PhoneUpdateRequestDto request = buildPhoneUpdateRequestDto(TestPhone.INVALID_YEAR_FUTURE);

            mockMvc.perform(put(URL, phoneId1)
                            .header(HttpHeaders.AUTHORIZATION, auth(accessToken))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/phones/{phoneId}")
    class DeletePhoneByIdTest {
        private final static String URL = "/api/v1/phones/{phoneId}";

        @Test
        void whenExists_thenStatus204() throws Exception {
            mockMvc.perform(delete(URL, phoneId1)
                            .header(HttpHeaders.AUTHORIZATION, auth(accessToken)))
                    .andExpect(status().isNoContent());
        }

        @Test
        void whenDoesntExist_thenStatus404() throws Exception {
            mockMvc.perform(delete(URL, NON_EXISTING_ID)
                            .header(HttpHeaders.AUTHORIZATION, auth(accessToken)))
                    .andExpect(status().isNotFound());
        }

        @Test
        void whenRequestMissingToken_thenStatus403() throws Exception {
            mockMvc.perform(get(URL, phoneId1))
                    .andExpect(status().isForbidden());
        }

        @Test
        void whenRequestHasInvalidToken_thenStatus403() throws Exception {
            mockMvc.perform(get(URL, phoneId1)
                            .header(HttpHeaders.AUTHORIZATION, auth("some_invalid_text")))
                    .andExpect(status().isForbidden());
        }

    }

    static class TestResources {
        static final long NON_EXISTING_ID = 99_999L;

        static String auth(String token) {
            return "Bearer " + token;
        }

        static PhoneCreateRequestDto buildPhoneCreateRequestDto(TestPhone testPhone) {
            return new PhoneCreateRequestDto(
                    testPhone.name,
                    testPhone.description,
                    testPhone.price,
                    testPhone.brand,
                    testPhone.releaseYear
            );
        }

        static PhoneUpdateRequestDto buildPhoneUpdateRequestDto(TestPhone testPhone) {
            return new PhoneUpdateRequestDto(
                    testPhone.name,
                    testPhone.description,
                    testPhone.price,
                    testPhone.brand,
                    testPhone.releaseYear
            );
        }
    }

    enum TestPhone {
        // Valid phones
        PHONE_1("iPhone 15", "Latest Apple smartphone", new BigDecimal("999.99"), "Apple", 2024),
        PHONE_2("Samsung Galaxy S24", "Flagship Samsung phone", new BigDecimal("899.99"), "Samsung", 2024),
        PHONE_3("Google Pixel 8", "Pure Android experience", new BigDecimal("699.99"), "Google", 2023),

        VALID_PHONE("Valid Phone", "Valid description", new BigDecimal("500.00"), "ValidBrand", 2020),
        VALID_PHONE_NULL_DESCRIPTION("Valid Phone", null, new BigDecimal("500.00"), "ValidBrand", 2020),
        VALID_PHONE_BOUNDARY_MIN("Abc", "", new BigDecimal("0.00"), "Abc", 1970),
        VALID_PHONE_BOUNDARY_MAX("N".repeat(255), "D".repeat(1000), new BigDecimal("99999.99"), "B".repeat(255), 2026),

        // Invalid name
        INVALID_NAME_NULL(null, "description", new BigDecimal("100.00"), "Brand", 2020),
        INVALID_NAME_BLANK("", "description", new BigDecimal("100.00"), "Brand", 2020),
        INVALID_NAME_TOO_SHORT("Ab", "description", new BigDecimal("100.00"), "Brand", 2020),
        INVALID_NAME_TOO_LONG("N".repeat(256), "description", new BigDecimal("100.00"), "Brand", 2020),

        // Invalid description
        INVALID_DESCRIPTION_TOO_LONG("Phone", "D".repeat(1001), new BigDecimal("100.00"), "Brand", 2020),

        // Invalid price
        INVALID_PRICE_NULL("Phone", "description", null, "Brand", 2020),
        INVALID_PRICE_NEGATIVE("Phone", "description", new BigDecimal("-0.01"), "Brand", 2020),

        // Invalid brand
        INVALID_BRAND_NULL("Phone", "description", new BigDecimal("100.00"), null, 2020),
        INVALID_BRAND_BLANK("Phone", "description", new BigDecimal("100.00"), "", 2020),
        INVALID_BRAND_TOO_SHORT("Phone", "description", new BigDecimal("100.00"), "Ab", 2020),
        INVALID_BRAND_TOO_LONG("Phone", "description", new BigDecimal("100.00"), "B".repeat(256), 2020),

        // Invalid releaseYear
        INVALID_YEAR_NULL("Phone", "description", new BigDecimal("100.00"), "Brand", null),
        INVALID_YEAR_TOO_EARLY("Phone", "description", new BigDecimal("100.00"), "Brand", 1969),
        INVALID_YEAR_FUTURE("Phone", "description", new BigDecimal("100.00"), "Brand", 2027);

        private final String name;
        private final String description;
        private final BigDecimal price;
        private final String brand;
        private final Integer releaseYear;

        TestPhone(String name, String description, BigDecimal price, String brand, Integer releaseYear) {
            this.name = name;
            this.description = description;
            this.price = price;
            this.brand = brand;
            this.releaseYear = releaseYear;
        }
    }

}
