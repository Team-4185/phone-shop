package com.challengeteam.shop.service.impl;

import com.challengeteam.shop.entity.phone.Phone;
import com.challengeteam.shop.service.PriceService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;
import java.util.Set;

@Service
public class PriceServiceImpl implements PriceService {

    @Override
    public BigDecimal calculateTotalPrice(Map<Phone, Integer> products) {
        BigDecimal result = new BigDecimal(BigInteger.ZERO);

        for (Map.Entry<Phone, Integer> entry : products.entrySet()) {
            BigDecimal itemPrice = calculatePriceForUnit(entry.getKey(), entry.getValue());
            result = result.add(itemPrice);
        }

        return result;
    }

    @Override
    public BigDecimal calculatePriceForItem(Phone phone, Integer amount) {
        return calculatePriceForUnit(phone, amount);
    }

    private BigDecimal calculatePriceForUnit(Phone phone, Integer amount) {
        BigDecimal unitPrice = phone.getPrice();

        return unitPrice.multiply(new BigDecimal(amount));
    }
}
