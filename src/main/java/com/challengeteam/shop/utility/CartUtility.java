package com.challengeteam.shop.utility;

import com.challengeteam.shop.entity.cart.Cart;
import com.challengeteam.shop.entity.cart.CartItem;

import java.math.BigDecimal;

public class CartUtility {

    public static BigDecimal countCartTotalPrice(Cart cart) {
        return cart.getCartItems().stream()
                .map(item -> item.getPhone().getPrice().multiply(BigDecimal.valueOf(item.getAmount())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public static boolean isCartHasPhone(Cart cart, Long phoneId) {
        return cart.getCartItems().stream()
                .anyMatch(item -> item.getPhone().getId().equals(phoneId));
    }

    public static Integer getCartItemAmount(Cart cart, Long phoneId) {
        return cart.getCartItems().stream()
                .filter(item -> item.getPhone().getId().equals(phoneId))
                .map(CartItem::getAmount)
                .findFirst()
                .orElse(0);
    }

}
