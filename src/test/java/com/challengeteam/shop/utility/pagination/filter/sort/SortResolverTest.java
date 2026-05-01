package com.challengeteam.shop.utility.pagination.filter.sort;

import com.challengeteam.shop.entity.phone.Phone_;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;

class SortResolverTest {

    @Test
    @DisplayName("name_asc → ASC by name")
    void shouldReturnAscByName_whenNameAsc() {
        Sort sort = SortResolver.resolve("name_asc");

        assertThat(sort.getOrderFor(Phone_.NAME))
                .isNotNull()
                .extracting(Sort.Order::getDirection)
                .isEqualTo(Sort.Direction.ASC);
    }

    @Test
    @DisplayName("name_desc → DESC by name")
    void shouldReturnDescByName_whenNameDesc() {
        Sort sort = SortResolver.resolve("name_desc");

        assertThat(sort.getOrderFor(Phone_.NAME))
                .isNotNull()
                .extracting(Sort.Order::getDirection)
                .isEqualTo(Sort.Direction.DESC);
    }

    @Test
    @DisplayName("price_asc → ASC by price")
    void shouldReturnAscByPrice_whenPriceAsc() {
        Sort sort = SortResolver.resolve("price_asc");

        assertThat(sort.getOrderFor(Phone_.PRICE))
                .isNotNull()
                .extracting(Sort.Order::getDirection)
                .isEqualTo(Sort.Direction.ASC);
    }

    @Test
    @DisplayName("price_desc → DESC by price")
    void shouldReturnDescByPrice_whenPriceDesc() {
        Sort sort = SortResolver.resolve("price_desc");

        assertThat(sort.getOrderFor(Phone_.PRICE))
                .isNotNull()
                .extracting(Sort.Order::getDirection)
                .isEqualTo(Sort.Direction.DESC);
    }

    @Test
    @DisplayName("unknown value → default ASC by name")
    void shouldReturnDefaultAscByName_whenUnknownValue() {
        Sort sort = SortResolver.resolve("unknown_value");

        assertThat(sort.getOrderFor(Phone_.NAME))
                .isNotNull()
                .extracting(Sort.Order::getDirection)
                .isEqualTo(Sort.Direction.ASC);
    }

    @Test
    @DisplayName("case insensitive → PRICE_ASC treated same as price_asc")
    void shouldBeCaseInsensitive() {
        Sort sort = SortResolver.resolve("PRICE_ASC");

        assertThat(sort.getOrderFor(Phone_.PRICE))
                .isNotNull()
                .extracting(Sort.Order::getDirection)
                .isEqualTo(Sort.Direction.ASC);
    }
}