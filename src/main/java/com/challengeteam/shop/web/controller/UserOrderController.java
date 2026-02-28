package com.challengeteam.shop.web.controller;

import com.challengeteam.shop.dto.order.CreateOrderDto;
import com.challengeteam.shop.dto.order.CreateOrderResponseDto;
import com.challengeteam.shop.security.SimpleUserDetailsService.SimpleUserDetails;
import com.challengeteam.shop.service.UserOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/me/orders")
@SecurityRequirement(name = "bearer-jwt")
public class UserOrderController {
    private final UserOrderService userOrderService;

    @Operation(
            summary = "Creates an order",
            description = """
                    Creates new order with phones from user cart.
                    
                    It is possible to create order with two payment options:
                    - ONLINE - payment process executes immediately
                    - POSTPAID - payment goes after retrieving goods
                    
                    The max value of total price you can create order is 999,999.99
                    
                    Tips: for ONLINE payment method you can use non-real cards for testing.
                    Such cards already exist, so you can find it here: https://docs.stripe.com/testing?testing-method=card-numbers
                    """
    )
    @PostMapping
    public ResponseEntity<CreateOrderResponseDto> makeOrder(@RequestBody @Valid CreateOrderDto createOrderDto,
                                                            @AuthenticationPrincipal SimpleUserDetails userDetails) {
        CreateOrderResponseDto response = userOrderService.executeOrderWorkflow(userDetails.getUserId(), createOrderDto);
        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }

}
