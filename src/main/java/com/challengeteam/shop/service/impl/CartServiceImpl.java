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
import com.challengeteam.shop.utility.CartUtility;
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
    public Cart putItemToCart(Cart cart, CartItemAddRequestDto cartItemAddRequestDto) {
        Objects.requireNonNull(cart, "cart");
        Objects.requireNonNull(cartItemAddRequestDto, "cartItemAddRequestDto");

        Long phoneId = cartItemAddRequestDto.phoneId();
        Integer amount = cartItemAddRequestDto.amount();

        Phone phone = phoneService.getById(phoneId)
                .orElseThrow(() -> new ResourceNotFoundException("Phone with id " + phoneId + " not found"));

        boolean isCartHasPhone = CartUtility.isCartHasPhone(cart, phoneId);

        if (isCartHasPhone) {
            throw new PhoneAlreadyInCartException("Phone with id " + phoneId + " already in cart with id " + cart.getId());
        } else {
            CartItem cartItem = CartItem.builder()
                    .phone(phone)
                    .cart(cart)
                    .amount(amount)
                    .build();

            cartItemRepository.save(cartItem);
            log.debug("Added new phone {} to cart {}", phoneId, cart.getId());
        }

        cart.setTotalPrice(CartUtility.countCartTotalPrice(cart));
        cartRepository.save(cart);

        return cart;
    }

    @Transactional
    @Override
    public Cart updateAmountCartItem(Cart cart, CartItemUpdateRequestDto cartItemUpdateRequestDto) {
        Objects.requireNonNull(cart, "cart");
        Objects.requireNonNull(cartItemUpdateRequestDto, "cartItemUpdateRequestDto");

        Long phoneId = cartItemUpdateRequestDto.phoneId();
        Integer amount = cartItemUpdateRequestDto.newAmount();

        CartItem cartItem = cartItemRepository.findByCartIdAndPhoneId(cart.getId(), phoneId)
                        .orElseThrow(() -> new ResourceNotFoundException("Phone with id " + phoneId + " not found in cart"));

        cartItem.setAmount(amount);
        cartItemRepository.save(cartItem);

        cart.setTotalPrice(CartUtility.countCartTotalPrice(cart));
        cartRepository.save(cart);

        log.debug("Updated phone in cart: {}", cartItem);
        return cart;
    }

    @Transactional
    @Override
    public Cart removeItemFromCart(Cart cart, Long phoneId) {
        Objects.requireNonNull(cart, "cart");
        Objects.requireNonNull(phoneId, "phoneId");

        CartItem cartItem = cartItemRepository.findByCartIdAndPhoneId(cart.getId(), phoneId)
                .orElseThrow(() -> new ResourceNotFoundException("Phone with id " + phoneId + " not found in cart"));

        cart.getCartItems().remove(cartItem);
        cartItemRepository.delete(cartItem);

        cart.setTotalPrice(CartUtility.countCartTotalPrice(cart));
        cartRepository.save(cart);

        log.debug("Removed phone in cart: {}", cartItem);
        return cart;
    }

    @Transactional
    @Override
    public Cart clearCart(Cart cart) {
        Objects.requireNonNull(cart, "cart");

        cart.getCartItems().clear();
        cartItemRepository.deleteAllByCartId(cart.getId());

        cart.setTotalPrice(BigDecimal.ZERO);
        cartRepository.save(cart);

        log.debug("Cleared cart: {}", cart);
        return cart;
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Cart> getCartByUserId(Long userId) {
        Objects.requireNonNull(userId, "userId");

        log.debug("Get cartId by userId: {}", userId);

        return cartRepository.findByUserId(userId);
    }

}
