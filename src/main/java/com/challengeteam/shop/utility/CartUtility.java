package com.challengeteam.shop.utility;

import com.challengeteam.shop.entity.cart.Cart;
import com.challengeteam.shop.entity.cart.CartItem;

import java.math.BigDecimal;
import java.util.Objects;

public class CartUtility {

    public static BigDecimal countCartTotalPrice(Cart cart) {
        Objects.requireNonNull(cart, "cart");

        return cart.getCartItems().stream()
                .map(item -> {
                    BigDecimal price = item.getVariant() == null
                            ? item.getPhone().getPrice()
                            : item.getVariant().getPrice();
                    return price.multiply(BigDecimal.valueOf(item.getAmount()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public static Integer countTotalAmount(Cart cart) {
        Objects.requireNonNull(cart, "cart");

        return cart.getCartItems().stream()
                .mapToInt(CartItem::getAmount)
                .sum();
    }

    public static boolean isCartHasPhone(Cart cart, Long phoneId) {
        Objects.requireNonNull(cart, "cart");
        Objects.requireNonNull(phoneId, "phoneId");

        return cart.getCartItems().stream()
                .anyMatch(item -> item.getPhone().getId().equals(phoneId));
    }

    public static boolean isCartHasVariant(Cart cart, Long variantId) {
        Objects.requireNonNull(cart, "cart");
        Objects.requireNonNull(variantId, "variantId");

        return cart.getCartItems().stream()
                .anyMatch(item -> item.getVariant() != null && item.getVariant().getId().equals(variantId));
    }

    public static Integer getCartItemAmount(Cart cart, Long phoneId) {
        Objects.requireNonNull(cart, "cart");
        Objects.requireNonNull(phoneId, "phoneId");

        return cart.getCartItems().stream()
                .filter(item -> item.getPhone().getId().equals(phoneId))
                .map(CartItem::getAmount)
                .findFirst()
                .orElse(0);
    }

    public static Integer getCartItemAmountByVariant(Cart cart, Long variantId) {
        Objects.requireNonNull(cart, "cart");
        Objects.requireNonNull(variantId, "variantId");

        return cart.getCartItems().stream()
                .filter(item -> item.getVariant() != null && item.getVariant().getId().equals(variantId))
                .map(CartItem::getAmount)
                .findFirst()
                .orElse(0);
    }

}
