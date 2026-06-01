package com.challengeteam.shop.web.controller.order;

import com.challengeteam.shop.dto.order.OrderResponseDto;
import com.challengeteam.shop.dto.order.request.checkout.CheckoutRequestDto;
import com.challengeteam.shop.dto.order.request.order.OrderRequestDto;
import com.challengeteam.shop.service.order.create.OrderCreatorService;
import com.challengeteam.shop.service.order.customer.CustomerOrderService;
import com.challengeteam.shop.service.order.find.OrderFetchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Validated
@Tag(name = "Orders", description = "Manage customer orders")
public class OrderController {
    private final OrderCreatorService orderCreatorService;
    private final OrderFetchService orderFetchService;
    private final CustomerOrderService customerOrderService;

    @Operation(
            summary = "Create a new order (legacy)",
            description = "Legacy direct order creation endpoint. Prefer /api/v1/orders/checkout for authenticated customer checkout from cart."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Order created successfully",
                    content = @Content(schema = @Schema(implementation = OrderResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid order data provided"
            )
    })
    @PostMapping
    @Deprecated
    public ResponseEntity<OrderResponseDto> create(@Valid @RequestBody OrderRequestDto orderRequestDto,
                                                   Authentication authentication) {
        OrderResponseDto response = orderCreatorService.create(orderRequestDto, authentication);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    @Operation(
            summary = "Checkout current user's cart",
            description = "Creates an order from the authenticated user's current cart and clears the cart after successful order creation.",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Order created successfully",
                    content = @Content(schema = @Schema(implementation = OrderResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid checkout data provided"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "User is not authenticated"
            )
    })
    @PostMapping("/checkout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OrderResponseDto> checkout(@Valid @RequestBody CheckoutRequestDto checkoutRequestDto,
                                                     Authentication authentication) {
        OrderResponseDto response = customerOrderService.checkoutFromCart(checkoutRequestDto, authentication);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    @Operation(
            summary = "Get my orders",
            description = "Returns a paginated list of orders for the currently logged-in user",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "List of orders retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Page.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "User is not authenticated"
            )
    })
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<OrderResponseDto>> getMyOrders(
            @Parameter(hidden = true) Authentication authentication,
            @Parameter(description = "Pagination parameters (page number, size, sort)")
            Pageable pageable) {
        return ResponseEntity.ok(orderFetchService.getMyOrders(authentication, pageable));
    }

    @Operation(
            summary = "Get order by order ID",
            description = "Returns detailed information about a specific order using its ID",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Order found and returned successfully",
                    content = @Content(schema = @Schema(implementation = OrderResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Order not found with the given ID"
            ),
    })
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDto> getByOrderNumber(
            @Parameter(description = "The unique ID of the order")
            @PathVariable long id,
            Authentication authentication) {
        return ResponseEntity.ok(orderFetchService.getByOrderId(id, authentication));
    }

    @Operation(
            summary = "Cancel my order",
            description = "Cancels the authenticated customer's order when the current status still allows cancellation.",
            security = @SecurityRequirement(name = "bearer-jwt")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Order cancelled successfully",
                    content = @Content(schema = @Schema(implementation = OrderResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Order status does not allow cancellation"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Order not found"
            )
    })
    @PostMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OrderResponseDto> cancel(
            @Parameter(description = "The unique ID of the order")
            @PathVariable long id,
            Authentication authentication) {
        return ResponseEntity.ok(customerOrderService.cancel(id, authentication));
    }
}
