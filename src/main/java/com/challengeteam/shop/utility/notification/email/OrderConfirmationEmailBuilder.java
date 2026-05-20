package com.challengeteam.shop.utility.notification.email;

import com.challengeteam.shop.dto.email.Notification;
import com.challengeteam.shop.dto.notification.email.OrderItemEmailDto;
import com.challengeteam.shop.entity.order.Order;
import com.challengeteam.shop.entity.order.OrderItem;
import com.challengeteam.shop.service.notification.EmailNotificationSenderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Component responsible for building order confirmation email notifications.
 * <p>
 * This builder constructs email notifications for order confirmations using Thymeleaf templates.
 * It processes order data and generates HTML content that is ready to be sent via
 * {@link EmailNotificationSenderService}.
 * </p>
 *
 * @see Notification
 * @see EmailNotificationSenderService
 * @see Order
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderConfirmationEmailBuilder {

    private static final String TEMPLATE_NAME = "order-confirmation";
    private static final String EMAIL_SUBJECT = "Your GadgetRoom order is confirmed! #";
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")
                    .withZone(ZoneId.of("Europe/Kyiv"));

    private final TemplateEngine templateEngine;

    /**
     * Builds a {@link Notification} ready to be passed to {@link EmailNotificationSenderService}.
     *
     * @param order the placed order entity
     * @return notification with rendered HTML content
     */
    public Notification buildOrderConfirmationNotification(Order order) {
        String htmlContent = buildHtmlContent(order);
        return new Notification(
                order.getCustomerEmail(),
                "GadgetRoom <noreply@gadgetroom.ua>",
                EMAIL_SUBJECT + order.getId(),
                htmlContent
        );
    }

    private String buildHtmlContent(Order order) {
        Context context = buildThymeleafContext(order);
        String rendered = templateEngine.process(TEMPLATE_NAME, context);
        log.debug("Rendered order confirmation email for order: {}", order.getId());
        return rendered;
    }

    private Context buildThymeleafContext(Order order) {
        Context context = new Context();

        context.setVariable("customerFirstName", resolveCustomerFirstName(order));
        context.setVariable("orderNumber", order.getId());
        context.setVariable("orderDate", DATE_FORMATTER.format(order.getCreatedAt()));
        context.setVariable("paymentMethod", formatPaymentMethod(order));
        context.setVariable("deliveryMethod", formatDeliveryMethod(order));
        context.setVariable("items", order.getItems().stream()
                .map(this::toEmailItem)
                .toList());
        context.setVariable("total", order.getTotal());

        return context;
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

    private String formatPaymentMethod(Order order) {
        return switch (order.getPaymentMethod()) {
            case CARD -> "Credit / Debit Card";
            case CASH_ON_DELIVERY -> "Cash on Delivery";
        };
    }

    private String formatDeliveryMethod(Order order) {
        return switch (order.getDeliveryMethod()) {
            case COURIER -> "Courier to address";
            case POST_OFFICE -> "Post office pickup";
            case PICKUP -> "Self-pickup";
        };
    }

    private OrderItemEmailDto toEmailItem(OrderItem item) {
        return new OrderItemEmailDto(
                item.getProductName(),
                item.getSku(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getTotalPrice()
        );
    }
}