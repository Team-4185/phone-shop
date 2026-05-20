package com.challengeteam.shop.mapper;

import com.challengeteam.shop.dto.admin.dashboard.AdminDashboardLowStockAlertResponseDto;
import com.challengeteam.shop.dto.admin.dashboard.AdminDashboardRecentOrderResponseDto;
import com.challengeteam.shop.entity.order.Order;
import com.challengeteam.shop.entity.phone.Phone;
import org.springframework.stereotype.Component;

/** Maps domain entities to dashboard widget DTOs. */
@Component
public class AdminDashboardMapper {

  public AdminDashboardRecentOrderResponseDto toRecentOrder(Order order) {
    return new AdminDashboardRecentOrderResponseDto(
        order.getId(),
        order.getCustomerEmail(),
        order.getStatus(),
        order.getPaymentDetails().getPaymentStatus(),
        order.getTotal(),
        order.getCreatedAt());
  }

  public AdminDashboardLowStockAlertResponseDto toLowStockAlert(Phone phone) {
    return new AdminDashboardLowStockAlertResponseDto(
        phone.getId(),
        phone.getName(),
        phone.getSku(),
        phone.getBrand(),
        phone.getStock(),
        phone.getStatus());
  }
}
