package com.challengeteam.shop.mapper;

import com.challengeteam.shop.dto.admin.order.AdminOrderDetailsResponseDto;
import com.challengeteam.shop.dto.admin.order.AdminOrderItemResponseDto;
import com.challengeteam.shop.dto.admin.order.AdminOrderListItemResponseDto;
import com.challengeteam.shop.entity.order.Order;
import com.challengeteam.shop.entity.order.OrderItem;
import com.challengeteam.shop.service.admin.AdminOrderWorkflowService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Maps order domain objects to DTOs dedicated to the admin Order Management API. */
@Component
@RequiredArgsConstructor
public class AdminOrderMapper {

  private final AdminOrderWorkflowService adminOrderWorkflowService;

  public AdminOrderListItemResponseDto toListItem(Order order) {
    return new AdminOrderListItemResponseDto(
        order.getId(),
        order.getUser().getId(),
        order.getUser().getEmail(),
        order.getStatus(),
        order.getTotal(),
        order.getItems().stream().mapToInt(OrderItem::getQuantity).sum(),
        order.getCreatedAt(),
        order.getUpdatedAt());
  }

  public AdminOrderDetailsResponseDto toDetails(Order order) {
    return new AdminOrderDetailsResponseDto(
        order.getId(),
        order.getUser().getId(),
        order.getUser().getEmail(),
        order.getUser().getFirstName(),
        order.getUser().getLastName(),
        order.getUser().getPhoneNumber(),
        order.getUser().getCity(),
        order.getStatus(),
        order.getTotal(),
        adminOrderWorkflowService.getAvailableActions(order.getStatus()),
        order.getItems().stream().map(this::toItem).toList(),
        order.getCreatedAt(),
        order.getUpdatedAt());
  }

  private AdminOrderItemResponseDto toItem(OrderItem item) {
    Long phoneId = item.getPhone() == null ? null : item.getPhone().getId();

    return new AdminOrderItemResponseDto(
        item.getId(),
        phoneId,
        item.getProductName(),
        item.getSku(),
        item.getUnitPrice(),
        item.getQuantity(),
        item.getTotalPrice());
  }
}
