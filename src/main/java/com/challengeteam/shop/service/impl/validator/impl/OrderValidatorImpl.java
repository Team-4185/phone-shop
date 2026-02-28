package com.challengeteam.shop.service.impl.validator.impl;

import com.challengeteam.shop.dto.order.OrderDestination;
import com.challengeteam.shop.dto.order.OrderPaymentDetail;
import com.challengeteam.shop.dto.order.OrderRecipient;
import com.challengeteam.shop.entity.phone.Phone;
import com.challengeteam.shop.entity.user.User;
import com.challengeteam.shop.exceptionHandling.exception.CriticalSystemException;
import com.challengeteam.shop.exceptionHandling.exception.ValidationException;
import com.challengeteam.shop.service.PriceService;
import com.challengeteam.shop.service.UserService;
import com.challengeteam.shop.service.impl.validator.OrderValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RequiredArgsConstructor
@Component
public class OrderValidatorImpl implements OrderValidator {
    public final static BigDecimal TOTAL_PRICE_MAX = new BigDecimal("999999.99");
    private final UserService userService;
    private final PriceService priceService;

    @Override
    public void validateProducts(Map<Phone, Integer> products) {
        Objects.requireNonNull(products, "products");

        if  (products.isEmpty()) {
            throw new ValidationException("Products are missing");
        }

        BigDecimal totalPrice = priceService.calculateTotalPrice(products);
        if (totalPrice.compareTo(TOTAL_PRICE_MAX) > 0) {
            throw new ValidationException("Total price must be less than " + TOTAL_PRICE_MAX);
        }
    }

    @Override
    public void validateUser(User user) {
        Objects.requireNonNull(user, "user");

        if (!userService.existsByEmail(user.getEmail())) {
            throw new ValidationException("User with email " + user.getEmail() + " is missing");
        }
    }

    @Override
    public void validateDestination(OrderDestination destination) {
        Objects.requireNonNull(destination, "destination");
    }

    @Override
    public void validateRecipient(OrderRecipient recipient) {
        Objects.requireNonNull(recipient, "recipient");
    }

    @Override
    public void validatePaymentDetail(OrderPaymentDetail paymentDetail) {
        Objects.requireNonNull(paymentDetail, "paymentDetail");
    }

}
