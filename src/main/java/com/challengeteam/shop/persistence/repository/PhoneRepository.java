package com.challengeteam.shop.persistence.repository;

import com.challengeteam.shop.entity.phone.Phone;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PhoneRepository extends JpaRepository<Phone, Long>, JpaSpecificationExecutor<Phone> {

    boolean existsBySku(String sku);

    boolean existsBySkuAndIdNot(String sku, Long id);

    @Query(value = """
            SELECT EXISTS(
                  SELECT 1
                  FROM images AS i
                  WHERE i.id = :imageId AND i.fk_phone_id = :phoneId
            );
            """, nativeQuery = true)
    boolean existsPhoneByIdWithImage(@Param("phoneId") long phoneId, @Param("imageId") long imageId);

    @Query("SELECT p FROM Phone p LEFT JOIN FETCH p.images WHERE p.id = :id")
    Optional<Phone> findByIdWithImages(@Param("id") Long id);

    @Query("SELECT p FROM Phone p LEFT JOIN FETCH p.images WHERE p IN :phones")
    List<Phone> findAllWithImages(@Param("phones") List<Phone> phones);

    @Query("SELECT p FROM Phone p WHERE p.stock <= :threshold ORDER BY p.stock ASC, p.name ASC")
    List<Phone> findLowStockProducts(@Param("threshold") int threshold, org.springframework.data.domain.Pageable pageable);

    long countByStockLessThanEqual(Integer threshold);

}
