package com.challengeteam.shop.mapper.order;

import com.challengeteam.shop.dto.order.response.orderItem.OrderItemResponseDto;
import com.challengeteam.shop.entity.order.OrderItem;
import com.challengeteam.shop.mapper.phone.PhoneMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderItemMapper {

  private final PhoneMapper phoneMapper;

  public OrderItemResponseDto toOrderItemResponseDto(OrderItem orderItem) {
    if (orderItem == null) {
      return null;
    }
    return new OrderItemResponseDto(
        orderItem.getId(),
        orderItem.getPhone() == null ? null : phoneMapper.toResponse(orderItem.getPhone()),
        orderItem.getVariant() == null ? null : orderItem.getVariant().getId(),
        orderItem.getProductName(),
        orderItem.getSku(),
        orderItem.getSelectedColor(),
        orderItem.getSelectedStorage(),
        orderItem.getUnitPrice(),
        orderItem.getQuantity(),
        orderItem.getTotalPrice());
  }
}
