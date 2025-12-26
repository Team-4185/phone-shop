package com.challengeteam.shop.service.impl.validator;

public interface CartValidator {

    void validateCartItemAmountToUpdate(Integer amount);

    void validateCartItemTotalAmount(Integer amount);

}
