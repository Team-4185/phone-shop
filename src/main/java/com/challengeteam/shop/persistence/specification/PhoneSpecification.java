package com.challengeteam.shop.persistence.specification;

import com.challengeteam.shop.dto.pagination.paginationRequest.PhoneFilterDto;
import com.challengeteam.shop.entity.phone.Phone;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class PhoneSpecification {

    private static Specification<Phone> hasBrand(String brand) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        criteriaBuilder.lower(root.get("brandName")),
                        brand.toLowerCase()
                );
    }

    private static Specification<Phone> priceGreaterThanOrEqual(BigDecimal minPrice) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice);
    }

    private static Specification<Phone> priceLessThanOrEqual(BigDecimal maxPrice) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice);
    }

    public static Specification<Phone> build(PhoneFilterDto requestDto) {
        Specification<Phone> spec = (root, query, cb) -> cb.conjunction();

        if (requestDto.brand() != null && !requestDto.brand().isBlank()) {
            spec = spec.and(hasBrand(requestDto.brand()));
        }
        if (requestDto.minPrice() != null) {
            spec = spec.and(priceGreaterThanOrEqual(requestDto.minPrice()));
        }
        if (requestDto.maxPrice() != null) {
            spec = spec.and(priceLessThanOrEqual(requestDto.maxPrice()));
        }
        return spec;
    }
}
