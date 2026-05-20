package com.challengeteam.shop.dto.notification.email;

import java.math.BigDecimal;

/**
 * Lightweight DTO used only for rendering the order confirmation email template.
 * Avoids passing JPA entities directly into the Thymeleaf context.
 */
public record OrderItemEmailDto(
        String productName,
        String sku,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal totalPrice
) {
}