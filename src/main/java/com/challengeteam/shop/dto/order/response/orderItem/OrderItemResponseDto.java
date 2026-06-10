package com.challengeteam.shop.dto.order.response.orderItem;

import com.challengeteam.shop.dto.phone.response.PhoneResponseDto;
import com.challengeteam.shop.entity.phone.PhoneColor;
import com.challengeteam.shop.entity.phone.StorageCapacity;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DTO for {@link com.challengeteam.shop.entity.order.OrderItem}
 */
public record OrderItemResponseDto(Long id,
                                   PhoneResponseDto phone,
                                   Long variantId,
                                   String productName,
                                   String sku,
                                   PhoneColor selectedColor,
                                   StorageCapacity selectedStorage,
                                   BigDecimal unitPrice,
                                   Integer quantity,
                                   BigDecimal totalPrice) implements Serializable {
}
