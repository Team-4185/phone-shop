package com.challengeteam.shop.persistence.repository;

import com.challengeteam.shop.entity.order.OrderItem;
import com.challengeteam.shop.entity.order.OrderItemId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, OrderItemId> {
}
