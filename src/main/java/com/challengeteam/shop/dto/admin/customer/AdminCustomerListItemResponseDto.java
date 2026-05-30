package com.challengeteam.shop.dto.admin.customer;

import java.math.BigDecimal;
import java.time.Instant;

/** Customer row returned for the admin Customers table. */
public record AdminCustomerListItemResponseDto(
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
    Instant createdAt,
    Instant updatedAt) {}
