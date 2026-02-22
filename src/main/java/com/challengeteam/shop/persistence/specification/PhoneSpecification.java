package com.challengeteam.shop.persistence.specification;

import com.challengeteam.shop.dto.pagination.PhoneFilterDto;
import com.challengeteam.shop.entity.phone.Phone;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class PhoneSpecification {

    public static Specification<Phone> hasBrand(String brand) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        criteriaBuilder.lower(root.get("brand")),
                        brand.toLowerCase()
                );
    }

    public static Specification<Phone> priceGreaterThanOrEqual(BigDecimal minPrice) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice);
    }

    public static Specification<Phone> priceLessThanOrEqual(BigDecimal maxPrice) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice);
    }

    public static Specification<Phone> build(PhoneFilterDto requestDto) {
        Specification<Phone> spec = (root, query, cb) -> cb.conjunction();

        if (requestDto.brand() != null && !requestDto.brand().isBlank()) {
            spec = spec.and(PhoneSpecification.hasBrand(requestDto.brand()));
        }

        if (requestDto.minPrice() != null) {
            spec = spec.and(PhoneSpecification.priceGreaterThanOrEqual(requestDto.minPrice()));
        }

        if (requestDto.maxPrice() != null) {
            spec = spec.and(PhoneSpecification.priceLessThanOrEqual(requestDto.maxPrice()));
        }

        return spec;
    }

}
