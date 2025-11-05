package com.challengeteam.shop.web.controller;

import com.challengeteam.shop.dto.cart.CartItemAddRequestDto;
import com.challengeteam.shop.dto.cart.CartItemUpdateRequestDto;
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

    @PostMapping("/add")
    public ResponseEntity<CartResponseDto> addItemToUserCart(@AuthenticationPrincipal SimpleUserDetails simpleUserDetails,
                                        @RequestBody CartItemAddRequestDto cartItemAddRequestDto) {
        Long userId = simpleUserDetails.getUserId();
        Cart cart = userCartService.addItemToUserCart(userId, cartItemAddRequestDto)
                .orElseThrow(() -> new ResourceNotFoundException("Cart for user with id " + userId + " not found"));
        CartResponseDto cartResponseDto = cartMapper.toCartResponseDto(cart);
        return ResponseEntity.ok(cartResponseDto);
    }

    @PutMapping("/update")
    public ResponseEntity<CartResponseDto> updateAmountUserCartItem(@AuthenticationPrincipal SimpleUserDetails simpleUserDetails,
                                           @RequestBody CartItemUpdateRequestDto cartItemUpdateRequestDto) {
        Long userId = simpleUserDetails.getUserId();
        Cart cart = userCartService.updateAmountUserCartItem(userId, cartItemUpdateRequestDto)
                .orElseThrow(() -> new ResourceNotFoundException("Cart for user with id " + userId + " not found"));
        CartResponseDto cartResponseDto = cartMapper.toCartResponseDto(cart);
        return ResponseEntity.ok(cartResponseDto);
    }

    @DeleteMapping("/remove/{phoneId}")
    public ResponseEntity<CartResponseDto> removeItem(@AuthenticationPrincipal SimpleUserDetails simpleUserDetails,
                                           @PathVariable Long phoneId) {
        Long userId = simpleUserDetails.getUserId();
        Cart cart = userCartService.removeItemFromUserCart(userId, phoneId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart for user with id " + userId + " not found"));
        CartResponseDto cartResponseDto = cartMapper.toCartResponseDto(cart);
        return ResponseEntity.ok(cartResponseDto);
    }

    @DeleteMapping("/clear")
    public ResponseEntity<CartResponseDto> clearCart(@AuthenticationPrincipal SimpleUserDetails simpleUserDetails) {
        Long userId = simpleUserDetails.getUserId();
        Cart cart = userCartService.clearUserCart(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart for user with id " + userId + " not found"));
        CartResponseDto cartResponseDto = cartMapper.toCartResponseDto(cart);
        return ResponseEntity.ok(cartResponseDto);
    }

}
