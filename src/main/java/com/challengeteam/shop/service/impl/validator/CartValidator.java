package com.challengeteam.shop.service.impl.validator;

import com.challengeteam.shop.entity.cart.Cart;

public interface CartValidator {

    void validateItemAmount(Integer amount);

    void validateTotalAmount(Cart cart, Integer newAmount);

}
