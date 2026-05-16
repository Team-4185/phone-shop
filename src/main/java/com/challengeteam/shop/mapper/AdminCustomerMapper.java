package com.challengeteam.shop.mapper;

import com.challengeteam.shop.dto.admin.customer.AdminCustomerDetailsResponseDto;
import com.challengeteam.shop.dto.admin.customer.AdminCustomerListItemResponseDto;
import com.challengeteam.shop.dto.admin.customer.AdminCustomerOrderSummaryResponseDto;
import com.challengeteam.shop.dto.admin.customer.AdminCustomerStatus;
import com.challengeteam.shop.entity.order.Order;
import com.challengeteam.shop.entity.user.User;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;

/** Maps user and order purchase data to admin customer DTOs. */
@Component
public class AdminCustomerMapper {

  public AdminCustomerListItemResponseDto toListItem(
      User user, List<Order> orders, AdminCustomerStatus status) {
    return new AdminCustomerListItemResponseDto(
        user.getId(),
        user.getEmail(),
        user.getFirstName(),
        user.getLastName(),
        user.getPhoneNumber(),
        user.getCity(),
        status,
        (long) orders.size(),
        calculateTotalSpent(orders),
        findLastOrderAt(orders),
        user.getCreatedAt(),
        user.getUpdatedAt());
  }

  public AdminCustomerDetailsResponseDto toDetails(
      User user, List<Order> orders, List<Order> recentOrders, AdminCustomerStatus status) {
    return new AdminCustomerDetailsResponseDto(
        user.getId(),
        user.getEmail(),
        user.getFirstName(),
        user.getLastName(),
        user.getPhoneNumber(),
        user.getCity(),
        status,
        (long) orders.size(),
        calculateTotalSpent(orders),
        findLastOrderAt(orders),
        recentOrders.stream().map(this::toOrderSummary).toList(),
        user.getCreatedAt(),
        user.getUpdatedAt());
  }

  private AdminCustomerOrderSummaryResponseDto toOrderSummary(Order order) {
    return new AdminCustomerOrderSummaryResponseDto(
        order.getId(),
        order.getStatus(),
        order.getPaymentStatus(),
        order.getTotal(),
        order.getCreatedAt());
  }

  public BigDecimal calculateTotalSpent(List<Order> orders) {
    return orders.stream()
        .map(Order::getTotal)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  public Instant findLastOrderAt(List<Order> orders) {
    return orders.stream().map(Order::getCreatedAt).max(Comparator.naturalOrder()).orElse(null);
  }
}
