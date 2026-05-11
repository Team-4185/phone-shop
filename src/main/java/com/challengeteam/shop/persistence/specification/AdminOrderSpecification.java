package com.challengeteam.shop.persistence.specification;

import com.challengeteam.shop.dto.admin.order.AdminOrderFilterDto;
import com.challengeteam.shop.entity.order.Order;
import com.challengeteam.shop.entity.order.OrderItem;
import com.challengeteam.shop.entity.order.OrderStatus;
import com.challengeteam.shop.entity.user.User;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import java.math.BigDecimal;
import java.util.function.Function;
import org.springframework.data.jpa.domain.Specification;

/** Builds JPA criteria used by admin order list filters. */
public final class AdminOrderSpecification {

  private AdminOrderSpecification() {}

  public static Specification<Order> build(AdminOrderFilterDto filterDto) {
    Specification<Order> spec = (root, query, cb) -> cb.conjunction();

    spec = addIfHasText(spec, filterDto.search(), AdminOrderSpecification::matchesSearch);
    spec = addIfPresent(spec, filterDto.status(), AdminOrderSpecification::hasStatus);
    spec = addIfPresent(spec, filterDto.minTotal(), AdminOrderSpecification::totalGreaterOrEqual);
    spec = addIfPresent(spec, filterDto.maxTotal(), AdminOrderSpecification::totalLessOrEqual);

    return spec;
  }

  private static Specification<Order> matchesSearch(String search) {
    return (root, query, cb) -> {
      query.distinct(true);
      String pattern = "%" + search.toLowerCase() + "%";
      Join<Order, User> user = root.join("user", JoinType.LEFT);
      Join<Order, OrderItem> items = root.join("items", JoinType.LEFT);

      return cb.or(
          cb.like(cb.lower(root.get("customerEmail")), pattern),
          cb.like(cb.lower(user.get("email")), pattern),
          cb.like(cb.lower(items.get("sku")), pattern),
          cb.like(cb.lower(items.get("productName")), pattern),
          cb.like(root.get("id").as(String.class), pattern));
    };
  }

  private static Specification<Order> hasStatus(OrderStatus status) {
    return (root, query, cb) -> cb.equal(root.get("status"), status);
  }

  private static Specification<Order> totalGreaterOrEqual(BigDecimal minTotal) {
    return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("total"), minTotal);
  }

  private static Specification<Order> totalLessOrEqual(BigDecimal maxTotal) {
    return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("total"), maxTotal);
  }

  private static <T> Specification<Order> addIfPresent(
      Specification<Order> spec, T value, Function<T, Specification<Order>> specificationFactory) {
    return value == null ? spec : spec.and(specificationFactory.apply(value));
  }

  private static Specification<Order> addIfHasText(
      Specification<Order> spec,
      String value,
      Function<String, Specification<Order>> specificationFactory) {
    return value == null || value.isBlank() ? spec : spec.and(specificationFactory.apply(value));
  }
}
