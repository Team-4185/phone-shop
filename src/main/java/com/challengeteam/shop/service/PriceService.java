package com.challengeteam.shop.service;

import com.challengeteam.shop.entity.phone.Phone;

import java.math.BigDecimal;
import java.util.Map;

public interface PriceService {

    BigDecimal calculateTotalPrice(Map<Phone, Integer> products);
    BigDecimal calculatePriceForItem(Phone phone, Integer count);
}
