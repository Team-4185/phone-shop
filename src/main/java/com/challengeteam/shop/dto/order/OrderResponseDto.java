package com.challengeteam.shop.dto.order;

import com.challengeteam.shop.dto.order.response.orderItem.OrderItemResponseDto;
import com.challengeteam.shop.dto.order.response.paymentDetails.PaymentDetailsResponseDto;
import com.challengeteam.shop.dto.order.response.shippingAddress.ShippingAddressResponseDto;
import com.challengeteam.shop.dto.order.response.user.UserResponseDto;
import com.challengeteam.shop.entity.order.DeliveryMethod;
import com.challengeteam.shop.entity.order.OrderStatus;
import com.challengeteam.shop.entity.order.payment.PaymentMethod;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO for {@link com.challengeteam.shop.entity.order.Order}
 */
public record OrderResponseDto(Long id,
                               Instant createdAt,
                               Instant updatedAt,
                               UserResponseDto user,
                               String customerEmail,
                               String customerFirstName,
                               String customerLastName,
                               String customerPhoneNumber,
                               OrderStatus status,
                               PaymentMethod paymentMethod,
                               DeliveryMethod deliveryMethod,
                               String paymentProvider,
                               String deliveryProvider,
                               String pickupPointId,
                               LocalDate estimatedDeliveryDate,
                               BigDecimal deliveryPrice,
                               ShippingAddressResponseDto shippingAddress,
                               PaymentDetailsResponseDto paymentDetails,
                               BigDecimal total,
                               List<OrderItemResponseDto> items) implements Serializable {
}
