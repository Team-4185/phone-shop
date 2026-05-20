package com.challengeteam.shop.service.order.find;

import com.challengeteam.shop.dto.order.OrderResponseDto;
import com.challengeteam.shop.exceptionHandling.exception.OrderNotFoundException;
import com.challengeteam.shop.exceptionHandling.exception.UnauthorizedException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

/**
 * Service interface for fetching and retrieving order information.
 * Provides methods to retrieve orders for authenticated users and fetch specific orders by ID.
 */
public interface OrderFetchService {
    /**
     * Retrieves a paginated list of orders for the authenticated user.
     *
     * @param authentication the authentication object containing the current user's security context
     * @param pageable       pagination information including page number, size, and sorting
     * @return a {@link Page} of {@link OrderResponseDto} containing the user's orders
     * @throws UnauthorizedException if the user is not authenticated
     */
    Page<OrderResponseDto> getMyOrders(Authentication authentication, Pageable pageable);


    /**
     * Retrieves a specific order by its unique identifier for an authenticated user.
     * The order is fetched with its associated items eagerly loaded.
     *
     * @param orderID        the unique identifier of the order to retrieve
     * @param authentication the authentication object containing the current user's security context
     * @return an {@link OrderResponseDto} containing the order details
     * @throws OrderNotFoundException if no order is found with the given ID
     * @throws UnauthorizedException  if the user is not authenticated or not authorized to access the order
     */
    OrderResponseDto getByOrderId(long orderID, Authentication authentication);
}