package com.challengeteam.shop.service.pagination;

import com.challengeteam.shop.dto.pagination.paginationRequest.PhoneMultipleFilterRequest;
import com.challengeteam.shop.dto.pagination.paginationResponse.PageResponseDto;
import com.challengeteam.shop.dto.phone.PhoneResponseDto;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for filtering and sorting phone entities with pagination support.
 * <p>
 * This service provides functionality to filter phones based on multiple criteria such as
 * brand, price range, and stock availability, while supporting custom sorting options.
 * Results are returned in a paginated format for efficient data retrieval.
 * </p>
 *
 * @see PhoneMultipleFilterRequest
 * @see PageResponseDto
 * @see PhoneResponseDto
 */
public interface PhoneFilteringAndSortingService {
    /**
     * Filters and sorts phone entities based on the provided criteria and returns a paginated response.
     * <p>
     * This method applies multiple filters including brand, price range, and stock availability,
     * and sorts the results according to the specified sort criteria. The filtered and sorted
     * results are returned as a paginated response containing phone DTOs and pagination metadata.
     * </p>
     *
     * @param phoneFilterDto the filter criteria containing brands, price range, stock status, and sort options
     * @param pageable       the pagination information including page number and size
     * @return a {@link PageResponseDto} containing the filtered and sorted {@link PhoneResponseDto} objects,
     * along with pagination metadata such as total elements, total pages, and navigation flags
     * @throws org.springframework.dao.DataAccessException if there is an error accessing the database
     */

    PageResponseDto<PhoneResponseDto> filterAndSort(PhoneMultipleFilterRequest phoneFilterDto, Pageable pageable);
}