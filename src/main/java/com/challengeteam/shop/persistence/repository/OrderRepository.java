package com.challengeteam.shop.persistence.repository;

import com.challengeteam.shop.entity.order.Order;
import com.challengeteam.shop.entity.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    @Query(
            "SELECT DISTINCT o FROM CustomerOrder o "
                    + "LEFT JOIN FETCH o.user "
                    + "LEFT JOIN FETCH o.items i "
                    + "LEFT JOIN FETCH i.phone "
                    + "WHERE o.id = :id")
    Optional<Order> findByIdWithDetails(@Param("id") Long id);

    @Query(
            "SELECT DISTINCT o FROM CustomerOrder o "
                    + "LEFT JOIN FETCH o.user "
                    + "LEFT JOIN FETCH o.items i "
                    + "LEFT JOIN FETCH i.phone "
                    + "WHERE o IN :orders")
    List<Order> findAllWithDetails(@Param("orders") List<Order> orders);

    @EntityGraph(value = "Order.withItems")
    @Query("SELECT o FROM CustomerOrder o WHERE o.id = :id")
    Optional<Order> findByIdWithItems(@Param("id") Long id);

    @EntityGraph(value = "Order.withItems")
    Page<Order> findByUser(User user, Pageable pageable);
}
