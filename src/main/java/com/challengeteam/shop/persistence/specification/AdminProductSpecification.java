package com.challengeteam.shop.persistence.specification;

import com.challengeteam.shop.dto.admin.product.AdminProductFilterDto;
import com.challengeteam.shop.entity.phone.Phone;
import com.challengeteam.shop.entity.phone.ProductStatus;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.function.Function;

public final class AdminProductSpecification {

  private AdminProductSpecification() {}

  public static Specification<Phone> build(AdminProductFilterDto filterDto) {
    Specification<Phone> spec = (root, query, cb) -> cb.conjunction();

    spec = addIfHasText(spec, filterDto.search(), AdminProductSpecification::matchesSearch);
    spec = addIfHasText(spec, filterDto.brand(), AdminProductSpecification::hasBrand);
    spec = addIfPresent(spec, filterDto.status(), AdminProductSpecification::hasStatus);
    spec =
        addIfPresent(
            spec, filterDto.minPrice(), AdminProductSpecification::priceGreaterThanOrEqual);
    spec =
        addIfPresent(spec, filterDto.maxPrice(), AdminProductSpecification::priceLessThanOrEqual);

    return spec;
  }

  private static Specification<Phone> matchesSearch(String search) {
    return (root, query, cb) -> {
      String pattern = "%" + search.toLowerCase() + "%";

      return cb.or(
          cb.like(cb.lower(root.get("name")), pattern),
          cb.like(cb.lower(root.get("sku")), pattern),
          cb.like(cb.lower(root.get("brandName")), pattern),
          cb.like(cb.lower(root.get("description")), pattern));
    };
  }

  private static Specification<Phone> hasBrand(String brand) {
    return (root, query, cb) -> cb.equal(cb.lower(root.get("brandName")), brand.toLowerCase());
  }

  private static Specification<Phone> hasStatus(ProductStatus status) {
    return (root, query, cb) -> cb.equal(root.get("status"), status);
  }

  private static Specification<Phone> priceGreaterThanOrEqual(BigDecimal minPrice) {
    return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("price"), minPrice);
  }

  private static Specification<Phone> priceLessThanOrEqual(BigDecimal maxPrice) {
    return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("price"), maxPrice);
  }

  private static <T> Specification<Phone> addIfPresent(
      Specification<Phone> spec, T value, Function<T, Specification<Phone>> specificationFactory) {
    return value == null ? spec : spec.and(specificationFactory.apply(value));
  }

  private static Specification<Phone> addIfHasText(
      Specification<Phone> spec,
      String value,
      Function<String, Specification<Phone>> specificationFactory) {
    return value == null || value.isBlank() ? spec : spec.and(specificationFactory.apply(value));
  }
}
