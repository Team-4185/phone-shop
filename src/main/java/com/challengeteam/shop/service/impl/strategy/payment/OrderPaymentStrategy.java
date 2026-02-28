package com.challengeteam.shop.service.impl.strategy.payment;

import com.challengeteam.shop.dto.order.CreateOrderResponseDto;
import com.challengeteam.shop.entity.order.Order;
import com.challengeteam.shop.entity.order.PaymentMethod;

public interface OrderPaymentStrategy {

    PaymentMethod getSupportedMethod();
    CreateOrderResponseDto processPayment(Order order);

}
