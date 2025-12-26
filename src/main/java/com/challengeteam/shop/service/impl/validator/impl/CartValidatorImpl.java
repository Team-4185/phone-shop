package com.challengeteam.shop.service.impl.validator.impl;

import com.challengeteam.shop.exceptionHandling.exception.InvalidCartItemAmountException;
import com.challengeteam.shop.properties.UserCartProperties;
import com.challengeteam.shop.service.impl.validator.CartValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CartValidatorImpl implements CartValidator {

    private final UserCartProperties userCartProperties;

    @Override
    public void validateCartItemAmountToUpdate(Integer amount) {
        if (amount < userCartProperties.getMinUpdate() || amount > userCartProperties.getMaxUpdate()) {
            throw new InvalidCartItemAmountException("Invalid cart item amount");
        }
    }

    @Override
    public void validateCartItemTotalAmount(Integer amount) {
        if (amount > 100) {
            throw new InvalidCartItemAmountException("Invalid cart item total amount");
        }
    }

}
