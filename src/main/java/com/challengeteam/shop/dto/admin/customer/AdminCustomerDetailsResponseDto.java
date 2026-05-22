package com.challengeteam.shop.dto.admin.customer;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/** Full customer payload for the admin customer details screen. */
public record AdminCustomerDetailsResponseDto(
    Long id,
    String email,
    String firstName,
    String lastName,
    String phoneNumber,
    String city,
    AdminCustomerStatus status,
    Long totalOrders,
    BigDecimal totalSpent,
    Instant lastOrderAt,
    List<AdminCustomerOrderSummaryResponseDto> recentOrders,
    Instant createdAt,
    Instant updatedAt) {}
