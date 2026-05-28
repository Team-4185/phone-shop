package com.challengeteam.shop.mapper;

import com.challengeteam.shop.dto.admin.dashboard.AdminDashboardLowStockAlertResponseDto;
import com.challengeteam.shop.dto.admin.dashboard.AdminDashboardRecentOrderResponseDto;
import com.challengeteam.shop.entity.order.Order;
import com.challengeteam.shop.entity.order.OrderItem;
import com.challengeteam.shop.entity.phone.Phone;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/** Maps domain entities to dashboard widget DTOs. */
@Component
public class AdminDashboardMapper {

  public AdminDashboardRecentOrderResponseDto toRecentOrder(Order order) {
    return new AdminDashboardRecentOrderResponseDto(
        order.getId(),
        order.getCustomerEmail(),
        resolveCustomerName(order),
        resolveProductName(order),
        order.getStatus(),
        order.getPaymentDetails().getPaymentStatus(),
        order.getTotal(),
        order.getCreatedAt());
  }

  public AdminDashboardLowStockAlertResponseDto toLowStockAlert(Phone phone, int threshold) {
    return new AdminDashboardLowStockAlertResponseDto(
        phone.getId(),
        phone.getName(),
        phone.getSku(),
        phone.getBrand(),
        phone.getStock(),
        threshold,
        phone.getStatus());
  }

  private String resolveCustomerName(Order order) {
    String fullName =
        java.util.stream.Stream.of(order.getCustomerFirstName(), order.getCustomerLastName())
            .filter(Objects::nonNull)
            .filter(value -> !value.isBlank())
            .collect(Collectors.joining(" "));
    return fullName.isBlank() ? order.getCustomerEmail() : fullName;
  }

  private String resolveProductName(Order order) {
    return order.getItems().stream()
        .findFirst()
        .map(OrderItem::getProductName)
        .orElse(null);
  }
}
