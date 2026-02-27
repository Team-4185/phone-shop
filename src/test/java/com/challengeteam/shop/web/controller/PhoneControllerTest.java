package com.challengeteam.shop.web.controller;

import com.challengeteam.shop.dto.phone.PhoneCreateRequestDto;
import com.challengeteam.shop.dto.phone.PhoneUpdateRequestDto;
import com.challengeteam.shop.entity.image.Image;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.challengeteam.shop.web.controller.PhoneControllerTest.TestPhone.VALID_PHONE_BOUNDARY_MAX;
import static com.challengeteam.shop.web.controller.PhoneControllerTest.TestPhone.VALID_PHONE_BOUNDARY_MIN;
import static com.challengeteam.shop.web.controller.PhoneControllerTest.TestResources.*;
import static org.hamcrest.Matchers.*;
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
    private String token;
    private Long phone1;
    private Long phone2;
    private Long phone3;

    @DynamicPropertySource
    static void loadPropertiesForTest(DynamicPropertyRegistry propertyRegistry) {
        TestContextConfigurator.initRequiredProperties(propertyRegistry);
    }

    @BeforeEach
    public void setup() {
        // clear all
        phoneRepository.deleteAll();

        // add 3 phones
        phone1 = phoneService.create(
                buildPhoneCreateRequestDto(TestPhone.PHONE_1),
                new ArrayList<>()
        );
        phone2 = phoneService.create(
                buildPhoneCreateRequestDto(TestPhone.PHONE_2),
                List.of(buildMultipartFile("images"), buildMultipartFile("images"))
        );
        phone3 = phoneService.create(
                buildPhoneCreateRequestDto(TestPhone.PHONE_3),
                List.of(buildMultipartFile("images"))
        );

        // authorize
        token = testAuthHelper.authorizeLikeTestUser();

    }

    @Nested
    @DisplayName("GET /api/v1/phones")
    class GetPhonesTest {
        private final static String URL = "/api/v1/phones";

        @Test
        void whenValidRequest_thenStatus200AndReturnPagedPhones() throws Exception {
            mockMvc.perform(get(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth(token)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content", hasSize(3)))
                    .andExpect(jsonPath("$.content[*].images").exists())
                    .andExpect(jsonPath("$.totalElements").value(3))
                    .andExpect(jsonPath("$.totalPages").value(1))
                    .andExpect(jsonPath("$.size").value(10))
                    .andExpect(jsonPath("$.page").value(1))
                    .andExpect(jsonPath("$.first").value(true))
                    .andExpect(jsonPath("$.last").value(true));
        }

        @Test
        void whenRequestWithPagination_thenReturnCorrectPage() throws Exception {
            mockMvc.perform(get(URL)
                            .param("page", "1")
                            .param("size", "2")
                            .header(HttpHeaders.AUTHORIZATION, auth(token)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(2)))
                    .andExpect(jsonPath("$.content[*].images").exists())
                    .andExpect(jsonPath("$.totalElements").value(3))
                    .andExpect(jsonPath("$.totalPages").value(2))
                    .andExpect(jsonPath("$.size").value(2))
                    .andExpect(jsonPath("$.page").value(1));
        }

        @Test
        void whenRequestSecondPage_thenReturnCorrectPage() throws Exception {
            mockMvc.perform(get(URL)
                            .param("page", "2")
                            .param("size", "2")
                            .header(HttpHeaders.AUTHORIZATION, auth(token)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[*].images").exists())
                    .andExpect(jsonPath("$.totalElements").value(3))
                    .andExpect(jsonPath("$.totalPages").value(2))
                    .andExpect(jsonPath("$.size").value(2))
                    .andExpect(jsonPath("$.page").value(2));
        }

        @Test
        void whenRequestEmptyPage_thenReturnEmptyContent() throws Exception {
            mockMvc.perform(get(URL)
                            .param("page", "2")
                            .param("size", "3")
                            .header(HttpHeaders.AUTHORIZATION, auth(token)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(0)))
                    .andExpect(jsonPath("$.totalElements").value(3))
                    .andExpect(jsonPath("$.page").value(2));
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

        @Test
        void whenPageOutOfBounds_thenStatus400() throws Exception {
            mockMvc.perform(get(URL)
                            .param("page", "0")
                            .param("size", "10")
                            .header(HttpHeaders.AUTHORIZATION, auth(token)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void whenSizeOutOfBounds_thenStatus400() throws Exception {
            mockMvc.perform(get(URL)
                            .param("page", "1")
                            .param("size", "0")
                            .header(HttpHeaders.AUTHORIZATION, auth(token)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void whenPageIsTooHigh_thenStatus400() throws Exception {
            mockMvc.perform(get(URL)
                            .param("page", "1000000000")
                            .param("size", "1")
                            .header(HttpHeaders.AUTHORIZATION, auth(token)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void whenSizeIsTooHigh_thenStatus400() throws Exception {
            mockMvc.perform(get(URL)
                            .param("page", "1")
                            .param("size", "101")
                            .header(HttpHeaders.AUTHORIZATION, auth(token)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void whenPageIsNotInteger_thenStatus400() throws Exception {
            mockMvc.perform(get(URL)
                            .param("page", "word")
                            .param("size", "1")
                            .header(HttpHeaders.AUTHORIZATION, auth(token)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void whenSizeIsNotInteger_thenStatus400() throws Exception {
            mockMvc.perform(get(URL)
                            .param("page", "1")
                            .param("size", "word")
                            .header(HttpHeaders.AUTHORIZATION, auth(token)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void whenFilterByBrand_thenReturnOnlyMatchingPhones() throws Exception {
            mockMvc.perform(get(URL)
                            .param("brand", "Apple")
                            .header(HttpHeaders.AUTHORIZATION, auth(token)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].brand").value("Apple"))
                    .andExpect(jsonPath("$.content[0].name").value("iPhone 15"))
                    .andExpect(jsonPath("$.content[*].images").exists());
        }

        @Test
        void whenFilterByMinPrice_thenReturnPhonesAbovePrice() throws Exception {
            mockMvc.perform(get(URL)
                            .param("minPrice", "800.00")
                            .header(HttpHeaders.AUTHORIZATION, auth(token)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content", hasSize(2)))
                    .andExpect(jsonPath("$.content[*].price", everyItem(greaterThanOrEqualTo(800.00))))
                    .andExpect(jsonPath("$.content[*].images").exists());
        }

        @Test
        void whenFilterByMaxPrice_thenReturnPhonesBelowPrice() throws Exception {
            mockMvc.perform(get(URL)
                            .param("maxPrice", "900.00")
                            .header(HttpHeaders.AUTHORIZATION, auth(token)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content", hasSize(2)))
                    .andExpect(jsonPath("$.content[*].price", everyItem(lessThanOrEqualTo(900.00))))
                    .andExpect(jsonPath("$.content[*].images").exists());
        }

        @Test
        void whenFilterByPriceRange_thenReturnPhonesInRange() throws Exception {
            mockMvc.perform(get(URL)
                            .param("minPrice", "700.00")
                            .param("maxPrice", "900.00")
                            .header(HttpHeaders.AUTHORIZATION, auth(token)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].name").value("Samsung Galaxy S24"))
                    .andExpect(jsonPath("$.content[0].price").value(899.99))
                    .andExpect(jsonPath("$.content[*].images").exists());
        }

        @Test
        void whenFilterByCombination_thenReturnMatchingPhones() throws Exception {
            mockMvc.perform(get(URL)
                            .param("brand", "Samsung")
                            .param("minPrice", "100.00")
                            .param("maxPrice", "2000.00")
                            .header(HttpHeaders.AUTHORIZATION, auth(token)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].brand").value("Samsung"))
                    .andExpect(jsonPath("$.content[0].name").value("Samsung Galaxy S24"))
                    .andExpect(jsonPath("$.content[*].images").exists());
        }

        @Test
        void whenMinPriceIsNegative_thenStatus400() throws Exception {
            mockMvc.perform(get(URL)
                            .param("minPrice", "-10.00")
                            .header(HttpHeaders.AUTHORIZATION, auth(token)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void whenMaxPriceIsNegative_thenStatus400() throws Exception {
            mockMvc.perform(get(URL)
                            .param("maxPrice", "-50.00")
                            .header(HttpHeaders.AUTHORIZATION, auth(token)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void whenMinPriceIsNotDecimal_thenStatus400() throws Exception {
            mockMvc.perform(get(URL)
                            .param("minPrice", "invalid")
                            .header(HttpHeaders.AUTHORIZATION, auth(token)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void whenMaxPriceIsNotDecimal_thenStatus400() throws Exception {
            mockMvc.perform(get(URL)
                            .param("maxPrice", "invalid")
                            .header(HttpHeaders.AUTHORIZATION, auth(token)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void whenSortByNameAsc_thenReturnSortedPhones() throws Exception {
            mockMvc.perform(get(URL)
                            .param("sort", "name_asc")
                            .header(HttpHeaders.AUTHORIZATION, auth(token)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content", hasSize(3)))
                    .andExpect(jsonPath("$.content[0].name").value("Google Pixel 8"))
                    .andExpect(jsonPath("$.content[1].name").value("iPhone 15"))
                    .andExpect(jsonPath("$.content[2].name").value("Samsung Galaxy S24"))
                    .andExpect(jsonPath("$.content[*].images").exists());
        }

        @Test
        void whenSortByNameDesc_thenReturnSortedPhones() throws Exception {
            mockMvc.perform(get(URL)
                            .param("sort", "name_desc")
                            .header(HttpHeaders.AUTHORIZATION, auth(token)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content", hasSize(3)))
                    .andExpect(jsonPath("$.content[0].name").value("Samsung Galaxy S24"))
                    .andExpect(jsonPath("$.content[1].name").value("iPhone 15"))
                    .andExpect(jsonPath("$.content[2].name").value("Google Pixel 8"))
                    .andExpect(jsonPath("$.content[*].images").exists());
        }

        @Test
        void whenSortByPriceAsc_thenReturnSortedPhones() throws Exception {
            mockMvc.perform(get(URL)
                            .param("sort", "price_asc")
                            .header(HttpHeaders.AUTHORIZATION, auth(token)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content", hasSize(3)))
                    .andExpect(jsonPath("$.content[0].price").value(699.99))
                    .andExpect(jsonPath("$.content[0].name").value("Google Pixel 8"))
                    .andExpect(jsonPath("$.content[1].price").value(899.99))
                    .andExpect(jsonPath("$.content[1].name").value("Samsung Galaxy S24"))
                    .andExpect(jsonPath("$.content[2].price").value(999.99))
                    .andExpect(jsonPath("$.content[2].name").value("iPhone 15"))
                    .andExpect(jsonPath("$.content[*].images").exists());
        }

        @Test
        void whenSortByPriceDesc_thenReturnSortedPhones() throws Exception {
            mockMvc.perform(get(URL)
                            .param("sort", "price_desc")
                            .header(HttpHeaders.AUTHORIZATION, auth(token)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content", hasSize(3)))
                    .andExpect(jsonPath("$.content[0].price").value(999.99))
                    .andExpect(jsonPath("$.content[0].name").value("iPhone 15"))
                    .andExpect(jsonPath("$.content[1].price").value(899.99))
                    .andExpect(jsonPath("$.content[1].name").value("Samsung Galaxy S24"))
                    .andExpect(jsonPath("$.content[2].price").value(699.99))
                    .andExpect(jsonPath("$.content[2].name").value("Google Pixel 8"))
                    .andExpect(jsonPath("$.content[*].images").exists());
        }

        @Test
        void whenNoSortProvided_thenDefaultToNameAsc() throws Exception {
            mockMvc.perform(get(URL)
                            .header(HttpHeaders.AUTHORIZATION, auth(token)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content", hasSize(3)))
                    .andExpect(jsonPath("$.content[0].name").value("Google Pixel 8"))
                    .andExpect(jsonPath("$.content[1].name").value("iPhone 15"))
                    .andExpect(jsonPath("$.content[2].name").value("Samsung Galaxy S24"))
                    .andExpect(jsonPath("$.content[*].images").exists());
        }

        @Test
        void whenSortIsEmpty_thenDefaultToNameAsc() throws Exception {
            mockMvc.perform(get(URL)
                            .param("sort", "")
                            .header(HttpHeaders.AUTHORIZATION, auth(token)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content", hasSize(3)))
                    .andExpect(jsonPath("$.content[0].name").value("Google Pixel 8"))
                    .andExpect(jsonPath("$.content[1].name").value("iPhone 15"))
                    .andExpect(jsonPath("$.content[2].name").value("Samsung Galaxy S24"))
                    .andExpect(jsonPath("$.content[*].images").exists());
        }

        @Test
        void whenSortIsBlank_thenDefaultToNameAsc() throws Exception {
            mockMvc.perform(get(URL)
                            .param("sort", "   ")
                            .header(HttpHeaders.AUTHORIZATION, auth(token)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content", hasSize(3)))
                    .andExpect(jsonPath("$.content[0].name").value("Google Pixel 8"))
                    .andExpect(jsonPath("$.content[1].name").value("iPhone 15"))
                    .andExpect(jsonPath("$.content[2].name").value("Samsung Galaxy S24"))
                    .andExpect(jsonPath("$.content[*].images").exists());
        }

        @Test
        void whenSortIsInvalid_thenStatus400() throws Exception {
            mockMvc.perform(get(URL)
                            .param("sort", "invalid_sort")
                            .header(HttpHeaders.AUTHORIZATION, auth(token)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void whenSortWithWrongFormat_thenStatus400() throws Exception {
            mockMvc.perform(get(URL)
                            .param("sort", "name")
                            .header(HttpHeaders.AUTHORIZATION, auth(token)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void whenSortWithRandomText_thenStatus400() throws Exception {
            mockMvc.perform(get(URL)
                            .param("sort", "random_value")
                            .header(HttpHeaders.AUTHORIZATION, auth(token)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void whenCombinedFilterAndSort_thenReturnCorrectResults() throws Exception {
            mockMvc.perform(get(URL)
                            .param("brand", "Apple")
                            .param("minPrice", "100.00")
                            .param("maxPrice", "2000.00")
                            .param("sort", "price_asc")
                            .header(HttpHeaders.AUTHORIZATION, auth(token)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].brand").value("Apple"))
                    .andExpect(jsonPath("$.content[0].name").value("iPhone 15"))
                    .andExpect(jsonPath("$.content[0].price").value(999.99))
                    .andExpect(jsonPath("$.content[*].images").exists());
        }

        @Test
        void whenCombinedFilterSortAndPagination_thenReturnCorrectResults() throws Exception {
            mockMvc.perform(get(URL)
                            .param("page", "1")
                            .param("size", "2")
                            .param("sort", "name_desc")
                            .header(HttpHeaders.AUTHORIZATION, auth(token)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content", hasSize(2)))
                    .andExpect(jsonPath("$.size").value(2))
                    .andExpect(jsonPath("$.page").value(1))
                    .andExpect(jsonPath("$.content[0].name").value("Samsung Galaxy S24"))
                    .andExpect(jsonPath("$.content[1].name").value("iPhone 15"))
                    .andExpect(jsonPath("$.content[*].images").exists());
        }

        @Test
        void whenAllParametersCombined_thenReturnCorrectResults() throws Exception {
            mockMvc.perform(get(URL)
                            .param("page", "1")
                            .param("size", "5")
                            .param("minPrice", "800.00")
                            .param("maxPrice", "1500.00")
                            .param("sort", "price_desc")
                            .header(HttpHeaders.AUTHORIZATION, auth(token)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content", hasSize(2)))
                    .andExpect(jsonPath("$.content[0].name").value("iPhone 15"))
                    .andExpect(jsonPath("$.content[0].price").value(999.99))
                    .andExpect(jsonPath("$.content[1].name").value("Samsung Galaxy S24"))
                    .andExpect(jsonPath("$.content[1].price").value(899.99))
                    .andExpect(jsonPath("$.content[*].images").exists());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/phones/{phoneId}")
    class GetPhoneByIdTest {
        private final static String URL = "/api/v1/phones/{phoneId}";

        @Test
        void whenExists_thenStatus200AndReturnPhoneResponseDto() throws Exception {
            mockMvc.perform(get(URL, phone1)
                            .header(HttpHeaders.AUTHORIZATION, auth(token)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(phone1))
                    .andExpect(jsonPath("$.name").value(TestPhone.PHONE_1.name))
                    .andExpect(jsonPath("$.description").value(TestPhone.PHONE_1.description))
                    .andExpect(jsonPath("$.price").value(TestPhone.PHONE_1.price))
                    .andExpect(jsonPath("$.brand").value(TestPhone.PHONE_1.brand))
                    .andExpect(jsonPath("$.releaseYear").value(TestPhone.PHONE_1.releaseYear))
                    .andExpect(jsonPath("$.cpu").value(TestPhone.PHONE_1.cpu))
                    .andExpect(jsonPath("$.coresNumber").value(TestPhone.PHONE_1.coresNumber))
                    .andExpect(jsonPath("$.screenSize").value(TestPhone.PHONE_1.screenSize))
                    .andExpect(jsonPath("$.frontCamera").value(TestPhone.PHONE_1.frontCamera))
                    .andExpect(jsonPath("$.mainCamera").value(TestPhone.PHONE_1.mainCamera))
                    .andExpect(jsonPath("$.batteryCapacity").value(TestPhone.PHONE_1.batteryCapacity))
                    .andExpect(jsonPath("$.images").isArray())
                    .andExpect(jsonPath("$.images", hasSize(0)));
        }

        @Test
        void whenPhoneHasImages_thenReturnPhoneWithImages() throws Exception {
            mockMvc.perform(get(URL, phone2)
                            .header(HttpHeaders.AUTHORIZATION, auth(token)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.images").isArray())
                    .andExpect(jsonPath("$.images", hasSize(2)))
                    .andExpect(jsonPath("$.images[0].id").exists())
                    .andExpect(jsonPath("$.images[0].name").exists())
                    .andExpect(jsonPath("$.images[0].url").exists())
                    .andExpect(jsonPath("$.images[0].size").exists())
                    .andExpect(jsonPath("$.images[0].mimeType").exists());
        }

        @Test
        void whenDoesntExist_thenStatus404() throws Exception {
            mockMvc.perform(get(URL, NON_EXISTING_ID)
                            .header(HttpHeaders.AUTHORIZATION, auth(token)))
                    .andExpect(status().isNotFound());
        }

        @Test
        void whenRequestMissingToken_thenStatus403() throws Exception {
            mockMvc.perform(get(URL, phone1))
                    .andExpect(status().isForbidden());
        }

        @Test
        void whenRequestHasInvalidToken_thenStatus403() throws Exception {
            mockMvc.perform(get(URL, phone1)
                            .header(HttpHeaders.AUTHORIZATION, auth("some_invalid_text")))
                    .andExpect(status().isForbidden());
        }

        @Test
        void whenIdIsNotInteger_thenStatus404() throws Exception {
            var request = get(URL, "not_integer")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.AUTHORIZATION, auth(token));

            mockMvc.perform(request)
                    .andExpect(status().isNotFound());
        }

    }

    @Nested
    @DisplayName("POST /api/v1/phones")
    class CreatePhoneTest {
        private final static String URL = "/api/v1/phones";

        @Test
        void whenValidRequest_thenStatus201AndLocationHeader() throws Exception {
            PhoneCreateRequestDto json = buildPhoneCreateRequestDto(TestPhone.VALID_PHONE);
            byte[] content = objectMapper.writeValueAsBytes(json);

            var request = multipart(URL)
                    .file((MockMultipartFile) buildJsonLikeMultipartFile(content, "phone"))
                    .file((MockMultipartFile) buildMultipartFile("images"))
                    .header(HttpHeaders.AUTHORIZATION, auth(token));

            mockMvc.perform(request)
                    .andExpect(status().isCreated())
                    .andExpect(header().exists(HttpHeaders.LOCATION));
        }

        @Test
        void whenValidRequestWithNullDescription_thenStatus201() throws Exception {
            PhoneCreateRequestDto json = buildPhoneCreateRequestDto(TestPhone.VALID_PHONE_NULL_DESCRIPTION);
            byte[] content = objectMapper.writeValueAsBytes(json);

            var request = multipart(URL)
                    .file((MockMultipartFile) buildJsonLikeMultipartFile(content, "phone"))
                    .file((MockMultipartFile) buildMultipartFile("images"))
                    .header(HttpHeaders.AUTHORIZATION, auth(token));

            mockMvc.perform(request)
                    .andExpect(status().isCreated())
                    .andExpect(header().exists(HttpHeaders.LOCATION));
        }

        @Test
        void whenValidRequestWithBoundaryMinValues_thenStatus201() throws Exception {
            PhoneCreateRequestDto json = buildPhoneCreateRequestDto(VALID_PHONE_BOUNDARY_MIN);
            byte[] content = objectMapper.writeValueAsBytes(json);

            var request = multipart(URL)
                    .file((MockMultipartFile) buildJsonLikeMultipartFile(content, "phone"))
                    .file((MockMultipartFile) buildMultipartFile("images"))
                    .header(HttpHeaders.AUTHORIZATION, auth(token));

            mockMvc.perform(request)
                    .andExpect(status().isCreated())
                    .andExpect(header().exists(HttpHeaders.LOCATION));
        }

        @Test
        void whenValidRequestWithBoundaryMaxValues_thenStatus201() throws Exception {
            PhoneCreateRequestDto json = buildPhoneCreateRequestDto(VALID_PHONE_BOUNDARY_MAX);
            byte[] content = objectMapper.writeValueAsBytes(json);

            var request = multipart(URL)
                    .file((MockMultipartFile) buildJsonLikeMultipartFile(content, "phone"))
                    .file((MockMultipartFile) buildMultipartFile("images"))
                    .header(HttpHeaders.AUTHORIZATION, auth(token));

            mockMvc.perform(request)
                    .andExpect(status().isCreated())
                    .andExpect(header().exists(HttpHeaders.LOCATION));
        }

        @Test
        void whenRequestMissingToken_thenStatus403() throws Exception {
            PhoneCreateRequestDto json = buildPhoneCreateRequestDto(TestPhone.VALID_PHONE);
            byte[] content = objectMapper.writeValueAsBytes(json);

            var request = multipart(URL)
                    .file((MockMultipartFile) buildJsonLikeMultipartFile(content, "phone"))
                    .file((MockMultipartFile) buildMultipartFile("images"));

            mockMvc.perform(request)
                    .andExpect(status().isForbidden());
        }

        @Test
        void whenRequestHasInvalidToken_thenStatus403() throws Exception {
            PhoneCreateRequestDto json = buildPhoneCreateRequestDto(TestPhone.VALID_PHONE);
            byte[] content = objectMapper.writeValueAsBytes(json);

            var request = multipart(URL)
                    .file((MockMultipartFile) buildJsonLikeMultipartFile(content, "phone"))
                    .file((MockMultipartFile) buildMultipartFile("images"))
                    .header(HttpHeaders.AUTHORIZATION, auth("some_invalid_text"));

            mockMvc.perform(request)
                    .andExpect(status().isForbidden());
        }

        // Validation tests
        @Test
        void whenNameIsNull_thenStatus400() throws Exception {
            expect400WithInvalidBody(TestPhone.INVALID_NAME_NULL);
        }

        @Test
        void whenNameIsBlank_thenStatus400() throws Exception {
            expect400WithInvalidBody(TestPhone.INVALID_NAME_BLANK);
        }

        @Test
        void whenNameTooShort_thenStatus400() throws Exception {
            expect400WithInvalidBody(TestPhone.INVALID_NAME_TOO_SHORT);
        }

        @Test
        void whenNameTooLong_thenStatus400() throws Exception {
            expect400WithInvalidBody(TestPhone.INVALID_NAME_TOO_LONG);
        }

        @Test
        void whenDescriptionTooLong_thenStatus400() throws Exception {
            expect400WithInvalidBody(TestPhone.INVALID_DESCRIPTION_TOO_LONG);
        }

        @Test
        void whenPriceIsNull_thenStatus400() throws Exception {
            expect400WithInvalidBody(TestPhone.INVALID_PRICE_NULL);
        }

        @Test
        void whenPriceIsNegative_thenStatus400() throws Exception {
            expect400WithInvalidBody(TestPhone.INVALID_PRICE_NEGATIVE);
        }

        @Test
        void whenBrandIsNull_thenStatus400() throws Exception {
            expect400WithInvalidBody(TestPhone.INVALID_BRAND_NULL);
        }

        @Test
        void whenBrandIsBlank_thenStatus400() throws Exception {
            expect400WithInvalidBody(TestPhone.INVALID_BRAND_BLANK);
        }

        @Test
        void whenBrandTooShort_thenStatus400() throws Exception {
            expect400WithInvalidBody(TestPhone.INVALID_BRAND_TOO_SHORT);
        }

        @Test
        void whenBrandTooLong_thenStatus400() throws Exception {
            expect400WithInvalidBody(TestPhone.INVALID_BRAND_TOO_LONG);
        }

        @Test
        void whenReleaseYearIsNull_thenStatus400() throws Exception {
            expect400WithInvalidBody(TestPhone.INVALID_YEAR_NULL);
        }

        @Test
        void whenReleaseYearTooEarly_thenStatus400() throws Exception {
            expect400WithInvalidBody(TestPhone.INVALID_YEAR_TOO_EARLY);
        }

        @Test
        void whenReleaseYearInFuture_thenStatus400() throws Exception {
            expect400WithInvalidBody(TestPhone.INVALID_YEAR_FUTURE);
        }

        @Test
        void whenCpuIsNull_thenStatus400() throws Exception {
            expect400WithInvalidBody(TestPhone.INVALID_CPU_NULL);
        }

        @Test
        void whenCpuIsBlank_thenStatus400() throws Exception {
            expect400WithInvalidBody(TestPhone.INVALID_CPU_BLANK);
        }

        @Test
        void whenCpuIsTooLong_thenStatus400() throws Exception {
            expect400WithInvalidBody(TestPhone.INVALID_CPU_TOO_LONG);
        }

        @Test
        void whenCoresNumberIsNull_thenStatus400() throws Exception {
            expect400WithInvalidBody(TestPhone.INVALID_CORES_NULL);
        }

        @Test
        void whenCoresNumberIsTooSmall_thenStatus400() throws Exception {
            expect400WithInvalidBody(TestPhone.INVALID_CORES_TOO_SMALL);
        }

        @Test
        void whenCoresNumberIsTooBig_thenStatus400() throws Exception {
            expect400WithInvalidBody(TestPhone.INVALID_CORES_TOO_BIG);
        }

        @Test
        void whenScreenSizeIsNull_thenStatus400() throws Exception {
            expect400WithInvalidBody(TestPhone.INVALID_SCREEN_NULL);
        }

        @Test
        void whenScreenSizeIsBlank_thenStatus400() throws Exception {
            expect400WithInvalidBody(TestPhone.INVALID_SCREEN_BLANK);
        }

        @Test
        void whenScreenSizeWithoutQuotes_thenStatus400() throws Exception {
            expect400WithInvalidBody(TestPhone.INVALID_SCREEN_NO_QUOTES);
        }

        @Test
        void whenScreenSizeWithText_thenStatus400() throws Exception {
            expect400WithInvalidBody(TestPhone.INVALID_SCREEN_WITH_TEXT);
        }

        @Test
        void whenFrontCameraIsNull_thenStatus400() throws Exception {
            expect400WithInvalidBody(TestPhone.INVALID_FRONT_CAMERA_NULL);
        }

        @Test
        void whenFrontCameraIsBlank_thenStatus400() throws Exception {
            expect400WithInvalidBody(TestPhone.INVALID_FRONT_CAMERA_BLANK);
        }

        @Test
        void whenFrontCameraIsDecimal_thenStatus400() throws Exception {
            expect400WithInvalidBody(TestPhone.INVALID_FRONT_CAMERA_DECIMAL);
        }

        @Test
        void whenFrontCameraWithoutUnit_thenStatus400() throws Exception {
            expect400WithInvalidBody(TestPhone.INVALID_FRONT_CAMERA_NO_UNIT);
        }

        @Test
        void whenMainCameraIsNull_thenStatus400() throws Exception {
            expect400WithInvalidBody(TestPhone.INVALID_MAIN_CAMERA_NULL);
        }

        @Test
        void whenMainCameraIsBlank_thenStatus400() throws Exception {
            expect400WithInvalidBody(TestPhone.INVALID_MAIN_CAMERA_BLANK);
        }

        @Test
        void whenMainCameraIsDecimal_thenStatus400() throws Exception {
            expect400WithInvalidBody(TestPhone.INVALID_MAIN_CAMERA_DECIMAL);
        }

        @Test
        void whenMainCameraWithoutUnit_thenStatus400() throws Exception {
            expect400WithInvalidBody(TestPhone.INVALID_MAIN_CAMERA_NO_UNIT);
        }

        @Test
        void whenMainCameraWithWrongFormat_thenStatus400() throws Exception {
            expect400WithInvalidBody(TestPhone.INVALID_MAIN_CAMERA_WRONG_FORMAT);
        }

        @Test
        void whenBatteryCapacityIsNull_thenStatus400() throws Exception {
            expect400WithInvalidBody(TestPhone.INVALID_BATTERY_NULL);
        }

        @Test
        void whenBatteryCapacityIsBlank_thenStatus400() throws Exception {
            expect400WithInvalidBody(TestPhone.INVALID_BATTERY_BLANK);
        }

        @Test
        void whenBatteryCapacityWithoutUnit_thenStatus400() throws Exception {
            expect400WithInvalidBody(TestPhone.INVALID_BATTERY_NO_UNIT);
        }

        @Test
        void whenBatteryCapacityWithWrongUnit_thenStatus400() throws Exception {
            expect400WithInvalidBody(TestPhone.INVALID_BATTERY_WRONG_UNIT);
        }

        @Test
        void whenCreateWithoutImages_thenReturn201() throws Exception {
            PhoneCreateRequestDto json = buildPhoneCreateRequestDto(TestPhone.VALID_PHONE);
            byte[] content = objectMapper.writeValueAsBytes(json);

            var request = multipart(URL)
                    .file((MockMultipartFile) buildJsonLikeMultipartFile(content, "phone"))
                    .header(HttpHeaders.AUTHORIZATION, auth(token));

            mockMvc.perform(request)
                    .andExpect(status().isCreated());
        }

        @Test
        void whenCreateWithMultipleImages_thenReturn201() throws Exception {
            PhoneCreateRequestDto json = buildPhoneCreateRequestDto(TestPhone.VALID_PHONE);
            byte[] content = objectMapper.writeValueAsBytes(json);

            var request = multipart(URL)
                    .file((MockMultipartFile) buildJsonLikeMultipartFile(content, "phone"))
                    .file((MockMultipartFile) buildMultipartFile("images"))
                    .file((MockMultipartFile) buildMultipartFile("images"))
                    .file((MockMultipartFile) buildMultipartFile("images"))
                    .header(HttpHeaders.AUTHORIZATION, auth(token));

            mockMvc.perform(request)
                    .andExpect(status().isCreated());
        }

        @Test
        void whenCreateWithUnsupportedImage_thenReturn400() throws Exception {
            PhoneCreateRequestDto json = buildPhoneCreateRequestDto(TestPhone.VALID_PHONE);
            byte[] content = objectMapper.writeValueAsBytes(json);

            var request = multipart(URL)
                    .file((MockMultipartFile) buildJsonLikeMultipartFile(content, "phone"))
                    .file((MockMultipartFile) buildMultipartFile("images"))
                    .file((MockMultipartFile) buildUnsupportedMultipartFile("images"))      // unsupported file
                    .file((MockMultipartFile) buildMultipartFile("images"))
                    .header(HttpHeaders.AUTHORIZATION, auth(token));

            mockMvc.perform(request)
                    .andExpect(status().isBadRequest());
        }

        private void expect400WithInvalidBody(TestPhone phone) throws Exception {
            PhoneCreateRequestDto json = buildPhoneCreateRequestDto(phone);
            byte[] content = objectMapper.writeValueAsBytes(json);

            var request = multipart(URL)
                    .file((MockMultipartFile) buildJsonLikeMultipartFile(content, "phone"))
                    .file((MockMultipartFile) buildMultipartFile("images"))
                    .header(HttpHeaders.AUTHORIZATION, auth(token));

            mockMvc.perform(request)
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

            mockMvc.perform(put(URL, phone1)
                            .header(HttpHeaders.AUTHORIZATION, auth(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNoContent());
        }

        @Test
        void whenValidRequestWithNullDescription_thenStatus204() throws Exception {
            PhoneUpdateRequestDto request = buildPhoneUpdateRequestDto(TestPhone.VALID_PHONE_NULL_DESCRIPTION);

            mockMvc.perform(put(URL, phone1)
                            .header(HttpHeaders.AUTHORIZATION, auth(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNoContent());
        }

        @Test
        void whenValidRequestWithBoundaryMinValues_thenStatus204() throws Exception {
            PhoneUpdateRequestDto request = buildPhoneUpdateRequestDto(VALID_PHONE_BOUNDARY_MIN);

            mockMvc.perform(put(URL, phone1)
                            .header(HttpHeaders.AUTHORIZATION, auth(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNoContent());
        }

        @Test
        void whenValidRequestWithBoundaryMaxValues_thenStatus204() throws Exception {
            PhoneUpdateRequestDto request = buildPhoneUpdateRequestDto(VALID_PHONE_BOUNDARY_MAX);

            mockMvc.perform(put(URL, phone1)
                            .header(HttpHeaders.AUTHORIZATION, auth(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNoContent());
        }

        @Test
        void whenRequestMissingToken_thenStatus403() throws Exception {
            PhoneUpdateRequestDto request = buildPhoneUpdateRequestDto(TestPhone.VALID_PHONE);

            mockMvc.perform(put(URL, phone1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        void whenRequestHasInvalidToken_thenStatus403() throws Exception {
            PhoneUpdateRequestDto request = buildPhoneUpdateRequestDto(TestPhone.VALID_PHONE);

            mockMvc.perform(put(URL, phone1)
                            .header(HttpHeaders.AUTHORIZATION, auth("some_invalid_text"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        void whenIdIsNotInteger_thenStatus404() throws Exception {
            var request = put(URL, "not_integer")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.AUTHORIZATION, auth(token));

            mockMvc.perform(request)
                    .andExpect(status().isNotFound());
        }

        // Validation tests
        @Test
        void whenNameIsNull_thenStatus204() throws Exception {
            PhoneUpdateRequestDto request = buildPhoneUpdateRequestDto(TestPhone.INVALID_NAME_NULL);

            mockMvc.perform(put(URL, phone1)
                            .header(HttpHeaders.AUTHORIZATION, auth(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNoContent());
        }

        @Test
        void whenNameIsBlank_thenStatus400() throws Exception {
            expect400WithInvalidBody(TestPhone.INVALID_NAME_BLANK);
        }

        @Test
        void whenNameTooShort_thenStatus400() throws Exception {
            expect400WithInvalidBody(TestPhone.INVALID_NAME_TOO_SHORT);
        }

        @Test
        void whenNameTooLong_thenStatus400() throws Exception {
            expect400WithInvalidBody(TestPhone.INVALID_NAME_TOO_LONG);
        }

        @Test
        void whenDescriptionTooLong_thenStatus400() throws Exception {
            expect400WithInvalidBody(TestPhone.INVALID_DESCRIPTION_TOO_LONG);
        }

        @Test
        void whenPriceIsNull_thenStatus204() throws Exception {
            PhoneUpdateRequestDto request = buildPhoneUpdateRequestDto(TestPhone.INVALID_PRICE_NULL);

            mockMvc.perform(put(URL, phone1)
                            .header(HttpHeaders.AUTHORIZATION, auth(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNoContent());
        }

        @Test
        void whenPriceIsNegative_thenStatus400() throws Exception {
            expect400WithInvalidBody(TestPhone.INVALID_PRICE_NEGATIVE);
        }

        @Test
        void whenBrandIsNull_thenStatus204() throws Exception {
            PhoneUpdateRequestDto request = buildPhoneUpdateRequestDto(TestPhone.INVALID_BRAND_NULL);

            mockMvc.perform(put(URL, phone1)
                            .header(HttpHeaders.AUTHORIZATION, auth(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNoContent());
        }

        @Test
        void whenBrandIsBlank_thenStatus400() throws Exception {
            expect400WithInvalidBody(TestPhone.INVALID_BRAND_BLANK);
        }

        @Test
        void whenBrandTooShort_thenStatus400() throws Exception {
            expect400WithInvalidBody(TestPhone.INVALID_BRAND_TOO_SHORT);
        }

        @Test
        void whenBrandTooLong_thenStatus400() throws Exception {
            expect400WithInvalidBody(TestPhone.INVALID_BRAND_TOO_LONG);
        }

        @Test
        void whenReleaseYearIsNull_thenStatus204() throws Exception {
            PhoneUpdateRequestDto request = buildPhoneUpdateRequestDto(TestPhone.INVALID_YEAR_NULL);

            mockMvc.perform(put(URL, phone1)
                            .header(HttpHeaders.AUTHORIZATION, auth(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNoContent());
        }

        @Test
        void whenReleaseYearTooEarly_thenStatus400() throws Exception {
            expect400WithInvalidBody(TestPhone.INVALID_YEAR_TOO_EARLY);
        }

        @Test
        void whenReleaseYearInFuture_thenStatus400() throws Exception {
            expect400WithInvalidBody(TestPhone.INVALID_YEAR_FUTURE);
        }

        private void expect400WithInvalidBody(TestPhone phone) throws Exception {
            PhoneUpdateRequestDto request = buildPhoneUpdateRequestDto(phone);

            mockMvc.perform(put(URL, phone1)
                            .header(HttpHeaders.AUTHORIZATION, auth(token))
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
            mockMvc.perform(delete(URL, phone1)
                            .header(HttpHeaders.AUTHORIZATION, auth(token)))
                    .andExpect(status().isNoContent());
        }

        @Test
        void whenDoesntExist_thenStatus404() throws Exception {
            mockMvc.perform(delete(URL, NON_EXISTING_ID)
                            .header(HttpHeaders.AUTHORIZATION, auth(token)))
                    .andExpect(status().isNotFound());
        }

        @Test
        void whenRequestMissingToken_thenStatus403() throws Exception {
            mockMvc.perform(delete(URL, phone1))
                    .andExpect(status().isForbidden());
        }

        @Test
        void whenRequestHasInvalidToken_thenStatus403() throws Exception {
            mockMvc.perform(delete(URL, phone1)
                            .header(HttpHeaders.AUTHORIZATION, auth("some_invalid_text")))
                    .andExpect(status().isForbidden());
        }

        @Test
        void whenIdIsNotInteger_thenStatus404() throws Exception {
            var request = delete(URL, "not_integer")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.AUTHORIZATION, auth(token));

            mockMvc.perform(request)
                    .andExpect(status().isNotFound());
        }

    }

    @Nested
    @DisplayName("GET /api/v1/phones/{id}/images")
    class GetPhoneImages {
        private final static String URL = "/api/v1/phones/{id}/images";

        @Test
        void whenPhoneHasImages_thenStatus200WithImages() throws Exception {
            var request = get(URL, phone2)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, auth(token));

            mockMvc.perform(request)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.size()").value(2))
                    .andExpect(jsonPath("$.[0].id").exists())
                    .andExpect(jsonPath("$.[0].name").exists())
                    .andExpect(jsonPath("$.[0].url").exists())
                    .andExpect(jsonPath("$.[0].size").exists())
                    .andExpect(jsonPath("$.[0].mimeType").exists());
        }

        @Test
        void whenPhoneDoesntHaveImages_thenStatus200WithEmptyArray() throws Exception {
            var request = get(URL, phone1)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, auth(token));

            mockMvc.perform(request)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.size()").value(0))
                    .andExpect(jsonPath("$.[0].id").doesNotExist());
        }

        @Test
        void whenPhoneDoesntExist_thenReturn404() throws Exception {
            var request = get(URL, NON_EXISTING_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, auth(token));

            mockMvc.perform(request)
                    .andExpect(status().isNotFound());
        }

        @Test
        void whenRequestMissingToken_thenStatus403() throws Exception {
            mockMvc.perform(get(URL, phone1))
                    .andExpect(status().isForbidden());
        }

        @Test
        void whenRequestHasInvalidToken_thenStatus403() throws Exception {
            mockMvc.perform(get(URL, phone1)
                            .header(HttpHeaders.AUTHORIZATION, auth("some_invalid_text")))
                    .andExpect(status().isForbidden());
        }

        @Test
        void whenIdIsNotInteger_thenStatus404() throws Exception {
            var request = get(URL, "not_integer")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.AUTHORIZATION, auth(token));

            mockMvc.perform(request)
                    .andExpect(status().isNotFound());
        }

    }

    @Nested
    @DisplayName("POST /api/v1/phones/{id}/add-image")
    class AddImageToPhone {
        private static final String URL = "/api/v1/phones/{id}/add-image";

        @Test
        void whenPhoneExists_thenAddImageAndReturn204() throws Exception {
            var request = multipart(URL, phone1)
                    .file((MockMultipartFile) buildMultipartFile("image"))
                    .header(HttpHeaders.AUTHORIZATION, auth(token));

            mockMvc.perform(request)
                    .andExpect(status().isNoContent());
        }

        @Test
        void whenPhoneDoesntExists_thenReturn404() throws Exception {
            var request = multipart(URL, NON_EXISTING_ID)
                    .file((MockMultipartFile) buildMultipartFile("image"))
                    .header(HttpHeaders.AUTHORIZATION, auth(token));

            mockMvc.perform(request)
                    .andExpect(status().isNotFound());
        }

        @Test
        void whenImageIsMissing_thenReturn400() throws Exception {
            var request = multipart(URL, phone1)
                    .header(HttpHeaders.AUTHORIZATION, auth(token));

            mockMvc.perform(request)
                    .andExpect(status().isBadRequest());

        }

        @Test
        void whenAddUnsupportedImage_thenReturn400() throws Exception {
            var request = multipart(URL, phone1)
                    .file((MockMultipartFile) buildUnsupportedMultipartFile("image"))
                    .header(HttpHeaders.AUTHORIZATION, auth(token));

            mockMvc.perform(request)
                    .andExpect(status().isBadRequest());
        }

        @Test
        void whenRequestMissingToken_thenStatus403() throws Exception {
            var request = multipart(URL, phone1)
                    .file((MockMultipartFile) buildUnsupportedMultipartFile("image"))
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

            mockMvc.perform(request)
                    .andExpect(status().isForbidden());
        }

        @Test
        void whenRequestHasInvalidToken_thenStatus403() throws Exception {
            var request = multipart(URL, phone1)
                    .file((MockMultipartFile) buildUnsupportedMultipartFile("image"))
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.AUTHORIZATION, auth("not_valid_token"));

            mockMvc.perform(request)
                    .andExpect(status().isForbidden());
        }

        @Test
        void whenIdIsNotInteger_thenStatus404() throws Exception {
            var request = multipart(URL, "not_integer")
                    .file((MockMultipartFile) buildUnsupportedMultipartFile("image"))
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.AUTHORIZATION, auth(token));

            mockMvc.perform(request)
                    .andExpect(status().isNotFound());
        }

    }

    @Nested
    @DisplayName("DELETE /api/v1/phones/{id}/images/{imageId}")
    class DeletePhonesImageByIdTest {
        private static final String URL = "/api/v1/phones/{id}/images/{imageId}";
        private List<Image> phone2Images;
        private List<Image> phone3Images;

        @BeforeEach
        void getImages() {
            // after phone was inserted with images we retrieve list of images
            phone3Images = phoneService.getPhoneImages(phone3);

            // get foreign images to try to delete them
            phone2Images = phoneService.getPhoneImages(phone2);
        }

        @Test
        void whenPhoneExistsAndHaveImage_thenDeleteImageAndReturn204() throws Exception {
            var request = delete(URL, phone3, phone3Images.get(0).getId())
                    .header(HttpHeaders.AUTHORIZATION, auth(token));

            mockMvc.perform(request)
                    .andExpect(status().isNoContent());
        }

        @Test
        void whenPhoneDoesntExist_thenReturn404() throws Exception {
            var request = delete(URL, NON_EXISTING_ID, phone3Images.get(0).getId())
                    .header(HttpHeaders.AUTHORIZATION, auth(token));

            mockMvc.perform(request)
                    .andExpect(status().isNotFound());
        }

        @Test
        void whenImageDoesntExist_thenReturn404() throws Exception {
            var request = delete(URL, phone3, NON_EXISTING_IMAGE_ID)
                    .header(HttpHeaders.AUTHORIZATION, auth(token));

            mockMvc.perform(request)
                    .andExpect(status().isNotFound());
        }

        @Test
        void whenTryToDeleteForeignImage_thenReturn400() throws Exception {
            // use phone3, but delete image from phone2
            var request = delete(URL, phone3, phone2Images.get(0).getId())
                    .header(HttpHeaders.AUTHORIZATION, auth(token));

            mockMvc.perform(request)
                    .andExpect(status().isNotFound());
        }

        @Test
        void whenRequestMissingToken_thenStatus403() throws Exception {
            mockMvc.perform(delete(URL, phone3, phone3Images.get(0).getId()))
                    .andExpect(status().isForbidden());
        }

        @Test
        void whenRequestHasInvalidToken_thenStatus403() throws Exception {
            mockMvc.perform(delete(URL, phone3, phone3Images.get(0).getId())
                            .header(HttpHeaders.AUTHORIZATION, auth("some_invalid_text")))
                    .andExpect(status().isForbidden());
        }

        @Test
        void whenIdIsNotInteger_thenStatus404() throws Exception {
            var request = delete(URL, "not_integer", phone3Images.get(0).getId())
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.AUTHORIZATION, auth(token));

            mockMvc.perform(request)
                    .andExpect(status().isNotFound());
        }

        @Test
        void whenImageIdIsNotInteger_thenStatus404() throws Exception {
            var request = delete(URL, phone3, "not_integer")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.AUTHORIZATION, auth(token));

            mockMvc.perform(request)
                    .andExpect(status().isNotFound());
        }

    }

    static class TestResources {
        static final long NON_EXISTING_ID = 99_999L;
        static final long NON_EXISTING_IMAGE_ID = 99_999L;

        static final String FILE_ORIGINAL_NAME = "file.jpeg";
        static final String FILE_CONTENT_TYPE = "image/jpeg";
        static final byte[] FILE_CONTENT = FILE_ORIGINAL_NAME.getBytes();

        static final String UNSUPPORTED_FILE_CONTENT_TYPE = "image/gif";


        static String auth(String token) {
            return "Bearer " + token;
        }

        static PhoneCreateRequestDto buildPhoneCreateRequestDto(TestPhone testPhone) {
            return new PhoneCreateRequestDto(
                    testPhone.name,
                    testPhone.description,
                    testPhone.price,
                    testPhone.brand,
                    testPhone.releaseYear,
                    testPhone.cpu,
                    testPhone.coresNumber,
                    testPhone.screenSize,
                    testPhone.frontCamera,
                    testPhone.mainCamera,
                    testPhone.batteryCapacity
            );
        }


        static PhoneUpdateRequestDto buildPhoneUpdateRequestDto(TestPhone testPhone) {
            return new PhoneUpdateRequestDto(
                    testPhone.name,
                    testPhone.description,
                    testPhone.price,
                    testPhone.brand,
                    testPhone.releaseYear,
                    testPhone.cpu,
                    testPhone.coresNumber,
                    testPhone.screenSize,
                    testPhone.frontCamera,
                    testPhone.mainCamera,
                    testPhone.batteryCapacity
            );
        }


        static MultipartFile buildMultipartFile(String name) {
            return new MockMultipartFile(
                    name,
                    FILE_ORIGINAL_NAME,
                    FILE_CONTENT_TYPE,
                    FILE_CONTENT
            );
        }

        static MultipartFile buildUnsupportedMultipartFile(String name) {
            return new MockMultipartFile(
                    name,
                    FILE_ORIGINAL_NAME,
                    UNSUPPORTED_FILE_CONTENT_TYPE,
                    FILE_CONTENT
            );
        }

        static MultipartFile buildJsonLikeMultipartFile(byte[] content, String name) {
            return new MockMultipartFile(
                    name,
                    "doesnt_matter",
                    "application/json",
                    content
            );
        }

    }

    enum TestPhone {
        // Valid phones
        PHONE_1(
                "iPhone 15", "Latest Apple smartphone", new BigDecimal("999.99"), "Apple", 2024,
                "Apple A16 Bionic", 6, "6.1\"", "12 MP", "48 MP", "3349 mAh"),

        PHONE_2(
                "Samsung Galaxy S24", "Flagship Samsung phone", new BigDecimal("899.99"), "Samsung", 2024,
                "Exynos 2400", 10, "6.2\"", "12 MP", "50 MP", "4000 mAh"),

        PHONE_3(
                "Google Pixel 8", "Pure Android experience", new BigDecimal("699.99"), "Google", 2023,
                "Google Tensor G3", 8, "6.2\"", "10 MP", "50 MP", "4575 mAh"),

        VALID_PHONE(
                "Valid Phone", "Valid description", new BigDecimal("500.00"), "ValidBrand", 2020,
                "Snapdragon 8 Gen 2", 8, "6.5\"", "12 MP", "50-12 MP", "4500 mAh"),

        VALID_PHONE_NULL_DESCRIPTION(
                "Valid Phone", null, new BigDecimal("500.00"), "ValidBrand", 2020,
                "Snapdragon 8 Gen 2", 8, "6.5\"", "12 MP", "50-12 MP", "4500 mAh"),

        VALID_PHONE_BOUNDARY_MIN(
                "Abc", "", new BigDecimal("0.00"), "Abc", 1970,
                "A", 1, "5\"", "1 MP", "1 MP", "1000 mAh"),

        VALID_PHONE_BOUNDARY_MAX(
                "N".repeat(255), "D".repeat(1000), new BigDecimal("99999.99"), "B".repeat(255), 2026,
                "C".repeat(50), 32, "7.9\"", "50 MP", "200-50-12 MP", "9000 mAh"),


        // Invalid name
        INVALID_NAME_NULL(
                null, "description", new BigDecimal("100.00"), "Brand", 2020,
                "Snapdragon 8 Gen 3", 8, "6.7\"", "12 MP", "50-12 MP", "5000 mAh"),

        INVALID_NAME_BLANK(
                "", "description", new BigDecimal("100.00"), "Brand", 2020,
                "Snapdragon 8 Gen 3", 8, "6.7\"", "12 MP", "50-12 MP", "5000 mAh"),

        INVALID_NAME_TOO_SHORT(
                "Ab", "description", new BigDecimal("100.00"), "Brand", 2020,
                "Snapdragon 8 Gen 3", 8, "6.7\"", "12 MP", "50-12 MP", "5000 mAh"),

        INVALID_NAME_TOO_LONG(
                "N".repeat(256), "description", new BigDecimal("100.00"), "Brand", 2020,
                "Snapdragon 8 Gen 3", 8, "6.7\"", "12 MP", "50-12 MP", "5000 mAh"),


        // Invalid description
        INVALID_DESCRIPTION_TOO_LONG(
                "Phone", "D".repeat(1001), new BigDecimal("100.00"), "Brand", 2020,
                "Snapdragon 8 Gen 3", 8, "6.7\"", "12 MP", "50-12 MP", "5000 mAh"),


        // Invalid price
        INVALID_PRICE_NULL(
                "Phone", "description", null, "Brand", 2020,
                "Snapdragon 8 Gen 3", 8, "6.7\"", "12 MP", "50-12 MP", "5000 mAh"),

        INVALID_PRICE_NEGATIVE(
                "Phone", "description", new BigDecimal("-0.01"), "Brand", 2020,
                "Snapdragon 8 Gen 3", 8, "6.7\"", "12 MP", "50-12 MP", "5000 mAh"),


        // Invalid brand
        INVALID_BRAND_NULL(
                "Phone", "description", new BigDecimal("100.00"), null, 2020,
                "Snapdragon 8 Gen 3", 8, "6.7\"", "12 MP", "50-12 MP", "5000 mAh"),

        INVALID_BRAND_BLANK(
                "Phone", "description", new BigDecimal("100.00"), "", 2020,
                "Snapdragon 8 Gen 3", 8, "6.7\"", "12 MP", "50-12 MP", "5000 mAh"),

        INVALID_BRAND_TOO_SHORT(
                "Phone", "description", new BigDecimal("100.00"), "Ab", 2020,
                "Snapdragon 8 Gen 3", 8, "6.7\"", "12 MP", "50-12 MP", "5000 mAh"),

        INVALID_BRAND_TOO_LONG(
                "Phone", "description", new BigDecimal("100.00"), "B".repeat(256), 2020,
                "Snapdragon 8 Gen 3", 8, "6.7\"", "12 MP", "50-12 MP", "5000 mAh"),


        // Invalid releaseYear
        INVALID_YEAR_NULL(
                "Phone", "description", new BigDecimal("100.00"), "Brand", null,
                "Snapdragon 8 Gen 3", 8, "6.7\"", "12 MP", "50-12 MP", "5000 mAh"),

        INVALID_YEAR_TOO_EARLY(
                "Phone", "description", new BigDecimal("100.00"), "Brand", 1969,
                "Snapdragon 8 Gen 3", 8, "6.7\"", "12 MP", "50-12 MP", "5000 mAh"),

        INVALID_YEAR_FUTURE(
                "Phone", "description", new BigDecimal("100.00"), "Brand", 2027,
                "Snapdragon 8 Gen 3", 8, "6.7\"", "12 MP", "50-12 MP", "5000 mAh"),


        // Invalid cpu
        INVALID_CPU_NULL(
        "Phone", "description", new BigDecimal("100.00"), "Brand", 2020,
                null, 8, "6.7\"", "12 MP", "50-12 MP", "5000 mAh"),

        INVALID_CPU_BLANK(
        "Phone", "description", new BigDecimal("100.00"), "Brand", 2020,
                "", 8, "6.7\"", "12 MP", "50-12 MP", "5000 mAh"),

        INVALID_CPU_TOO_LONG(
        "Phone", "description", new BigDecimal("100.00"), "Brand", 2020,
                "C".repeat(101), 8, "6.7\"", "12 MP", "50-12 MP", "5000 mAh"),


        // Invalid coresNumber
        INVALID_CORES_NULL(
                "Phone", "description", new BigDecimal("100.00"), "Brand", 2020,
                "Snapdragon 8 Gen 3", null, "6.7\"", "12 MP", "50-12 MP", "5000 mAh"),

        INVALID_CORES_TOO_SMALL(
                "Phone", "description", new BigDecimal("100.00"), "Brand", 2020,
                "Snapdragon 8 Gen 3", 0, "6.7\"", "12 MP", "50-12 MP", "5000 mAh"),

        INVALID_CORES_TOO_BIG(
                "Phone", "description", new BigDecimal("100.00"), "Brand", 2020,
                "Snapdragon 8 Gen 3", 33, "6.7\"", "12 MP", "50-12 MP", "5000 mAh"),


        // Invalid screenSize
        INVALID_SCREEN_NULL(
                "Phone", "description", new BigDecimal("100.00"), "Brand", 2020,
                "Snapdragon 8 Gen 3", 8, null, "12 MP", "50-12 MP", "5000 mAh"),

        INVALID_SCREEN_BLANK(
                "Phone", "description", new BigDecimal("100.00"), "Brand", 2020,
                "Snapdragon 8 Gen 3", 8, "", "12 MP", "50-12 MP", "5000 mAh"),

        INVALID_SCREEN_NO_QUOTES(
                "Phone", "description", new BigDecimal("100.00"), "Brand", 2020,
                "Snapdragon 8 Gen 3", 8, "6.7", "12 MP", "50-12 MP", "5000 mAh"),

        INVALID_SCREEN_WITH_TEXT(
                "Phone", "description", new BigDecimal("100.00"), "Brand", 2020,
                "Snapdragon 8 Gen 3", 8, "6.7 inch", "12 MP", "50-12 MP", "5000 mAh"),


        // Invalid frontCamera
        INVALID_FRONT_CAMERA_NULL(
                "Phone", "description", new BigDecimal("100.00"), "Brand", 2020,
                "Snapdragon 8 Gen 3", 8, "6.7\"", null, "50-12 MP", "5000 mAh"),

        INVALID_FRONT_CAMERA_BLANK(
                "Phone", "description", new BigDecimal("100.00"), "Brand", 2020,
                "Snapdragon 8 Gen 3", 8, "6.7\"", "", "50-12 MP", "5000 mAh"),

        INVALID_FRONT_CAMERA_DECIMAL(
                "Phone", "description", new BigDecimal("100.00"), "Brand", 2020,
                "Snapdragon 8 Gen 3", 8, "6.7\"", "10.5 MP", "50-12 MP", "5000 mAh"),

        INVALID_FRONT_CAMERA_NO_UNIT(
                "Phone", "description", new BigDecimal("100.00"), "Brand", 2020,
                "Snapdragon 8 Gen 3", 8, "6.7\"", "12", "50-12 MP", "5000 mAh"),


        // Invalid mainCamera
        INVALID_MAIN_CAMERA_NULL(
                "Phone", "description", new BigDecimal("100.00"), "Brand", 2020,
                "Snapdragon 8 Gen 3", 8, "6.7\"", "12 MP", null, "5000 mAh"),

        INVALID_MAIN_CAMERA_BLANK(
                "Phone", "description", new BigDecimal("100.00"), "Brand", 2020,
                "Snapdragon 8 Gen 3", 8, "6.7\"", "12 MP", "", "5000 mAh"),

        INVALID_MAIN_CAMERA_DECIMAL(
                "Phone", "description", new BigDecimal("100.00"), "Brand", 2020,
                "Snapdragon 8 Gen 3", 8, "6.7\"", "10.5 MP", "50-12 MP", "5000 mAh"),

        INVALID_MAIN_CAMERA_NO_UNIT(
                "Phone", "description", new BigDecimal("100.00"), "Brand", 2020,
                "Snapdragon 8 Gen 3", 8, "6.7\"", "12", "50-12 MP", "5000 mAh"),


        INVALID_MAIN_CAMERA_WRONG_FORMAT(
                "Phone", "description", new BigDecimal("100.00"), "Brand", 2020,
                "Snapdragon 8 Gen 3", 8, "6.7\"", "12 MP", "50,12 MP", "5000 mAh"),


        // Invalid batteryCapacity
        INVALID_BATTERY_NULL(
                "Phone", "description", new BigDecimal("100.00"), "Brand", 2020,
                "Snapdragon 8 Gen 3", 8, "6.7\"", "12 MP", "50-12 MP", null),

        INVALID_BATTERY_BLANK(
                "Phone", "description", new BigDecimal("100.00"), "Brand", 2020,
                "Snapdragon 8 Gen 3", 8, "6.7\"", "12 MP", "50-12 MP", ""),

        INVALID_BATTERY_NO_UNIT(
                "Phone", "description", new BigDecimal("100.00"), "Brand", 2020,
                "Snapdragon 8 Gen 3", 8, "6.7\"", "12 MP", "50-12 MP", "5000"),

        INVALID_BATTERY_WRONG_UNIT(
                "Phone", "description", new BigDecimal("100.00"), "Brand", 2020,
                "Snapdragon 8 Gen 3", 8, "6.7\"", "12 MP", "50-12 MP", "5000 mah");



        private final String name;
        private final String description;
        private final BigDecimal price;
        private final String brand;
        private final Integer releaseYear;
        private final String cpu;
        private final Integer coresNumber;
        private final String screenSize;
        private final String frontCamera;
        private final String mainCamera;
        private final String batteryCapacity;


        TestPhone(
                String name,
                String description,
                BigDecimal price,
                String brand,
                Integer releaseYear,
                String cpu,
                Integer coresNumber,
                String screenSize,
                String frontCamera,
                String mainCamera,
                String batteryCapacity
        ) {
            this.name = name;
            this.description = description;
            this.price = price;
            this.brand = brand;
            this.releaseYear = releaseYear;
            this.cpu = cpu;
            this.coresNumber = coresNumber;
            this.screenSize = screenSize;
            this.frontCamera = frontCamera;
            this.mainCamera = mainCamera;
            this.batteryCapacity = batteryCapacity;
        }

    }

}
