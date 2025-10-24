package com.challengeteam.shop.service.impl;

import com.challengeteam.shop.dto.cart.CartItemAddRequestDto;
import com.challengeteam.shop.dto.cart.CartItemUpdateRequestDto;
import com.challengeteam.shop.entity.cart.Cart;
import com.challengeteam.shop.entity.cart.CartItem;
import com.challengeteam.shop.entity.phone.Phone;
import com.challengeteam.shop.entity.user.User;
import com.challengeteam.shop.exceptionHandling.exception.ResourceNotFoundException;
import com.challengeteam.shop.repository.CartRepository;
import com.challengeteam.shop.service.CartService;
import com.challengeteam.shop.service.PhoneService;
import com.challengeteam.shop.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final UserService userService;
    private final PhoneService phoneService;

    @Override
    public Optional<Cart> getByUsername(String username) {
        return Optional.of(getCartByUsername(username));
    }

    @Override
    public void addItemToCart(String username, CartItemAddRequestDto dto) {
        Cart cart = getCartByUsername(username);

        Phone phone = phoneService.getById(dto.phoneId())
                .orElseThrow(() -> new ResourceNotFoundException("Phone not found"));

        CartItem cartItem = CartItem.builder()
                .cart(cart)
                .phone(phone)
                .amount(dto.amount())
                .build();

        Optional<CartItem> existingItem = cart.getCartItems().stream()
                .filter(ci -> ci.getPhone().getId().equals(phone.getId()))
                .findFirst();

        if (existingItem.isPresent()) {
            existingItem.get().setAmount(existingItem.get().getAmount() + dto.amount());
        } else {
            cart.getCartItems().add(cartItem);
        }

        updateCartTotalPrice(cart);
    }

    @Override
    public void updateItem(String username, CartItemUpdateRequestDto dto) {
        Cart cart = getCartByUsername(username);

        CartItem cartItem = cart.getCartItems().stream()
                .filter(item -> item.getPhone().getId().equals(dto.phoneId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        cartItem.setAmount(dto.newAmount());

        updateCartTotalPrice(cart);
    }

    @Override
    public void removeItem(String username, Long phoneId) {
        Cart cart = getCartByUsername(username);

        boolean removed = cart.getCartItems().removeIf(item -> item.getPhone().getId().equals(phoneId));
        if (!removed) throw new ResourceNotFoundException("Cart item not found");

        updateCartTotalPrice(cart);
    }

    @Override
    public void clearCart(String username) {
        Cart cart = getCartByUsername(username);
        cart.getCartItems().clear();
        cart.setTotalPrice(BigDecimal.ZERO);
        cartRepository.save(cart);
    }

    private Cart getCartByUsername(String username) {
        User user = userService.getByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
    }

    private void updateCartTotalPrice(Cart cart) {
        BigDecimal total = cart.getCartItems().stream()
                .map(item -> item.getPhone().getPrice().multiply(BigDecimal.valueOf(item.getAmount())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        cart.setTotalPrice(total);
        cartRepository.save(cart);
    }
}
