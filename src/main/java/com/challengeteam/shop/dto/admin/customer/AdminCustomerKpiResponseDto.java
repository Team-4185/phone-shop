package com.challengeteam.shop.dto.admin.customer;

import java.math.BigDecimal;

/** KPI card values for the admin Customers page. */
public record AdminCustomerKpiResponseDto(
    long totalClients,
    long newCustomersThisMonth,
    long inactiveCustomers,
    BigDecimal averageReceipt) {}
