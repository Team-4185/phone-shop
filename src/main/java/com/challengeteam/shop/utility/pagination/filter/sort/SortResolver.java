package com.challengeteam.shop.utility.pagination.filter.sort;

import com.challengeteam.shop.constraints.filter.FilterRequestConstraints;
import com.challengeteam.shop.entity.phone.Phone_;
import lombok.experimental.UtilityClass;
import org.springframework.data.domain.Sort;

@UtilityClass
public class SortResolver {
    public static Sort resolve(String sortOrder) {
        return switch (sortOrder.toLowerCase()) {
            case FilterRequestConstraints.SORT_ORDER_BY_NAME_DESC -> Sort.by(Sort.Direction.DESC, Phone_.NAME);
            case FilterRequestConstraints.SORT_ORDER_BY_PRICE_ASC -> Sort.by(Sort.Direction.ASC, Phone_.PRICE);
            case FilterRequestConstraints.SORT_ORDER_BY_PRICE_DESC -> Sort.by(Sort.Direction.DESC, Phone_.PRICE);
            default -> Sort.by(Sort.Direction.ASC, Phone_.NAME);
        };
    }
}