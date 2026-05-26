package com.challengeteam.shop.persistence.repository;

import com.challengeteam.shop.entity.order.Order;
import com.challengeteam.shop.entity.order.OrderStatus;
import com.challengeteam.shop.entity.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
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

    @Query("SELECT o FROM CustomerOrder o WHERE o.user IN :users")
    List<Order> findAllByUsers(@Param("users") List<User> users);

    @Query("SELECT o.status, COUNT(o) FROM CustomerOrder o GROUP BY o.status")
    List<Object[]> countOrdersByStatus();

    @Query("SELECT o FROM CustomerOrder o WHERE o.user.id = :userId ORDER BY o.createdAt DESC")
    List<Order> findRecentByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(o.total), 0) FROM CustomerOrder o")
    java.math.BigDecimal sumTotalRevenue();

    @Query("SELECT COALESCE(SUM(o.total), 0) FROM CustomerOrder o WHERE o.createdAt >= :start AND o.createdAt < :end")
    java.math.BigDecimal sumRevenueBetween(@Param("start") Instant start, @Param("end") Instant end);

    long countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(Instant start, Instant end);

    long countByStatus(OrderStatus status);

    @Query(
            value =
                    """
                    SELECT CAST(o.created_at AS DATE) AS sale_date,
                           COALESCE(SUM(o.total), 0) AS revenue,
                           COUNT(o.id) AS orders_count
                    FROM orders o
                    GROUP BY CAST(o.created_at AS DATE)
                    ORDER BY sale_date
                    """,
            nativeQuery = true)
    List<Object[]> aggregateSalesByDate();

    @Query(
            value =
                    """
                    SELECT CAST(o.created_at AS DATE) AS sale_date,
                           COALESCE(SUM(o.total), 0) AS revenue,
                           COUNT(o.id) AS sales_count
                    FROM orders o
                    WHERE o.created_at >= :start AND o.created_at < :end
                    GROUP BY CAST(o.created_at AS DATE)
                    ORDER BY sale_date
                    """,
            nativeQuery = true)
    List<Object[]> aggregateSalesByDateBetween(@Param("start") Instant start, @Param("end") Instant end);

    @Query(
            value =
                    """
                    SELECT CAST(DATE_TRUNC('month', o.created_at) AS DATE) AS sale_month,
                           COALESCE(SUM(o.total), 0) AS revenue,
                           COUNT(o.id) AS sales_count
                    FROM orders o
                    WHERE o.created_at >= :start AND o.created_at < :end
                    GROUP BY DATE_TRUNC('month', o.created_at)
                    ORDER BY sale_month
                    """,
            nativeQuery = true)
    List<Object[]> aggregateSalesByMonthBetween(@Param("start") Instant start, @Param("end") Instant end);

    @Query(
            value =
                    """
                    SELECT COALESCE(p.brand, 'Unknown') AS brand,
                           COALESCE(SUM(oi.total_price), 0) AS revenue,
                           COALESCE(SUM(oi.quantity), 0) AS units_sold
                    FROM orders_items oi
                    LEFT JOIN phones p ON p.id = oi.fk_phone_id
                    GROUP BY COALESCE(p.brand, 'Unknown')
                    ORDER BY revenue DESC
                    """,
            nativeQuery = true)
    List<Object[]> aggregateSalesByBrand();

    @Query(
            value =
                    """
                    SELECT oi.fk_phone_id AS phone_id,
                           MIN(oi.product_name) AS name,
                           oi.sku AS sku,
                           COALESCE(SUM(oi.quantity), 0) AS units_sold,
                           COALESCE(SUM(oi.total_price), 0) AS revenue,
                           MIN(p.stock) AS stock,
                           MIN(p.status) AS status
                    FROM orders_items oi
                    LEFT JOIN phones p ON p.id = oi.fk_phone_id
                    GROUP BY oi.fk_phone_id, oi.sku
                    ORDER BY units_sold DESC, revenue DESC
                    LIMIT :limit
                    """,
            nativeQuery = true)
    List<Object[]> findTopSellingProducts(@Param("limit") int limit);

    @Query("SELECT o FROM CustomerOrder o LEFT JOIN FETCH o.user ORDER BY o.createdAt DESC")
    List<Order> findRecentOrders(Pageable pageable);
}
