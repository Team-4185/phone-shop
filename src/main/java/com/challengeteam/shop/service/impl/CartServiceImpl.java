package com.challengeteam.shop.service.impl;

import com.challengeteam.shop.dto.cart.CartItemAddRequestDto;
import com.challengeteam.shop.entity.cart.Cart;
import com.challengeteam.shop.entity.cart.CartItem;
import com.challengeteam.shop.entity.phone.Phone;
import com.challengeteam.shop.entity.phone.ProductVariant;
import com.challengeteam.shop.exceptionHandling.exception.ResourceNotFoundException;
import com.challengeteam.shop.exceptionHandling.exception.phone.PhoneAlreadyInCartException;
import com.challengeteam.shop.persistence.repository.CartItemRepository;
import com.challengeteam.shop.persistence.repository.CartRepository;
import com.challengeteam.shop.persistence.repository.ProductVariantRepository;
import com.challengeteam.shop.service.CartService;
import com.challengeteam.shop.service.PhoneService;
import com.challengeteam.shop.service.impl.validator.CartValidator;
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

    private final ProductVariantRepository productVariantRepository;

    private final CartValidator cartValidator;

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

        cartValidator.validateItemAmount(cartItemAddRequestDto.amount());

        ProductVariant variant = resolveVariant(cartItemAddRequestDto);
        Long variantId = variant.getId();
        Integer amount = cartItemAddRequestDto.amount();

        Phone phone = variant.getPhone();

        ensureVariantIsNotInCart(cart, variantId);
        CartItem cartItem = CartItem.builder()
                .phone(phone)
                .variant(variant)
                .cart(cart)
                .amount(amount)
                .build();

        cart.getCartItems().add(cartItem);

        cartValidator.validateTotalAmount(cart);
        cartItemRepository.save(cartItem);
        log.debug("Added variant {} to cart {}", variantId, cart.getId());

        cart.setTotalPrice(CartUtility.countCartTotalPrice(cart));
        cartRepository.save(cart);

        return cart;
    }

    @Transactional
    @Override
    public Cart updateAmountCartItem(Cart cart, Long variantId, Integer amount) {
        Objects.requireNonNull(cart, "cart");
        Objects.requireNonNull(variantId, "variantId");
        Objects.requireNonNull(amount, "amount");

        cartValidator.validateItemAmount(amount);

        CartItem cartItem = cart.getCartItems().stream()
                .filter(i -> i.getVariant() != null && i.getVariant().getId().equals(variantId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Variant with id " + variantId + " not found in cart"));

        cartItem.setAmount(amount);
        cartValidator.validateTotalAmount(cart);
        cartItemRepository.save(cartItem);

        cart.setTotalPrice(CartUtility.countCartTotalPrice(cart));
        cartRepository.save(cart);

        log.debug("Updated variant in cart: {}", cartItem);
        return cart;
    }

    @Transactional
    @Override
    public Cart removeItemFromCart(Cart cart, Long variantId) {
        Objects.requireNonNull(cart, "cart");
        Objects.requireNonNull(variantId, "variantId");

        CartItem cartItem = cartItemRepository.findByCartIdAndVariantId(cart.getId(), variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Variant with id " + variantId + " not found in cart"));

        cart.getCartItems().remove(cartItem);
        cartItemRepository.delete(cartItem);

        cart.setTotalPrice(CartUtility.countCartTotalPrice(cart));
        cartRepository.save(cart);

        log.debug("Removed variant from cart: {}", cartItem);
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

    private ProductVariant resolveVariant(CartItemAddRequestDto request) {
        return Optional.ofNullable(request.variantId())
                .map(this::getVariantById)
                .orElseGet(() -> resolveDefaultVariantByPhoneId(request.phoneId()));
    }

    private ProductVariant getVariantById(Long variantId) {
        return productVariantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product variant with id " + variantId + " not found"));
    }

    private ProductVariant resolveDefaultVariantByPhoneId(Long phoneId) {
        Long resolvedPhoneId = Optional.ofNullable(phoneId)
                .orElseThrow(() -> new ResourceNotFoundException("Product variant id or phone id must be provided"));
        phoneService.getById(resolvedPhoneId)
                .orElseThrow(() -> new ResourceNotFoundException("Phone with id " + resolvedPhoneId + " not found"));
        return productVariantRepository.findAllByPhoneIdOrderByPriceAscIdAsc(resolvedPhoneId).stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product variant for phone with id " + resolvedPhoneId + " not found"));
    }

    private void ensureVariantIsNotInCart(Cart cart, Long variantId) {
        if (CartUtility.isCartHasVariant(cart, variantId)) {
            throw new PhoneAlreadyInCartException(
                    "Variant with id " + variantId + " already in cart with id " + cart.getId());
        }
    }

}
