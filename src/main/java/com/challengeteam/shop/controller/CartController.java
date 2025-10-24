package com.challengeteam.shop.controller;

import com.challengeteam.shop.dto.cart.CartItemAddRequestDto;
import com.challengeteam.shop.dto.cart.CartItemUpdateRequestDto;
import com.challengeteam.shop.dto.cart.CartResponseDto;
import com.challengeteam.shop.entity.cart.Cart;
import com.challengeteam.shop.exceptionHandling.exception.ResourceNotFoundException;
import com.challengeteam.shop.mapper.CartMapper;
import com.challengeteam.shop.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/carts")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    private final CartMapper cartMapper;

    @GetMapping
    public ResponseEntity<CartResponseDto> getMyCart(@AuthenticationPrincipal UserDetails userDetails) {
        Cart cart = cartService.getByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        CartResponseDto cartResponseDto = cartMapper.toCartResponseDto(cart);
        return ResponseEntity.ok(cartResponseDto);
    }

    @PostMapping("/add")
    public ResponseEntity<Void> addItem(@AuthenticationPrincipal UserDetails userDetails,
                                        @RequestBody CartItemAddRequestDto cartItemAddRequestDto) {
        cartService.addItemToCart(userDetails.getUsername(), cartItemAddRequestDto);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/update")
    public ResponseEntity<Void> updateItem(@AuthenticationPrincipal UserDetails userDetails,
                                           @RequestBody CartItemUpdateRequestDto cartItemUpdateRequestDto) {
        cartService.updateItem(userDetails.getUsername(), cartItemUpdateRequestDto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/remove/{phoneId}")
    public ResponseEntity<Void> removeItem(@AuthenticationPrincipal UserDetails userDetails,
                                           @PathVariable Long phoneId) {
        cartService.removeItem(userDetails.getUsername(), phoneId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearCart(@AuthenticationPrincipal UserDetails userDetails) {
        cartService.clearCart(userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

}
