package com.challengeteam.shop.service.impl.strategy.payment;

import com.challengeteam.shop.dto.order.CreateOrderResponseDto;
import com.challengeteam.shop.dto.order.OrderResponseDto;
import com.challengeteam.shop.entity.order.Order;
import com.challengeteam.shop.entity.order.OrderStatus;
import com.challengeteam.shop.entity.order.PaymentMethod;
import com.challengeteam.shop.mapper.OrderMapper;
import com.challengeteam.shop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@RequiredArgsConstructor
@Component
public class PostpaidOrderPaymentStrategy implements OrderPaymentStrategy {
    public final static PaymentMethod SUPPORTED_PAYMENT_METHOD = PaymentMethod.POSTPAID;
    private final OrderService orderService;
    private final OrderMapper orderMapper;

    @Override
    public PaymentMethod getSupportedMethod() {
        return SUPPORTED_PAYMENT_METHOD;
    }

    @Transactional
    @Override
    public CreateOrderResponseDto processPayment(Order order) {
        Objects.requireNonNull(order, "order");

        orderService.setOrderStatus(order.getId(), OrderStatus.PROCESSING);

        OrderResponseDto orderResponse = orderMapper.toResponse(order);
        return new CreateOrderResponseDto(orderResponse, "Order accepted. Payment upon receipt.");
    }

}
