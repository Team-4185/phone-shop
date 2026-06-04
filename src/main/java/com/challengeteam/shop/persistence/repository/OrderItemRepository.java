package com.challengeteam.shop.persistence.repository;

import com.challengeteam.shop.entity.order.OrderItem;
import com.challengeteam.shop.persistence.repository.projection.PhoneLastPurchaseProjection;
import com.challengeteam.shop.persistence.repository.projection.PhonePurchaseQuantityProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("""
            SELECT oi.phone.id AS phoneId, COALESCE(SUM(oi.quantity), 0) AS quantity
            FROM OrderItem oi
            WHERE oi.phone.id IN :phoneIds
              AND oi.order.createdAt >= :start
              AND oi.order.createdAt < :end
            GROUP BY oi.phone.id
            """)
    List<PhonePurchaseQuantityProjection> sumPurchasedQuantitiesByPhoneIdsBetween(
            @Param("phoneIds") Collection<Long> phoneIds,
            @Param("start") Instant start,
            @Param("end") Instant end);

    @Query("""
            SELECT oi.phone.id AS phoneId, MAX(oi.order.createdAt) AS lastPurchasedAt
            FROM OrderItem oi
            WHERE oi.phone.id IN :phoneIds
            GROUP BY oi.phone.id
            """)
    List<PhoneLastPurchaseProjection> findLastPurchaseByPhoneIds(@Param("phoneIds") Collection<Long> phoneIds);
}
