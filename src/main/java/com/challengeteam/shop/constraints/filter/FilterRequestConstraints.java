package com.challengeteam.shop.constraints.filter;

import lombok.experimental.UtilityClass;

@UtilityClass
public class FilterRequestConstraints {
    public final String MIN_PHONE_PRICE = "0";
    public final String SORT_ORDER_BY_NAME_ASC = "name_asc";
    public final String SORT_ORDER_BY_NAME_DESC = "name_desc";
    public final String SORT_ORDER_BY_PRICE_ASC = "price_asc";
    public final String SORT_ORDER_BY_PRICE_DESC = "price_desc";

    public static final String SORT_ORDER_REGEXP =
            SORT_ORDER_BY_NAME_ASC + "|" +
                    SORT_ORDER_BY_NAME_DESC + "|" +
                    SORT_ORDER_BY_PRICE_ASC + "|" +
                    SORT_ORDER_BY_PRICE_DESC;
}