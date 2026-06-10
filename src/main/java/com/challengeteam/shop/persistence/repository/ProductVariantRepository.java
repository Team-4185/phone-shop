package com.challengeteam.shop.persistence.repository;

import com.challengeteam.shop.entity.phone.PhoneColor;
import com.challengeteam.shop.entity.phone.ProductVariant;
import com.challengeteam.shop.entity.phone.StorageCapacity;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

  boolean existsBySku(String sku);

  boolean existsBySkuAndIdNot(String sku, Long id);

  boolean existsByPhoneIdAndColorAndStorageCapacity(
      Long phoneId, PhoneColor color, StorageCapacity storageCapacity);

  boolean existsByPhoneIdAndColorAndStorageCapacityAndIdNot(
      Long phoneId, PhoneColor color, StorageCapacity storageCapacity, Long id);

  List<ProductVariant> findAllByPhoneIdOrderByPriceAscIdAsc(Long phoneId);

  List<ProductVariant> findAllByPhoneIdIn(Collection<Long> phoneIds);

  Optional<ProductVariant> findByPhoneIdAndColorAndStorageCapacity(
      Long phoneId, PhoneColor color, StorageCapacity storageCapacity);

  @Query("""
      SELECT v FROM ProductVariant v
      JOIN FETCH v.phone p
      WHERE v.id IN :ids
      """)
  List<ProductVariant> findAllByIdWithPhone(@Param("ids") Collection<Long> ids);

  @Query("SELECT MIN(v.price) FROM ProductVariant v")
  Optional<BigDecimal> findCatalogMinPrice();

  @Query("SELECT MAX(v.price) FROM ProductVariant v")
  Optional<BigDecimal> findCatalogMaxPrice();

  @Query("SELECT DISTINCT v.color FROM ProductVariant v ORDER BY v.color ASC")
  List<PhoneColor> findDistinctColors();

  @Query("SELECT DISTINCT v.storageCapacity FROM ProductVariant v ORDER BY v.storageCapacity ASC")
  List<StorageCapacity> findDistinctStorageCapacities();

  @Query("SELECT COALESCE(SUM(v.stock), 0) FROM ProductVariant v")
  Long sumStock();

  long countByStockLessThanEqual(Integer threshold);
}
