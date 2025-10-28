package com.challengeteam.shop.service.impl;

import com.challengeteam.shop.dto.cart.CartItemAddRequestDto;
import com.challengeteam.shop.dto.cart.CartItemUpdateRequestDto;
import com.challengeteam.shop.entity.cart.Cart;
import com.challengeteam.shop.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {


    @Override
    public Optional<Cart> getCart(Long userId) {
        return Optional.empty();
    }

    @Override
    public void addItemToCart(Long id, CartItemAddRequestDto cartItemAddRequestDto) {

    }

    @Override
    public void updateUserCartItem(Long userId, CartItemUpdateRequestDto cartItemUpdateRequestDto) {

    }

    @Override
    public void removeItemFromUserCart(Long userId, Long phoneId) {

    }

    @Override
    public void clearUserCart(Long userId) {

    }
}
