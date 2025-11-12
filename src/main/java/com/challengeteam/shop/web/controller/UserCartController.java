package com.challengeteam.shop.web.controller;

import com.challengeteam.shop.dto.cart.CartItemAddRequestDto;
import com.challengeteam.shop.dto.cart.CartItemRemoveRequestDto;
import com.challengeteam.shop.dto.cart.CartResponseDto;
import com.challengeteam.shop.entity.cart.Cart;
import com.challengeteam.shop.exceptionHandling.exception.ResourceNotFoundException;
import com.challengeteam.shop.mapper.CartMapper;
import com.challengeteam.shop.security.SimpleUserDetailsService.SimpleUserDetails;
import com.challengeteam.shop.service.UserCartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/me/cart")
@RequiredArgsConstructor
public class UserCartController {

    private final UserCartService userCartService;

    private final CartMapper cartMapper;

    @GetMapping
    public ResponseEntity<CartResponseDto> getUserCart(@AuthenticationPrincipal SimpleUserDetails simpleUserDetails) {
        Long userId = simpleUserDetails.getUserId();
        Cart cart = userCartService.getUserCart(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart for user with id " + userId + " not found"));
        CartResponseDto cartResponseDto = cartMapper.toCartResponseDto(cart);
        return ResponseEntity.ok(cartResponseDto);
    }

    @PostMapping("/put")
    public ResponseEntity<CartResponseDto> putItemToUserCart(@AuthenticationPrincipal SimpleUserDetails simpleUserDetails,
                                        @RequestBody CartItemAddRequestDto cartItemAddRequestDto) {
        Long userId = simpleUserDetails.getUserId();
        Cart cart = userCartService.putItemToUserCart(userId, cartItemAddRequestDto);
        CartResponseDto cartResponseDto = cartMapper.toCartResponseDto(cart);
        return ResponseEntity.ok(cartResponseDto);
    }

    @PostMapping("/remove")
    public ResponseEntity<CartResponseDto> removeItem(@AuthenticationPrincipal SimpleUserDetails simpleUserDetails,
                                           @RequestBody CartItemRemoveRequestDto cartItemRemoveRequestDto) {
        Long userId = simpleUserDetails.getUserId();
        Cart cart = userCartService.removeItemFromUserCart(userId, cartItemRemoveRequestDto);
        CartResponseDto cartResponseDto = cartMapper.toCartResponseDto(cart);
        return ResponseEntity.ok(cartResponseDto);
    }

    @PostMapping("/clear")
    public ResponseEntity<CartResponseDto> clearCart(@AuthenticationPrincipal SimpleUserDetails simpleUserDetails) {
        Long userId = simpleUserDetails.getUserId();
        Cart cart = userCartService.clearUserCart(userId);
        CartResponseDto cartResponseDto = cartMapper.toCartResponseDto(cart);
        return ResponseEntity.ok(cartResponseDto);
    }

}
