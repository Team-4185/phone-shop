package com.challengeteam.shop.persistence.specification;

import com.challengeteam.shop.dto.admin.product.AdminProductFilterDto;
import com.challengeteam.shop.entity.phone.Phone;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.function.Function;

public final class AdminProductSpecification {

  private AdminProductSpecification() {}

  public static Specification<Phone> build(AdminProductFilterDto filterDto) {
    Specification<Phone> spec = fetchImages();

    spec = addIfHasText(spec, filterDto.search(), AdminProductSpecification::matchesSearch);
    spec = addIfHasText(spec, filterDto.brand(), AdminProductSpecification::hasBrand);
    spec =
        addIfPresent(
            spec, filterDto.minPrice(), AdminProductSpecification::priceGreaterThanOrEqual);
    spec =
        addIfPresent(spec, filterDto.maxPrice(), AdminProductSpecification::priceLessThanOrEqual);

    return spec;
  }

  private static Specification<Phone> fetchImages() {
    return (root, query, cb) -> {
      Objects.requireNonNull(query);

      if (query.getResultType() != Long.class && query.getResultType() != long.class) {
        root.fetch("images", JoinType.LEFT);
        query.distinct(true);
      }

      return cb.conjunction();
    };
  }

  private static Specification<Phone> matchesSearch(String search) {
    return (root, query, cb) -> {
      String pattern = "%" + search.toLowerCase() + "%";

      return cb.or(
          cb.like(cb.lower(root.get("name")), pattern),
          cb.like(cb.lower(root.get("brand")), pattern),
          cb.like(cb.lower(root.get("description")), pattern));
    };
  }

  private static Specification<Phone> hasBrand(String brand) {
    return (root, query, cb) -> cb.equal(cb.lower(root.get("brand")), brand.toLowerCase());
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
