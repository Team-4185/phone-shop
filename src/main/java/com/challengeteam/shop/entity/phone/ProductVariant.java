package com.challengeteam.shop.entity.phone;

import com.challengeteam.shop.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Sellable phone configuration with its own SKU, price, stock, and derived status.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(
    name = "product_variants",
    uniqueConstraints = {
      @UniqueConstraint(name = "uk_product_variants_sku", columnNames = "sku"),
      @UniqueConstraint(
          name = "uk_product_variants_phone_color_storage",
          columnNames = {"fk_phone_id", "color", "storage_capacity"})
    })
public class ProductVariant extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(nullable = false, name = "fk_phone_id")
  private Phone phone;

  @Column(nullable = false, length = 64)
  private String sku;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private PhoneColor color;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, name = "storage_capacity", length = 30)
  private StorageCapacity storageCapacity;

  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal price;

  @Column(nullable = false)
  private Integer stock;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private ProductStatus status;
}
