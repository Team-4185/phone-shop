package com.challengeteam.shop.dto.delivery;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

public record DeliveryQuote(
        String provider,
        BigDecimal price,
        LocalDate estimatedDeliveryDate) implements Serializable {
}
