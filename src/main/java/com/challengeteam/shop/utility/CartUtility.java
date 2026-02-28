package com.challengeteam.shop.utility;

import com.challengeteam.shop.entity.cart.Cart;
import com.challengeteam.shop.entity.cart.CartItem;

import java.math.BigDecimal;
import java.util.Objects;

public class CartUtility {

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
