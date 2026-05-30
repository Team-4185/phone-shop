package com.challengeteam.shop.service.order.find;

import com.challengeteam.shop.dto.order.OrderResponseDto;
import com.challengeteam.shop.entity.order.Order;
import com.challengeteam.shop.entity.user.User;
import com.challengeteam.shop.exceptionHandling.exception.order.OrderNotFoundException;
import com.challengeteam.shop.exceptionHandling.exception.security.UnauthorizedException;
import com.challengeteam.shop.mapper.order.OrderMapper;
import com.challengeteam.shop.persistence.repository.OrderRepository;
import com.challengeteam.shop.utility.AuthenticationUserExtractorHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of {@link OrderFetchService} for retrieving order information.
 * This service provides read-only transactional access to order data,
 * supporting both user-specific order retrieval and individual order lookup.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class OrderFetchServiceImpl implements OrderFetchService {

    private final OrderRepository orderRepository;
    private final AuthenticationUserExtractorHelper authenticationUserExtractorHelper;
    private final OrderMapper orderMapper;

    /**
     * Retrieves a paginated list of orders for the authenticated user.
     *
     * @param authentication the authentication object containing the current user's security context
     * @param pageable       pagination information including page number, size, and sorting
     * @return a {@link Page} of {@link OrderResponseDto} containing the user's orders
     * @throws UnauthorizedException if the user is not authenticated
     */
    @Override
    public Page<OrderResponseDto> getMyOrders(Authentication authentication, Pageable pageable) {
        User user = authenticationUserExtractorHelper
                .extractUserFromSecurityContextHolder(authentication)
                .orElseThrow(() -> new UnauthorizedException("User not authenticated"));

        return orderRepository.findByUser(user, pageable)
                .map(orderMapper::toDto);
    }

    /**
     * Retrieves a specific order by its unique identifier.
     * The order is fetched with its associated items eagerly loaded.
     *
     * @param orderID the unique identifier of the order to retrieve
     * @return an {@link OrderResponseDto} containing the order details
     * @throws OrderNotFoundException if no order is found with the given ID
     */
    @Override
    public OrderResponseDto getByOrderId(long orderID, Authentication authentication) {
        Order order = orderRepository.findByIdWithItems(orderID)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderID));

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            User user = authenticationUserExtractorHelper
                    .extractUserFromSecurityContextHolder(authentication)
                    .orElseThrow(() -> new UnauthorizedException("User not authenticated"));

            if (order.getUser() == null || !order.getUser().getId().equals(user.getId())) {
                throw new OrderNotFoundException("Order not found: " + orderID);
            }
        }
        return orderMapper.toDto(order);
    }
}