package com.challengeteam.shop.persistence.repository;

import com.challengeteam.shop.entity.review.ProductReview;
import com.challengeteam.shop.persistence.repository.projection.PhoneReviewSummaryProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductReviewRepository extends JpaRepository<ProductReview, Long> {

    boolean existsByPhoneIdAndUserId(Long phoneId, Long userId);

    @EntityGraph(attributePaths = {"user", "phone"})
    Page<ProductReview> findAllByPhoneId(Long phoneId, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "phone"})
    Optional<ProductReview> findByIdAndUserId(Long id, Long userId);

    @Query("""
            SELECT r.phone.id AS phoneId,
                   AVG(r.rating) AS averageRating,
                   COUNT(r) AS reviewsCount
            FROM ProductReview r
            WHERE r.phone.id IN :phoneIds
            GROUP BY r.phone.id
            """)
    List<PhoneReviewSummaryProjection> findSummariesByPhoneIds(@Param("phoneIds") Collection<Long> phoneIds);
}
