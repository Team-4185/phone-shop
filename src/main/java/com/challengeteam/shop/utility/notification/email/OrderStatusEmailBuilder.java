package com.challengeteam.shop.utility.notification.email;

import com.challengeteam.shop.dto.email.Notification;
import com.challengeteam.shop.entity.order.Order;
import com.challengeteam.shop.entity.order.OrderStatus;
import com.challengeteam.shop.service.notification.EmailNotificationSenderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.ZoneId;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderStatusEmailBuilder {

    private static final String TEMPLATE_NAME = "order-status-update";
    private static final String EMAIL_SUBJECT = "Your GadgetRoom order status changed #";
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")
                    .withZone(ZoneId.of("Europe/Kyiv"));

    private final TemplateEngine templateEngine;

    public Notification buildOrderStatusChangedNotification(Order order,
                                                            OrderStatus previousStatus,
                                                            OrderStatus currentStatus) {
        Context context = new Context();
        context.setVariable("customerFirstName", resolveCustomerFirstName(order));
        context.setVariable("orderNumber", order.getId());
        context.setVariable("previousStatus", formatStatus(previousStatus));
        context.setVariable("currentStatus", formatStatus(currentStatus));
        context.setVariable("updatedAt", DATE_FORMATTER.format(resolveUpdatedAt(order)));
        context.setVariable("total", order.getTotal());

        String rendered = templateEngine.process(TEMPLATE_NAME, context);
        log.debug("Rendered order status update email for order: {}", order.getId());
        return new Notification(
                order.getCustomerEmail(),
                "GadgetRoom <noreply@gadgetroom.ua>",
                EMAIL_SUBJECT + order.getId(),
                rendered
        );
    }

    private String resolveCustomerFirstName(Order order) {
        if (order.getUser() != null && order.getUser().getFirstName() != null) {
            return order.getUser().getFirstName();
        }
        if (order.getCustomerFirstName() != null) {
            return order.getCustomerFirstName();
        }
        return "Customer";
    }

    private String formatStatus(OrderStatus status) {
        return switch (status) {
            case NEW -> "New";
            case CONFIRMED -> "Confirmed";
            case PROCESSING -> "Processing";
            case SHIPPED -> "Shipped";
            case DELIVERED -> "Delivered";
            case CANCELLED -> "Cancelled";
        };
    }

    private Instant resolveUpdatedAt(Order order) {
        if (order.getUpdatedAt() != null) {
            return order.getUpdatedAt();
        }
        if (order.getCreatedAt() != null) {
            return order.getCreatedAt();
        }
        return Instant.now();
    }
}
