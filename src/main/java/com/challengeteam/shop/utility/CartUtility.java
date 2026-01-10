package com.challengeteam.shop.utility;

import com.challengeteam.shop.entity.cart.Cart;
import com.challengeteam.shop.entity.cart.CartItem;

import java.math.BigDecimal;
import java.util.Objects;

public class CartUtility {

    public static BigDecimal countCartTotalPrice(Cart cart) {
        Objects.requireNonNull(cart, "cart");

        return cart.getCartItems().stream()
                .map(item -> item.getPhone().getPrice().multiply(BigDecimal.valueOf(item.getAmount())))
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

    public static Integer getCartItemAmount(Cart cart, Long phoneId) {
        Objects.requireNonNull(cart, "cart");
        Objects.requireNonNull(phoneId, "phoneId");

        return cart.getCartItems().stream()
                .filter(item -> item.getPhone().getId().equals(phoneId))
                .map(CartItem::getAmount)
                .findFirst()
                .orElse(0);
    }

}
