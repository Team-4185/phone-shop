package com.challengeteam.shop.service.order.create;

import com.challengeteam.shop.dto.order.OrderResponseDto;
import com.challengeteam.shop.dto.order.request.order.OrderRequestDto;
import org.springframework.security.core.Authentication;

/**
 * Service interface for creating orders in the shop system.
 * <p>
 * This functional interface defines the contract for order creation operations,
 * handling the transformation of order requests into persisted order entities
 * with associated payment and shipping details.
 * </p>
 *
 * @see OrderRequestDto
 * @see OrderResponseDto
 * @see org.springframework.security.core.Authentication
 */
@FunctionalInterface
public interface OrderCreatorService {
    /**
     * Creates a new order based on the provided request data and user authentication.
     * <p>
     * This method processes the order request by:
     * <ul>
     *   <li>Validating the order items and their availability</li>
     *   <li>Calculating the total order amount</li>
     *   <li>Processing payment details</li>
     *   <li>Creating shipping address if delivery method requires it</li>
     *   <li>Persisting the order to the database</li>
     *   <li>Updating phone stock quantities</li>
     *   <li>Sending order confirmation notifications</li>
     * </ul>
     * </p>
     *
     * @param orderRequestDto the order request containing customer details, items,
     *                        payment method, delivery method, and associated information.
     *                        Must not be null and must pass all validation constraints.
     * @param authentication  the Spring Security authentication object containing
     *                        the authenticated user's information. Must not be null.
     * @return {@link OrderResponseDto} containing the created order details including
     * order ID, status, payment information, shipping address, and order items
     * @throws IllegalArgumentException                             if any requested phone items are not found
     *                                                              or if there is insufficient stock
     * @throws org.springframework.transaction.TransactionException if the transaction fails
     */
    OrderResponseDto create(OrderRequestDto orderRequestDto, Authentication authentication);
}