package com.challengeteam.shop.persistence.repository;

import com.challengeteam.shop.entity.phone.Phone;
import com.challengeteam.shop.entity.phone.PhoneColor;
import com.challengeteam.shop.entity.phone.StorageCapacity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
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

    List<Phone> findTop4ByOrderByCreatedAtDescIdDesc();

    @Query("""
            SELECT p FROM Phone p
            WHERE (:brand IS NULL OR p.brand = :brand)
              AND (:minPrice IS NULL OR p.price >= :minPrice)
              AND (:maxPrice IS NULL OR p.price <= :maxPrice)
            ORDER BY (
                SELECT COALESCE(SUM(oi.quantity), 0)
                FROM OrderItem oi
                WHERE oi.phone = p
            ) + (
                SELECT COUNT(f)
                FROM Favorite f
                WHERE f.phone = p
            ) DESC,
            p.name ASC,
            p.id ASC
            """)
    Page<Phone> findAllByCatalogPopularity(
            @Param("brand") String brand,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable);

    @Query("""
            SELECT p FROM Phone p
            WHERE (:brandsEmpty = true OR p.brand IN :brands)
              AND (:minPrice IS NULL OR p.price >= :minPrice)
              AND (:maxPrice IS NULL OR p.price <= :maxPrice)
              AND (:inStock = false OR p.stock > 0)
              AND (:preOrder = false OR p.stock = 0)
              AND (:colorsEmpty = true OR EXISTS (
                    SELECT 1
                    FROM Phone colorPhone
                    JOIN colorPhone.phoneCharacteristics.phoneColors color
                    WHERE colorPhone = p AND color IN :colors
              ))
              AND (:storageEmpty = true OR EXISTS (
                    SELECT 1
                    FROM Phone storagePhone
                    JOIN storagePhone.phoneCharacteristics.storageCapacities storage
                    WHERE storagePhone = p AND storage IN :storageCapacities
              ))
            ORDER BY (
                SELECT COALESCE(SUM(oi.quantity), 0)
                FROM OrderItem oi
                WHERE oi.phone = p
            ) + (
                SELECT COUNT(f)
                FROM Favorite f
                WHERE f.phone = p
            ) DESC,
            p.name ASC,
            p.id ASC
            """)
    Page<Phone> findAllByCatalogPopularity(
            @Param("brands") List<String> brands,
            @Param("brandsEmpty") boolean brandsEmpty,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("inStock") boolean inStock,
            @Param("preOrder") boolean preOrder,
            @Param("colors") List<PhoneColor> colors,
            @Param("colorsEmpty") boolean colorsEmpty,
            @Param("storageCapacities") List<StorageCapacity> storageCapacities,
            @Param("storageEmpty") boolean storageEmpty,
            Pageable pageable);

    @Query("""
            SELECT DISTINCT p FROM Phone p
            WHERE (:brandsEmpty = true OR p.brand IN :brands)
              AND (:minPrice IS NULL OR p.price >= :minPrice)
              AND (:maxPrice IS NULL OR p.price <= :maxPrice)
              AND (:inStock = false OR p.stock > 0)
              AND (:preOrder = false OR p.stock = 0)
              AND (:colorsEmpty = true OR EXISTS (
                    SELECT 1
                    FROM Phone colorPhone
                    JOIN colorPhone.phoneCharacteristics.phoneColors color
                    WHERE colorPhone = p AND color IN :colors
              ))
              AND (:storageEmpty = true OR EXISTS (
                    SELECT 1
                    FROM Phone storagePhone
                    JOIN storagePhone.phoneCharacteristics.storageCapacities storage
                    WHERE storagePhone = p AND storage IN :storageCapacities
              ))
            """)
    Page<Phone> findAllByCatalogFilters(
            @Param("brands") List<String> brands,
            @Param("brandsEmpty") boolean brandsEmpty,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("inStock") boolean inStock,
            @Param("preOrder") boolean preOrder,
            @Param("colors") List<PhoneColor> colors,
            @Param("colorsEmpty") boolean colorsEmpty,
            @Param("storageCapacities") List<StorageCapacity> storageCapacities,
            @Param("storageEmpty") boolean storageEmpty,
            Pageable pageable);

    @Query("SELECT DISTINCT p.brand FROM Phone p ORDER BY p.brand ASC")
    List<String> findDistinctBrands();

    @Query("SELECT DISTINCT color FROM Phone p JOIN p.phoneCharacteristics.phoneColors color ORDER BY color ASC")
    List<PhoneColor> findDistinctColors();

    @Query("SELECT DISTINCT storage FROM Phone p JOIN p.phoneCharacteristics.storageCapacities storage ORDER BY storage ASC")
    List<StorageCapacity> findDistinctStorageCapacities();

    @Query("SELECT MIN(p.price) FROM Phone p")
    Optional<BigDecimal> findCatalogMinPrice();

    @Query("SELECT MAX(p.price) FROM Phone p")
    Optional<BigDecimal> findCatalogMaxPrice();

    @Query("SELECT p FROM Phone p WHERE p.stock <= :threshold ORDER BY p.stock ASC, p.name ASC")
    List<Phone> findLowStockProducts(@Param("threshold") int threshold, org.springframework.data.domain.Pageable pageable);

    long countByStockLessThanEqual(Integer threshold);

    @Query("SELECT COALESCE(SUM(p.stock), 0) FROM Phone p")
    Long sumStock();

}
