package com.challengeteam.shop.mapper.admin;

import com.challengeteam.shop.dto.admin.order.AdminOrderDetailsResponseDto;
import com.challengeteam.shop.dto.admin.order.AdminOrderItemResponseDto;
import com.challengeteam.shop.dto.admin.order.AdminOrderListItemResponseDto;
import com.challengeteam.shop.entity.order.Order;
import com.challengeteam.shop.entity.order.OrderItem;
import com.challengeteam.shop.service.admin.AdminOrderWorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Maps order domain objects to DTOs dedicated to the admin Order Management API.
 */
@Component
@RequiredArgsConstructor
public class AdminOrderMapper {

    private final AdminOrderWorkflowService adminOrderWorkflowService;

    public AdminOrderListItemResponseDto toListItem(Order order) {
        return new AdminOrderListItemResponseDto(
                order.getId(),
                order.getUser() == null ? null : order.getUser().getId(),
                order.getCustomerEmail(),
                order.getStatus(),
                order.getPaymentMethod(),
                order.getPaymentDetails().getPaymentStatus(),
                order.getDeliveryMethod(),
                order.getPaymentProvider(),
                order.getDeliveryProvider(),
                order.getTotal(),
                order.getItems().stream().mapToInt(OrderItem::getQuantity).sum(),
                order.getCreatedAt(),
                order.getUpdatedAt());
    }

    public AdminOrderDetailsResponseDto toDetails(Order order) {
        return new AdminOrderDetailsResponseDto(
                order.getId(),
                order.getUser() == null ? null : order.getUser().getId(),
                order.getCustomerEmail(),
                order.getCustomerFirstName(),
                order.getCustomerLastName(),
                order.getCustomerPhoneNumber(),
                order.getShippingAddress() == null
                        ? null
                        : order.getShippingAddress().getCity(),
                order.getStatus(),
                order.getPaymentMethod(),
                order.getPaymentDetails().getPaymentStatus(),
                order.getDeliveryMethod(),
                order.getPaymentProvider(),
                order.getDeliveryProvider(),
                order.getPickupPointId(),
                order.getShippingAddress() == null
                        ? null
                        : order.getShippingAddress().getTrackingNumber(),
                order.getEstimatedDeliveryDate(),
                order.getDeliveryPrice(),
                order.getTotal(),
                adminOrderWorkflowService.getAvailableActions(order.getStatus()),
                order.getItems().stream().map(this::toItem).toList(),
                order.getCreatedAt(),
                order.getUpdatedAt());
    }

    private AdminOrderItemResponseDto toItem(OrderItem item) {
        Long phoneId = item.getPhone() == null ? null : item.getPhone().getId();

        return new AdminOrderItemResponseDto(
                item.getId(),
                phoneId,
                item.getProductName(),
                item.getSku(),
                item.getUnitPrice(),
                item.getQuantity(),
                item.getTotalPrice());
    }
}
