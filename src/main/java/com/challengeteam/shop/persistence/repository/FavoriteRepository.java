package com.challengeteam.shop.persistence.repository;

import com.challengeteam.shop.entity.favorite.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    boolean existsByUserIdAndPhoneId(Long userId, Long phoneId);

    Optional<Favorite> findByUserIdAndPhoneId(Long userId, Long phoneId);

    @Query("""
            SELECT DISTINCT f FROM Favorite f
            LEFT JOIN FETCH f.phone p
            LEFT JOIN FETCH p.images
            WHERE f.user.id = :userId
            ORDER BY f.createdAt DESC
            """)
    List<Favorite> findAllByUserIdWithPhoneImages(@Param("userId") Long userId);
}
