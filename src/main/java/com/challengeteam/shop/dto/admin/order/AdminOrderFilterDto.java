package com.challengeteam.shop.dto.admin.order;

import com.challengeteam.shop.entity.order.OrderStatus;
import com.challengeteam.shop.entity.order.PaymentStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * Query parameters supported by the admin Order Management table.
 *
 * @param search optional customer email, order id, or item SKU/name fragment
 * @param status optional order workflow status
 * @param paymentStatus optional payment status
 * @param minTotal optional lower bound for order total
 * @param maxTotal optional upper bound for order total
 * @param sort field and direction used by the admin table
 */
public record AdminOrderFilterDto(
    @Size(max = 100, message = "Search length must be less than or equal to 100 characters")
        String search,
    OrderStatus status,
    PaymentStatus paymentStatus,
    @DecimalMin(value = "0.0", message = "Minimum total cannot be negative") BigDecimal minTotal,
    @DecimalMin(value = "0.0", message = "Maximum total cannot be negative") BigDecimal maxTotal,
    @Pattern(
            regexp =
                "createdAt_asc|createdAt_desc|total_asc|total_desc|status_asc|status_desc|paymentStatus_asc|paymentStatus_desc|customerEmail_asc|customerEmail_desc|id_asc|id_desc",
            message =
                "Sort must be one of: createdAt_asc, createdAt_desc, total_asc, total_desc, status_asc, status_desc, paymentStatus_asc, paymentStatus_desc, customerEmail_asc, customerEmail_desc, id_asc, id_desc")
        String sort) {
  public AdminOrderFilterDto {
    sort = (sort == null || sort.isBlank()) ? "createdAt_desc" : sort;
  }
}
