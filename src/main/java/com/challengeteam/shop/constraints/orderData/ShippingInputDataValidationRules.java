package com.challengeteam.shop.constraints.orderData;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ShippingInputDataValidationRules {
    public final String REGION_PATTER_CONSTRAINT = "^[a-zA-Zа-яА-ЯёЁ\\s\\-']{2,50}$";
}
