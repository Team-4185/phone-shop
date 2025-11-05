package com.challengeteam.shop.service.impl;

import com.challengeteam.shop.dto.cart.CartItemAddRequestDto;
import com.challengeteam.shop.dto.cart.CartItemUpdateRequestDto;
import com.challengeteam.shop.entity.cart.Cart;
import com.challengeteam.shop.entity.cart.CartItem;
import com.challengeteam.shop.entity.phone.Phone;
import com.challengeteam.shop.exceptionHandling.exception.PhoneAlreadyInCartException;
import com.challengeteam.shop.exceptionHandling.exception.ResourceNotFoundException;
import com.challengeteam.shop.persistence.repository.CartItemRepository;
import com.challengeteam.shop.persistence.repository.CartRepository;
import com.challengeteam.shop.service.CartService;
import com.challengeteam.shop.service.PhoneService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;

    private final CartItemRepository cartItemRepository;

    private final PhoneService phoneService;

    @Transactional(readOnly = true)
    @Override
    public Optional<Cart> getCart(Long id) {
        Objects.requireNonNull(id, "id");

        log.debug("Get cart by id: {}", id);
        return cartRepository.findById(id);
    }

    @Transactional
    @Override
    public Optional<Cart> addItemToCart(Long cartId, CartItemAddRequestDto cartItemAddRequestDto) {
        Objects.requireNonNull(cartId, "cartId");
        Objects.requireNonNull(cartItemAddRequestDto, "cartItemAddRequestDto");

        Long phoneId = cartItemAddRequestDto.phoneId();
        Integer amount = cartItemAddRequestDto.amount();

        if (cartItemRepository.existsByCartIdAndPhoneId(cartId, phoneId)) {
            throw new PhoneAlreadyInCartException("Phone with id  " + phoneId + " already in cart");
        }

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart with id " + cartId + " not found"));

        Phone phone = phoneService.getById(phoneId)
                .orElseThrow(() -> new ResourceNotFoundException("Phone with id " + phoneId + " not found"));

        CartItem cartItem = CartItem.builder()
                .phone(phone)
                .cart(cart)
                .amount(amount)
                .build();

        cartItemRepository.save(cartItem);

        cart.setTotalPrice(countCartTotalPrice(cart));
        cartRepository.save(cart);

        log.debug("Added new phone in cart: {}", cartItem);
        return Optional.of(cart);
    }

    @Transactional
    @Override
    public Optional<Cart> updateAmountCartItem(Long cartId, CartItemUpdateRequestDto cartItemUpdateRequestDto) {
        Objects.requireNonNull(cartId, "cartId");
        Objects.requireNonNull(cartItemUpdateRequestDto, "cartItemUpdateRequestDto");

        Long phoneId = cartItemUpdateRequestDto.phoneId();
        Integer amount = cartItemUpdateRequestDto.newAmount();

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart with id " + cartId + " not found"));

        CartItem cartItem = cartItemRepository.findByCartIdAndPhoneId(cartId, phoneId)
                        .orElseThrow(() -> new ResourceNotFoundException("Phone with id " + phoneId + " not found in cart"));

        cartItem.setAmount(amount);
        cartItemRepository.save(cartItem);

        cart.setTotalPrice(countCartTotalPrice(cart));
        cartRepository.save(cart);

        log.debug("Updated phone in cart: {}", cartItem);
        return Optional.of(cart);
    }

    @Transactional
    @Override
    public Optional<Cart> removeItemFromCart(Long cartId, Long phoneId) {
        Objects.requireNonNull(cartId, "cartId");
        Objects.requireNonNull(phoneId, "phoneId");

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart with id " + cartId + " not found"));

        CartItem cartItem = cartItemRepository.findByCartIdAndPhoneId(cartId, phoneId)
                .orElseThrow(() -> new ResourceNotFoundException("Phone with id " + phoneId + " not found in cart"));

        cartItemRepository.delete(cartItem);

        cart.setTotalPrice(countCartTotalPrice(cart));
        cartRepository.save(cart);

        log.debug("Removed phone in cart: {}", cartItem);
        return Optional.of(cart);
    }

    @Transactional
    @Override
    public Optional<Cart> clearCart(Long cartId) {
        Objects.requireNonNull(cartId, "cartId");

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart with id " + cartId + " not found"));


        cartItemRepository.deleteAllByCartId(cartId);

        cart.setTotalPrice(BigDecimal.ZERO);
        cartRepository.save(cart);

        log.debug("Cleared cart: {}", cart);
        return Optional.of(cart);
    }

    @Transactional(readOnly = true)
    @Override
    public Cart getCartByUserId(Long userId) {
        Objects.requireNonNull(userId, "userId");

        log.debug("Get cartId by userId: {}", userId);

        return cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart for user with id " + userId + " not found"));

    }

    private BigDecimal countCartTotalPrice(Cart cart) {
        return cart.getCartItems().stream()
                .map(item -> item.getPhone().getPrice().multiply(BigDecimal.valueOf(item.getAmount())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}
