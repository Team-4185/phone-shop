package com.challengeteam.shop.dto.order.response.orderItem;

import com.challengeteam.shop.dto.phone.PhoneResponseDto;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DTO for {@link com.challengeteam.shop.entity.order.OrderItem}
 */
public record OrderItemResponseDto(Long id,
                                   PhoneResponseDto phone,
                                   String productName,
                                   String sku,
                                   BigDecimal unitPrice,
                                   Integer quantity,
                                   BigDecimal totalPrice) implements Serializable {
}