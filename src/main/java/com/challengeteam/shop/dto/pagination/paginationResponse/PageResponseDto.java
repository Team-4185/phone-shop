package com.challengeteam.shop.dto.pagination.paginationResponse;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Data Transfer Object for paginated responses.
 * <p>
 * This record encapsulates pagination metadata along with the page content,
 * providing a standardized structure for paginated API responses.
 * </p>
 *
 * @param <T>           the type of elements in the page content
 * @param content       the list of items for the current page
 * @param page          the current page number (1-indexed)
 * @param size          the number of items per page
 * @param totalElements the total number of elements across all pages
 * @param totalPages    the total number of pages
 * @param first         indicates whether this is the first page
 * @param last          indicates whether this is the last page
 */
public record PageResponseDto<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
    /**
     * Creates a PageResponseDto from a Spring Data Page object.
     * <p>
     * This factory method converts a Spring Data {@link org.springframework.data.domain.Page}
     * into a PageResponseDto, adjusting the page number to be 1-indexed (user-friendly)
     * instead of 0-indexed (Spring Data default).
     * </p>
     *
     * @param <T>  the type of elements in the page
     * @param page the Spring Data Page object to convert
     * @return a new PageResponseDto instance with data from the provided Page
     */
    public static <T> PageResponseDto<T> of(Page<T> page) {
        return new PageResponseDto<>(
                page.getContent(),
                page.getNumber() + 1,
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }
}