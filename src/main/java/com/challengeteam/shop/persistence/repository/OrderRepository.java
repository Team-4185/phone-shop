package com.challengeteam.shop.persistence.repository;

import com.challengeteam.shop.entity.order.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query(value = """
        SELECT o FROM Order o
        LEFT JOIN FETCH o.orderItems i
        LEFT JOIN FETCH i.phone p
        WHERE o IN :orders
    """)
    List<Order> attachOrderDetails(@Param("orders") List<Order> orders);

    default Page<Order> fetchAll(Pageable pageable) {
        // here two queries to repository: the first get page of orders, the second fill up order details
        // this is to avoid the n+1 problem
        Page<Order> page = findAll(pageable);
        List<Order> fullOrders = attachOrderDetails(page.toList());

        return new PageImpl<>(fullOrders, pageable, page.getTotalElements());
    }

    @Query(value = """
        SELECT o FROM Order o
        LEFT JOIN FETCH o.orderItems i
        LEFT JOIN FETCH i.phone p
        WHERE o.id = :id
    """)
    Optional<Order> fetchById(@Param("id") Long id);

    List<Order> getAllByUser_Id(Long userId);

    default List<Order> fetchByUserId(Long userId) {
        List<Order> orders = getAllByUser_Id(userId);
        return attachOrderDetails(orders);
    }

}
