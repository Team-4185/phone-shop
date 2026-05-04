package com.challengeteam.shop.web.controller;

import com.challengeteam.shop.entity.phone.Phone;
import com.challengeteam.shop.entity.phone.PhoneCharacteristics;
import com.challengeteam.shop.entity.phone.ProductStatus;
import com.challengeteam.shop.persistence.repository.PhoneRepository;
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
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(ContainerExtension.class)
class PhoneFilteringControllerTest {

    @Autowired
    private PhoneRepository phoneRepository;

    @Autowired
    private TestAuthHelper testAuthHelper;
    private String token;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @DynamicPropertySource
    static void loadProperties(DynamicPropertyRegistry registry) {
        TestContextConfigurator.initRequiredProperties(registry);
    }

    @BeforeEach
    void setUp() {
        phoneRepository.deleteAll();

        phoneRepository.saveAll(List.of(
                // Apple
                buildPhone("iPhone 15 Pro", "Apple", new BigDecimal("999"), 10, ProductStatus.IN_STOCK),
                buildPhone("iPhone 14", "Apple", new BigDecimal("799"), 0, ProductStatus.OUT_OF_STOCK),
                // Samsung
                buildPhone("Galaxy S24", "Samsung", new BigDecimal("899"), 5, ProductStatus.IN_STOCK),
                buildPhone("Galaxy S23", "Samsung", new BigDecimal("699"), 0, ProductStatus.OUT_OF_STOCK),
                // Google
                buildPhone("Pixel 8", "Google", new BigDecimal("599"), 3, ProductStatus.LOW_STOCK)
        ));

        token = testAuthHelper.authorizeLikeTestUser();
    }

    // ─────────────────────────────────────────────────────────────────────────

    /**
     * cases:
     * - no filters → all 5 phones returned
     * - filter by single brand → only that brand returned
     * - filter by multiple brands → union of both brands returned
     * - filter by unknown brand → empty result
     */
    @Nested
    @DisplayName("Brand filter")
    class BrandFilterTest {

        @Test
        @DisplayName("No filters → all phones returned")
        void shouldReturnAllPhones_whenNoFilters() throws Exception {
            mockMvc.perform(get("/api/v1/filter/by")
                            .header("Authorization", "Bearer " + token)
                            .param("page", "1").param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(5));
        }

        @Test
        @DisplayName("Single brand filter → only that brand")
        void shouldReturnOnlyMatchingBrand_whenSingleBrandGiven() throws Exception {
            mockMvc.perform(get("/api/v1/filter/by")
                            .header("Authorization", "Bearer " + token)
                            .param("brands", "Apple")
                            .param("page", "1").param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(2))
                    .andExpect(jsonPath("$.content[*].brand", everyItem(equalTo("Apple"))));
        }

        @Test
        @DisplayName("Multiple brands filter → union of both")
        void shouldReturnPhones_whenMultipleBrandsGiven() throws Exception {
            mockMvc.perform(get("/api/v1/filter/by")
                            .header("Authorization", "Bearer " + token)
                            .param("brands", "Apple", "Samsung")
                            .param("page", "1").param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(4))
                    .andExpect(jsonPath("$.content[?(@.brand == 'Apple')]").exists())
                    .andExpect(jsonPath("$.content[?(@.brand == 'Samsung')]").exists())
                    .andExpect(jsonPath("$.content[?(@.brand == 'Google')]").doesNotExist());
        }

        @Test
        @DisplayName("Unknown brand → empty result")
        void shouldReturnEmpty_whenUnknownBrandGiven() throws Exception {
            mockMvc.perform(get("/api/v1/filter/by")
                            .header("Authorization", "Bearer " + token)
                            .param("brands", "Nokia")
                            .param("page", "1").param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(0))
                    .andExpect(jsonPath("$.content").isEmpty());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────

    /**
     * cases:
     * - only minPrice → phones with price >= minPrice
     * - only maxPrice → phones with price <= maxPrice
     * - minPrice + maxPrice → phones strictly within range
     * - minPrice equals exact phone price → that phone included
     * - maxPrice equals exact phone price → that phone included
     * - range that excludes all → empty result
     */
    @Nested
    @DisplayName("Price filter")
    class PriceFilterTest {

        @Test
        @DisplayName("Only minPrice → phones above or equal threshold")
        void shouldReturnPhones_whenOnlyMinPriceGiven() throws Exception {
            mockMvc.perform(get("/api/v1/filter/by")
                            .header("Authorization", "Bearer " + token)
                            .param("minPrice", "800")
                            .param("page", "1").param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(2)); // 899, 999
        }

        @Test
        @DisplayName("Only maxPrice → phones below or equal threshold")
        void shouldReturnPhones_whenOnlyMaxPriceGiven() throws Exception {
            mockMvc.perform(get("/api/v1/filter/by")
                            .header("Authorization", "Bearer " + token)
                            .param("maxPrice", "700")
                            .param("page", "1").param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(2)); // 599, 699
        }

        @Test
        @DisplayName("minPrice + maxPrice → phones strictly within range")
        void shouldReturnPhones_whenBothPricesGiven() throws Exception {
            mockMvc.perform(get("/api/v1/filter/by")
                            .header("Authorization", "Bearer " + token)
                            .param("minPrice", "700")
                            .param("maxPrice", "900")
                            .param("page", "1").param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(2)); // 799, 899
        }

        @Test
        @DisplayName("minPrice equals exact price → phone included (boundary)")
        void shouldIncludePhone_whenMinPriceEqualsExactPrice() throws Exception {
            mockMvc.perform(get("/api/v1/filter/by")
                            .header("Authorization", "Bearer " + token)
                            .param("minPrice", "999")
                            .param("page", "1").param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(1))
                    .andExpect(jsonPath("$.content[0].name").value("iPhone 15 Pro"));
        }

        @Test
        @DisplayName("maxPrice equals exact price → phone included (boundary)")
        void shouldIncludePhone_whenMaxPriceEqualsExactPrice() throws Exception {
            mockMvc.perform(get("/api/v1/filter/by")
                            .header("Authorization", "Bearer " + token)
                            .param("maxPrice", "599")
                            .param("page", "1").param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(1))
                    .andExpect(jsonPath("$.content[0].name").value("Pixel 8"));
        }

        @Test
        @DisplayName("Range excludes all phones → empty result")
        void shouldReturnEmpty_whenPriceRangeExcludesAll() throws Exception {
            mockMvc.perform(get("/api/v1/filter/by")
                            .header("Authorization", "Bearer " + token)
                            .param("minPrice", "1500")
                            .param("maxPrice", "2000")
                            .param("page", "1").param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(0));
        }

        @Test
        @DisplayName("Min price greater max price -> exception")
        void shouldThrowException_whenMinPriceGreaterMaxPrice() throws Exception {
            mockMvc.perform(get("/api/v1/filter/by")
                            .header("Authorization", "Bearer " + token)
                            .param("minPrice", "200")
                            .param("maxPrice", "100")
                            .param("page", "1").param("size", "10"))
                    .andExpect(status().isBadRequest());
        }


    }

    // ─────────────────────────────────────────────────────────────────────────

    /**
     * cases:
     * - inStock=true → only phones with stock > 0
     * - inStock=false → filter not applied, all phones returned
     * - preOrder=true → only phones with stock = 0
     * - preOrder=false → filter not applied, all phones returned
     * - inStock=true + preOrder=true → impossible combination, 0 results
     */
    @Nested
    @DisplayName("Stock / preOrder filter")
    class StockFilterTest {

        @Test
        @DisplayName("inStock=true → only phones with stock > 0")
        void shouldReturnInStockPhones_whenInStockTrue() throws Exception {
            mockMvc.perform(get("/api/v1/filter/by")
                            .header("Authorization", "Bearer " + token)
                            .param("inStock", "true")
                            .param("page", "1").param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(3)); // stock > 0: 10, 5, 3
        }

        @Test
        @DisplayName("inStock=false → no filter applied, all phones returned")
        void shouldReturnAllPhones_whenInStockFalse() throws Exception {
            mockMvc.perform(get("/api/v1/filter/by")
                            .header("Authorization", "Bearer " + token)
                            .param("inStock", "false")
                            .param("page", "1").param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(5));
        }

        @Test
        @DisplayName("preOrder=true → only phones with stock = 0")
        void shouldReturnPreOrderPhones_whenPreOrderTrue() throws Exception {
            mockMvc.perform(get("/api/v1/filter/by")
                            .header("Authorization", "Bearer " + token)
                            .param("preOrder", "true")
                            .param("page", "1").param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(2)); // stock = 0: iPhone 14, Galaxy S23
        }

        @Test
        @DisplayName("preOrder=false → no filter applied, all phones returned")
        void shouldReturnAllPhones_whenPreOrderFalse() throws Exception {
            mockMvc.perform(get("/api/v1/filter/by")
                            .header("Authorization", "Bearer " + token)
                            .param("preOrder", "false")
                            .param("page", "1").param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(5));
        }

        @Test
        @DisplayName("inStock=true + preOrder=true → impossible condition, 0 results")
        void shouldReturnEmpty_whenBothInStockAndPreOrderTrue() throws Exception {
            mockMvc.perform(get("/api/v1/filter/by")
                            .header("Authorization", "Bearer " + token)
                            .param("inStock", "true")
                            .param("preOrder", "true")
                            .param("page", "1").param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(0));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────

    /**
     * cases:
     * - sort=price_asc → first element cheapest, last most expensive
     * - sort=price_desc → first element most expensive
     * - sort=name_asc → alphabetical order A→Z
     * - sort=name_desc → alphabetical order Z→A
     * - no sort param → default name_asc applied
     */
    @Nested
    @DisplayName("Sorting")
    class SortingTest {

        @Test
        @DisplayName("price_asc → cheapest first")
        void shouldSortByPriceAscending() throws Exception {
            mockMvc.perform(get("/api/v1/filter/by")
                            .header("Authorization", "Bearer " + token)
                            .param("sort", "price_asc")
                            .param("page", "1").param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].price").value(599))
                    .andExpect(jsonPath("$.content[4].price").value(999));
        }

        @Test
        @DisplayName("price_desc → most expensive first")
        void shouldSortByPriceDescending() throws Exception {
            mockMvc.perform(get("/api/v1/filter/by")
                            .header("Authorization", "Bearer " + token)
                            .param("sort", "price_desc")
                            .param("page", "1").param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].price").value(999))
                    .andExpect(jsonPath("$.content[4].price").value(599));
        }

        @Test
        @DisplayName("name_asc → alphabetical A to Z")
        void shouldSortByNameAscending() throws Exception {
            mockMvc.perform(get("/api/v1/filter/by")
                            .header("Authorization", "Bearer " + token)
                            .param("sort", "name_asc")
                            .param("page", "1").param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].name").value("Galaxy S23"))
                    .andExpect(jsonPath("$.content[4].name").value("Pixel 8"));
        }

        @Test
        @DisplayName("name_desc → alphabetical Z to A")
        void shouldSortByNameDescending() throws Exception {
            mockMvc.perform(get("/api/v1/filter/by")
                            .header("Authorization", "Bearer " + token)
                            .param("sort", "name_desc")
                            .param("page", "1").param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].name").value("Pixel 8"))
                    .andExpect(jsonPath("$.content[4].name").value("Galaxy S23"));
        }

        @Test
        @DisplayName("No sort param → default name_asc")
        void shouldApplyDefaultSort_whenSortNotProvided() throws Exception {
            mockMvc.perform(get("/api/v1/filter/by")
                            .header("Authorization", "Bearer " + token)
                            .param("page", "1").param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].name").value("Galaxy S23"));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────

    /**
     * cases:
     * - correct page + size → right slice returned
     * - last page has remaining elements only
     * - first page flag = true on page 1
     * - last page flag = true on last page
     * - totalPages matches expected
     */
    @Nested
    @DisplayName("Pagination")
    class PaginationTest {

        @Test
        @DisplayName("Page 1 size 2 → first 2 elements, isFirst=true")
        void shouldReturnFirstPage() throws Exception {
            mockMvc.perform(get("/api/v1/filter/by")
                            .header("Authorization", "Bearer " + token)
                            .param("page", "1").param("size", "2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.page").value(1))
                    .andExpect(jsonPath("$.size").value(2))
                    .andExpect(jsonPath("$.content.length()").value(2))
                    .andExpect(jsonPath("$.first").value(true))
                    .andExpect(jsonPath("$.last").value(false));
        }

        @Test
        @DisplayName("Page 3 size 2 → 1 remaining element, isLast=true")
        void shouldReturnLastPage_withRemainingElements() throws Exception {
            mockMvc.perform(get("/api/v1/filter/by")
                            .header("Authorization", "Bearer " + token)
                            .param("page", "3").param("size", "2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.page").value(3))
                    .andExpect(jsonPath("$.content.length()").value(1))
                    .andExpect(jsonPath("$.last").value(true));
        }

        @Test
        @DisplayName("totalPages = ceil(5/2) = 3")
        void shouldReturnCorrectTotalPages() throws Exception {
            mockMvc.perform(get("/api/v1/filter/by")
                            .header("Authorization", "Bearer " + token)
                            .param("page", "1").param("size", "2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalPages").value(3))
                    .andExpect(jsonPath("$.totalElements").value(5));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────

    /**
     * cases:
     * - combined brand + price filter
     * - combined brand + inStock filter
     * - combined price + inStock + sort
     */
    @Nested
    @DisplayName("Combined filters")
    class CombinedFilterTest {

        @Test
        @DisplayName("Brand Apple + maxPrice 900 → only iPhone 14 (799)")
        void shouldFilterByBrandAndMaxPrice() throws Exception {
            mockMvc.perform(get("/api/v1/filter/by")
                            .header("Authorization", "Bearer " + token)
                            .param("brands", "Apple")
                            .param("maxPrice", "900")
                            .param("page", "1").param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(1))
                    .andExpect(jsonPath("$.content[0].name").value("iPhone 14"));
        }

        @Test
        @DisplayName("Brand Samsung + inStock=true → only Galaxy S24 (stock=5)")
        void shouldFilterByBrandAndInStock() throws Exception {
            mockMvc.perform(get("/api/v1/filter/by")
                            .header("Authorization", "Bearer " + token)
                            .param("brands", "Samsung")
                            .param("inStock", "true")
                            .param("page", "1").param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(1))
                    .andExpect(jsonPath("$.content[0].name").value("Galaxy S24"));
        }

        @Test
        @DisplayName("minPrice 600 + inStock=true + sort price_desc → S24(899), iPhone15(999)... desc")
        void shouldFilterByPriceAndStockWithSort() throws Exception {
            mockMvc.perform(get("/api/v1/filter/by")
                            .header("Authorization", "Bearer " + token)
                            .param("minPrice", "600")
                            .param("inStock", "true")
                            .param("sort", "price_desc")
                            .param("page", "1").param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(2)) // iPhone15 Pro(999,stock=10), Galaxy S24(899,stock=5)
                    .andExpect(jsonPath("$.content[0].price").value(999))
                    .andExpect(jsonPath("$.content[1].price").value(899));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────

    /**
     * cases:
     * - minPrice negative → 400
     * - maxPrice negative → 400
     * - invalid sort value → 400
     * - page = 0 → 400
     * - size = 0 → 400
     */
    @Nested
    @DisplayName("Validation")
    class ValidationTest {

        @Test
        @DisplayName("Negative minPrice → 400")
        void shouldReturn400_whenMinPriceNegative() throws Exception {
            mockMvc.perform(get("/api/v1/filter/by")
                            .header("Authorization", "Bearer " + token)
                            .param("minPrice", "-1"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Negative maxPrice → 400")
        void shouldReturn400_whenMaxPriceNegative() throws Exception {
            mockMvc.perform(get("/api/v1/filter/by")
                            .header("Authorization", "Bearer " + token)
                            .param("maxPrice", "-50"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Invalid sort value → 400")
        void shouldReturn400_whenSortInvalid() throws Exception {
            mockMvc.perform(get("/api/v1/filter/by")
                            .header("Authorization", "Bearer " + token)
                            .param("sort", "invalid_sort"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("page=0 → 400")
        void shouldReturn400_whenPageIsZero() throws Exception {
            mockMvc.perform(get("/api/v1/filter/by")
                            .header("Authorization", "Bearer " + token)
                            .param("page", "0"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("size=0 → 400")
        void shouldReturn400_whenSizeIsZero() throws Exception {
            mockMvc.perform(get("/api/v1/filter/by")
                            .header("Authorization", "Bearer " + token)
                            .param("size", "0"))
                    .andExpect(status().isBadRequest());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────

    private Phone buildPhone(String name, String brand, BigDecimal price,
                             int stock, ProductStatus status) {
        return Phone.builder()
                .name(name)
                .brand(brand)
                .price(price)
                .stock(stock)
                .sku(UUID.randomUUID().toString())
                .releaseYear(2024)
                .status(status)
                .phoneCharacteristics(PhoneCharacteristics.builder()
                        .cpu("Snapdragon 8 Gen 3")
                        .coresNumber(8)
                        .batteryCapacity("5000")
                        .screenSize("6.7")
                        .mainCamera("50MP")
                        .frontCamera("12MP")
                        .build())
                .build();
    }

    @Nested
    @DisplayName("anonymous user - > valid response")
    class AnonymousUserTest {
        @Test
        void shouldReturnValidResponse_whenAnonymousUser() throws Exception {
            mockMvc.perform(get("/api/v1/filter/by")
                            .param("brands", "Samsung")
                            .param("inStock", "true")
                            .param("page", "1").param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(1))
                    .andExpect(jsonPath("$.content[0].name").value("Galaxy S24"));
        }
    }
}
