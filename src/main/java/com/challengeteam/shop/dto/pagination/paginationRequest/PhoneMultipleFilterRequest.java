package com.challengeteam.shop.dto.pagination.paginationRequest;

import com.challengeteam.shop.constraints.filter.FilterRequestConstraints;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.util.List;

/**
 * A record representing a request for filtering and sorting phones by multiple criteria.
 * <p>
 * This request object is used to filter phone listings based on various parameters including
 * brands, price range, stock availability, and pre-order status. It also supports sorting
 * the results by different fields and orders.
 * </p>
 * <p>
 * The compact constructor provides default values for the sort parameter, ensuring that
 * if no sort order is specified, results are sorted by name in ascending order.
 * </p>
 *
 * @param brands   a list of brand names to filter by; if null or empty, no brand filtering is applied
 * @param minPrice the minimum price threshold for filtering phones; must not be negative
 * @param maxPrice the maximum price threshold for filtering phones; must not be negative
 * @param inStock  a flag indicating whether to filter for phones that are in stock (quantity >= 1);
 *                 if null, no stock filtering is applied
 * @param preOrder a flag indicating whether to filter for phones available for pre-order;
 *                 if null, no pre-order filtering is applied
 * @param sort     the sort order for the results; must be one of: name_asc, name_desc, price_asc, price_desc;
 *                 defaults to name_asc if null or blank
 * @see com.challengeteam.shop.constraints.filter.FilterRequestConstraints
 */
public record PhoneMultipleFilterRequest(
        List<String> brands,

        @DecimalMin(value = FilterRequestConstraints.MIN_PHONE_PRICE, message = "Minimum price cannot be negative")
        BigDecimal minPrice,

        @DecimalMin(value = FilterRequestConstraints.MIN_PHONE_PRICE, message = "Maximum price cannot be negative")
        BigDecimal maxPrice,

        Boolean inStock,

        Boolean preOrder,

        @Pattern(
                regexp = FilterRequestConstraints.SORT_ORDER_REGEXP,
                message = "Sort must be one of: name_asc, name_desc, price_asc, price_desc"
        )
        String sort
) {
    /**
     * Compact constructor that provides default values for fields.
     * <p>
     * If the sort parameter is null or blank, it defaults to {@link FilterRequestConstraints#SORT_ORDER_BY_NAME_ASC}.
     * </p>
     */
    public PhoneMultipleFilterRequest {
        sort = (sort == null || sort.isBlank()) ? FilterRequestConstraints.SORT_ORDER_BY_NAME_ASC : sort;
    }
}