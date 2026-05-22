package com.challengeteam.shop.dto.admin.customer;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Query parameters supported by the admin Customers table.
 *
 * @param search optional email, name, phone, or city fragment
 * @param sort field and direction used by the admin table
 */
public record AdminCustomerFilterDto(
    @Size(max = 100, message = "Search length must be less than or equal to 100 characters")
        String search,
    @Pattern(
            regexp =
                "createdAt_asc|createdAt_desc|email_asc|email_desc|totalOrders_asc|totalOrders_desc|totalSpent_asc|totalSpent_desc|lastOrderAt_asc|lastOrderAt_desc",
            message =
                "Sort must be one of: createdAt_asc, createdAt_desc, email_asc, email_desc, totalOrders_asc, totalOrders_desc, totalSpent_asc, totalSpent_desc, lastOrderAt_asc, lastOrderAt_desc")
        String sort) {
  public AdminCustomerFilterDto {
    sort = (sort == null || sort.isBlank()) ? "createdAt_desc" : sort;
  }
}
