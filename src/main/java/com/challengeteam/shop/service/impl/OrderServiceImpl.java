package com.challengeteam.shop.service.impl;

import com.challengeteam.shop.dto.order.CreateOrderDto;
import com.challengeteam.shop.entity.order.*;
import com.challengeteam.shop.entity.phone.Phone;
import com.challengeteam.shop.entity.user.User;
import com.challengeteam.shop.exceptionHandling.exception.ResourceNotFoundException;
import com.challengeteam.shop.persistence.repository.OrderItemRepository;
import com.challengeteam.shop.persistence.repository.OrderRepository;
import com.challengeteam.shop.persistence.repository.UserRepository;
import com.challengeteam.shop.service.OrderService;
import com.challengeteam.shop.service.PriceService;
import com.challengeteam.shop.service.UserService;
import com.challengeteam.shop.service.impl.validator.OrderValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderValidator orderValidator;
    private final PriceService priceService;
    private final OrderItemRepository orderItemRepository;

    @Transactional(readOnly = true)
    @Override
    public Page<Order> getOrders(int page, int size) {
        if (page < 0 || size < 0) {
            throw new IllegalArgumentException("Required positive values for 'page' and 'size' parameters");
        }

        log.debug("Get page of orders with page number:{} and size number:{}", page, size);
        return orderRepository.fetchAll(PageRequest.of(page, size));
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Order> getById(long id) {
        if (id < 0) {
            throw new IllegalArgumentException("Required positive value for 'id'");
        }

        log.debug("Get order by id: {}", id);
        return orderRepository.fetchById(id);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Order> getOrdersByUserId(Long userId) {
        Objects.requireNonNull(userId, "userId");

        List<Order> orders = orderRepository.fetchByUserId(userId);
        log.debug("Get {} amount of orders by user id: {}", orders.size(), userId);
        return orders;
    }

    @Transactional
    @Override
    public Order create(User user, Map<Phone, Integer> products, CreateOrderDto createOrderDto) {
        Objects.requireNonNull(user, "user");
        Objects.requireNonNull(products, "products");
        Objects.requireNonNull(createOrderDto, "createOrderDto");

        // validate
        orderValidator.validateProducts(products);
        orderValidator.validateUser(user);
        orderValidator.validateDestination(createOrderDto.destination());
        orderValidator.validateRecipient(createOrderDto.recipient());
        orderValidator.validatePaymentDetail(createOrderDto.paymentDetail());

        // order
        Order order = assembleOrder(user, products, createOrderDto);
        order = orderRepository.save(order);

        // items
        Set<OrderItem> orderItems = new HashSet<>();
        for (Map.Entry<Phone, Integer> entry : products.entrySet()) {
            Phone phone = entry.getKey();
            Integer amount = entry.getValue();
            var orderItem = new OrderItem(order, phone, amount);
            orderItems.add(orderItem);
        }

        // complete order
        orderItems = new HashSet<>(orderItemRepository.saveAll(orderItems));
        order.setOrderItems(orderItems);

        log.debug("Created new order with id: {} for user with id: {}", order.getId(), user.getId());
        return order;
    }

    private Order assembleOrder(User user, Map<Phone, Integer> products, CreateOrderDto createOrderDto) {
        BigDecimal totalPrice = priceService.calculateTotalPrice(products);
        String destination = createOrderDto.destination().address();
        var recipient = Recipient.builder()
                .email(createOrderDto.recipient().email())
                .phone(createOrderDto.recipient().phone())
                .firstname(createOrderDto.recipient().firstname())
                .lastname(createOrderDto.recipient().lastname())
                .build();
        var paymentDetail = PaymentDetail.builder()
                .paymentMethod(createOrderDto.paymentDetail().paymentMethod())
                .paid(false)
                .build();

        return Order.builder()
                .user(user)
                .totalPrice(totalPrice)
                .destination(destination)
                .recipient(recipient)
                .paymentDetail(paymentDetail)
                .status(OrderStatus.PENDING)
                .processedByWebhook(false)
                .build();
    }

    @Transactional
    @Override
    public Order setOrderStatus(Long id, OrderStatus status) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(status, "status");

        Order order = orderRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not found order with id " + id));

        order.setStatus(status);
        order = orderRepository.save(order);
        log.debug("Updated status for order with id: {}. Current value is: {}", id, status);

        return order;
    }

    @Transactional
    @Override
    public Order makeOrderPaid(Long id, String paymentIntentId) {
        Objects.requireNonNull(id, "id");

        Order order = orderRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not found order with id: " + id));

        order.setStatus(OrderStatus.PROCESSING);
        order.getPaymentDetail().setPaid(true);
        order.getPaymentDetail().setIntentId(paymentIntentId);
        order = orderRepository.save(order);
        log.debug("Made order with id: {} paid. PaymentIntentId is '{}'", id, paymentIntentId);

        return order;

    }

    @Transactional
    @Override
    public Order makeOrderFailed(Long id) {
        Objects.requireNonNull(id, "id");

        Order order = orderRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not found order with id: " + id));
        order.setStatus(OrderStatus.FAILED);
        order = orderRepository.save(order);
        log.debug("Made order with id: {} failed.", id);

        return order;
    }

    @Transactional
    @Override
    public Order setProcessedByWebhook(Long id, boolean value) {
        Objects.requireNonNull(id, "id");

        Order order = orderRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not found order with id " + id));
        order.setProcessedByWebhook(value);
        order = orderRepository.save(order);
        log.debug("Updated processedByWebhook for order with id: {}. Current value is: {}", id, value);

        return order;
    }

    @Transactional
    @Override
    public Order setCheckoutUrl(Long id, String url) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(url, "url");

        Order order = orderRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not found order with id " + id));
        order.getPaymentDetail().setCheckoutUrl(url);
        order = orderRepository.save(order);
        log.debug("Updated checkoutUrl for order with id: {}. ", id);

        return order;
    }

}
