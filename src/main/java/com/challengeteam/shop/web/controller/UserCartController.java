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
        return ResponseEntity.ok(cartMapper.toCartResponseDto(cart));
    }

    @PostMapping("/add")
    public ResponseEntity<Void> addItemToUserCart(@AuthenticationPrincipal SimpleUserDetails simpleUserDetails,
                                        @RequestBody CartItemAddRequestDto cartItemAddRequestDto) {
        Long userId = simpleUserDetails.getUserId();
        userCartService.addItemToUserCart(userId, cartItemAddRequestDto);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/update")
    public ResponseEntity<Void> updateAmountUserCartItem(@AuthenticationPrincipal SimpleUserDetails simpleUserDetails,
                                           @RequestBody CartItemUpdateRequestDto cartItemUpdateRequestDto) {
        Long userId = simpleUserDetails.getUserId();
        userCartService.updateAmountUserCartItem(userId, cartItemUpdateRequestDto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/remove/{phoneId}")
    public ResponseEntity<Void> removeItem(@AuthenticationPrincipal SimpleUserDetails simpleUserDetails,
                                           @PathVariable Long phoneId) {
        Long userId = simpleUserDetails.getUserId();
        userCartService.removeItemFromUserCart(userId, phoneId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearCart(@AuthenticationPrincipal SimpleUserDetails simpleUserDetails) {
        Long userId = simpleUserDetails.getUserId();
        userCartService.clearUserCart(userId);
        return ResponseEntity.noContent().build();
    }

}
