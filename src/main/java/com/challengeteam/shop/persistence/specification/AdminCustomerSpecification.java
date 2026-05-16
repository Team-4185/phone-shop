package com.challengeteam.shop.persistence.specification;

import com.challengeteam.shop.dto.admin.customer.AdminCustomerFilterDto;
import com.challengeteam.shop.entity.user.User;
import java.util.function.Function;
import org.springframework.data.jpa.domain.Specification;

/** Builds JPA criteria used by admin customer list filters. */
public final class AdminCustomerSpecification {

  private AdminCustomerSpecification() {}

  public static Specification<User> build(AdminCustomerFilterDto filterDto) {
    Specification<User> spec = (root, query, cb) -> cb.equal(root.get("role").get("name"), "USER");

    spec = addIfHasText(spec, filterDto.search(), AdminCustomerSpecification::matchesSearch);

    return spec;
  }

  private static Specification<User> matchesSearch(String search) {
    return (root, query, cb) -> {
      String pattern = "%" + search.toLowerCase() + "%";

      return cb.or(
          cb.like(cb.lower(root.get("email")), pattern),
          cb.like(cb.lower(root.get("firstName")), pattern),
          cb.like(cb.lower(root.get("lastName")), pattern),
          cb.like(cb.lower(root.get("phoneNumber")), pattern),
          cb.like(cb.lower(root.get("city")), pattern));
    };
  }

  private static Specification<User> addIfHasText(
      Specification<User> spec,
      String value,
      Function<String, Specification<User>> specificationFactory) {
    return value == null || value.isBlank() ? spec : spec.and(specificationFactory.apply(value));
  }
}
