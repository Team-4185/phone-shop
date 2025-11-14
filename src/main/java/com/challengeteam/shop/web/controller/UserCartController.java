package com.challengeteam.shop.web.controller;

import com.challengeteam.shop.dto.cart.CartItemAddRequestDto;
import com.challengeteam.shop.dto.cart.CartItemRemoveRequestDto;
import com.challengeteam.shop.dto.cart.CartResponseDto;
import com.challengeteam.shop.entity.cart.Cart;
import com.challengeteam.shop.exceptionHandling.exception.ResourceNotFoundException;
import com.challengeteam.shop.mapper.CartMapper;
import com.challengeteam.shop.security.SimpleUserDetailsService.SimpleUserDetails;
import com.challengeteam.shop.service.UserCartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/me/cart")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
public class UserCartController {

    private final UserCartService userCartService;

    private final CartMapper cartMapper;

    @Operation(
            summary = "Get current user's cart",
            description = "Returns the authenticated user's shopping cart."
    )

    @GetMapping
    public ResponseEntity<CartResponseDto> getUserCart(@AuthenticationPrincipal SimpleUserDetails simpleUserDetails) {
        Long userId = simpleUserDetails.getUserId();
        Cart cart = userCartService.getUserCart(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart for user with id " + userId + " not found"));
        CartResponseDto cartResponseDto = cartMapper.toCartResponseDto(cart);
        return ResponseEntity.ok(cartResponseDto);
    }

    @Operation(
            summary = "Add item to cart",
            description = "Adds a product to the authenticated user's cart. "
                    + "If the product is already in the cart, its quantity is increased by the specified amount."
    )

    @PostMapping("/put")
    public ResponseEntity<CartResponseDto> putItemToUserCart(@AuthenticationPrincipal SimpleUserDetails simpleUserDetails,
                                        @RequestBody CartItemAddRequestDto cartItemAddRequestDto) {
        Long userId = simpleUserDetails.getUserId();
        Cart cart = userCartService.putItemToUserCart(userId, cartItemAddRequestDto);
        CartResponseDto cartResponseDto = cartMapper.toCartResponseDto(cart);
        return ResponseEntity.ok(cartResponseDto);
    }

    @Operation(
            summary = "Remove item from cart",
            description = "Decreases the quantity of an existing product in the user's cart by the specified amount. "
                    + "If the amount to remove is greater than or equal to the current quantity, "
                    + "the product is completely removed from the cart."
    )

    @PostMapping("/remove")
    public ResponseEntity<CartResponseDto> removeItemFromUserCart(@AuthenticationPrincipal SimpleUserDetails simpleUserDetails,
                                           @RequestBody CartItemRemoveRequestDto cartItemRemoveRequestDto) {
        Long userId = simpleUserDetails.getUserId();
        Cart cart = userCartService.removeItemFromUserCart(userId, cartItemRemoveRequestDto);
        CartResponseDto cartResponseDto = cartMapper.toCartResponseDto(cart);
        return ResponseEntity.ok(cartResponseDto);
    }

    @Operation(
            summary = "Clear user's cart",
            description = "Deletes all items from the authenticated user's cart."
    )

    @PostMapping("/clear")
    public ResponseEntity<CartResponseDto> clearCart(@AuthenticationPrincipal SimpleUserDetails simpleUserDetails) {
        Long userId = simpleUserDetails.getUserId();
        Cart cart = userCartService.clearUserCart(userId);
        CartResponseDto cartResponseDto = cartMapper.toCartResponseDto(cart);
        return ResponseEntity.ok(cartResponseDto);
    }

}
