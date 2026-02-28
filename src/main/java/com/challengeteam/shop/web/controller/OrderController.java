package com.challengeteam.shop.web.controller;

import com.challengeteam.shop.dto.order.OrderResponseDto;
import com.challengeteam.shop.dto.pagination.PageRequestDto;
import com.challengeteam.shop.dto.pagination.PageResponseDto;
import com.challengeteam.shop.entity.order.Order;
import com.challengeteam.shop.entity.user.User;
import com.challengeteam.shop.exceptionHandling.exception.ResourceNotFoundException;
import com.challengeteam.shop.mapper.OrderMapper;
import com.challengeteam.shop.service.OrderService;
import com.challengeteam.shop.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/orders")
@SecurityRequirement(name = "bearer-jwt")
public class OrderController {
    private final OrderService orderService;
    private final UserService userService;
    private final OrderMapper orderMapper;

    @Operation(
            summary = "Retrieves a Page of All Orders",
            description = """
                    Retrieves a single page of all orders in the system. The page contains information about the current page,
                    how many pages in total and directly objects describing the order.
                    
                    Specify the optional query parameters to retrieve a specific page:
                    - page (default: 1)
                    - size (default: 10)
                    """
    )
    @GetMapping
    public ResponseEntity<PageResponseDto<OrderResponseDto>> getOrders(@Valid PageRequestDto pageRequestDto) {
        int pageNumber = pageRequestDto.page() - 1;
        int sizeNumber = pageRequestDto.size();

        Page<OrderResponseDto> page = orderService
                .getOrders(pageNumber, sizeNumber)
                .map(orderMapper::toResponse);
        var response = PageResponseDto.of(page);

        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }

    @Operation(
            summary = "Retrieves an order by ID",
            description = "Specify order ID in request path to get object."
    )
    @GetMapping("/{id:\\d+}")
    public ResponseEntity<OrderResponseDto> getById(@PathVariable Long id) {
        Order order = orderService
                .getById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not found order with id: " + id));
        OrderResponseDto response = orderMapper.toResponse(order);

        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }

    @Operation(
            summary = "Retrieves all orders by user ID",
            description = "Specify user ID in request path to get list of all user's orders."
    )
    @GetMapping("/user/{userId:\\d+}")
    public ResponseEntity<List<OrderResponseDto>> getByUser(@PathVariable Long userId) {
        if (!userService.existsById(userId)) {
                throw new ResourceNotFoundException("Not found user with id: " + userId);
        }

        List<Order> orders = orderService.getOrdersByUserId(userId);
        var response = orderMapper.toResponse(orders);

        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }

}
