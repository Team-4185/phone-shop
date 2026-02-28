package com.challengeteam.shop.service.impl;

import com.challengeteam.shop.dto.order.CreateOrderDto;
import com.challengeteam.shop.dto.order.CreateOrderResponseDto;
import com.challengeteam.shop.entity.cart.Cart;
import com.challengeteam.shop.entity.order.Order;
import com.challengeteam.shop.entity.order.PaymentMethod;
import com.challengeteam.shop.entity.phone.Phone;
import com.challengeteam.shop.entity.user.User;
import com.challengeteam.shop.exceptionHandling.exception.CriticalSystemException;
import com.challengeteam.shop.exceptionHandling.exception.ResourceNotFoundException;
import com.challengeteam.shop.exceptionHandling.exception.UnsupportedPaymentMethodException;
import com.challengeteam.shop.mapper.CartItemMapper;
import com.challengeteam.shop.service.OrderService;
import com.challengeteam.shop.service.UserCartService;
import com.challengeteam.shop.service.UserOrderService;
import com.challengeteam.shop.service.UserService;
import com.challengeteam.shop.service.impl.strategy.payment.OrderPaymentStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserOrderServiceImpl implements UserOrderService {
    private final OrderService orderService;
    private final UserService userService;
    private final UserCartService userCartService;
    private final CartItemMapper cartItemMapper;
    private final List<OrderPaymentStrategy> paymentStrategies;

    @Transactional
    @Override
    public CreateOrderResponseDto executeOrderWorkflow(Long userId, CreateOrderDto createOrderDto) {
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(createOrderDto, "createOrderDto");

        User user = fetchUser(userId);
        Cart cart = fetchCart(userId);
        Map<Phone, Integer> products = cartItemMapper.productsAsMap(cart.getCartItems());
        Order order = orderService.create(user, products, createOrderDto);
        userCartService.clearUserCart(userId);

        PaymentMethod paymentMethod = createOrderDto.paymentDetail().paymentMethod();
        OrderPaymentStrategy paymentStrategy = paymentStrategies.stream()
                .filter(s -> s.getSupportedMethod().equals(paymentMethod))
                .findFirst()
                .orElseThrow(() -> new UnsupportedPaymentMethodException(paymentMethod));

        log.debug("Created order for user with id: {}. Was chosen order payment strategy " +
                  "supported '{}' payment method", userId, paymentStrategy.getSupportedMethod());
        return paymentStrategy.processPayment(order);
    }

    private Cart fetchCart(Long userId) {
        return userCartService
                .getUserCart(userId)
                .orElseThrow(() -> new CriticalSystemException("User with id " + userId + " doesn't have cart"));
    }

    private User fetchUser(Long userId) {
        return userService
                .getById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Not found user with id " + userId));
    }

}
