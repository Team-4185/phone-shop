package com.challengeteam.shop.service;

import com.challengeteam.shop.dto.order.CreateOrderDto;
import com.challengeteam.shop.entity.order.Order;
import com.challengeteam.shop.entity.order.OrderStatus;
import com.challengeteam.shop.entity.phone.Phone;
import com.challengeteam.shop.entity.user.User;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface OrderService {

    Page<Order> getOrders(int page, int size);
    Optional<Order> getById(long id);
    List<Order> getOrdersByUserId(Long userId);
    Order create(User user, Map<Phone, Integer> products, CreateOrderDto createOrderDto);
    Order setOrderStatus(Long id, OrderStatus status);
    Order makeOrderPaid(Long id, String paymentIntentId);
    Order makeOrderFailed(Long id);
    Order setProcessedByWebhook(Long id, boolean value);
    Order setCheckoutUrl(Long id, String url);

}
