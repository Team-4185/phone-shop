package com.challengeteam.shop.persistence.repository;

import com.challengeteam.shop.entity.order.Order;
import com.challengeteam.shop.entity.user.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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

  @Query("SELECT o FROM CustomerOrder o WHERE o.user IN :users")
  List<Order> findAllByUsers(@Param("users") List<User> users);

  @Query("SELECT o FROM CustomerOrder o WHERE o.user.id = :userId ORDER BY o.createdAt DESC")
  List<Order> findRecentByUserId(@Param("userId") Long userId, Pageable pageable);

  @Query("SELECT COALESCE(SUM(o.total), 0) FROM CustomerOrder o")
  java.math.BigDecimal sumTotalRevenue();

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
                 COALESCE(SUM(oi.total_price), 0) AS revenue
          FROM orders_items oi
          GROUP BY oi.fk_phone_id, oi.sku
          ORDER BY units_sold DESC, revenue DESC
          LIMIT :limit
          """,
      nativeQuery = true)
  List<Object[]> findTopSellingProducts(@Param("limit") int limit);

  @Query("SELECT o FROM CustomerOrder o LEFT JOIN FETCH o.user ORDER BY o.createdAt DESC")
  List<Order> findRecentOrders(Pageable pageable);
}
