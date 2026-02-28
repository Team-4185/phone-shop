package com.challengeteam.shop.web.handler;

import com.challengeteam.shop.dto.webhook.StripeResponse;
import com.challengeteam.shop.entity.order.Order;
import com.challengeteam.shop.exceptionHandling.exception.CriticalSystemException;
import com.challengeteam.shop.service.OrderService;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class StripeWebhookHandlerImpl implements StripeWebhookHandler {
    public static final String PAYMENT_INTENT_SUCCEEDED = "payment_intent.succeeded";
    public static final String CHECKOUT_SESSION_EXPIRED = "checkout.session.expired";
    private final OrderService orderService;

    @Override
    public StripeResponse handleEvent(String eventType, StripeObject stripeObject) {
        switch (eventType) {
            case PAYMENT_INTENT_SUCCEEDED -> {
                return handlePaymentIntentSucceeded(stripeObject);
            }
            case CHECKOUT_SESSION_EXPIRED -> {
                return handleCheckoutSessionExpired(stripeObject);
            }
            default -> {
                log.warn("Unhandled event appeared: {}", eventType);
                return new StripeResponse(HttpStatus.BAD_REQUEST);
            }
        }
    }

    private StripeResponse handlePaymentIntentSucceeded(StripeObject stripeObject) {
        PaymentIntent intent = (PaymentIntent) stripeObject;

        long orderId = Long.parseLong(intent.getMetadata().get("orderId"));
        Order order = orderService
                .getById(orderId)
                .orElseThrow(() -> new CriticalSystemException("Not found order with id: " + orderId + " , but order is paid"));
        if (!order.isProcessedByWebhook()) {
            orderService.makeOrderPaid(orderId, intent.getId());
            orderService.setProcessedByWebhook(orderId, true);
            log.debug("Handled payment_intent.succeeded event for order with id: {}", orderId);
        }

        return new StripeResponse(HttpStatus.OK);
    }

    private StripeResponse handleCheckoutSessionExpired(StripeObject stripeObject) {
        Session session = (Session) stripeObject;

        long orderId = Long.parseLong(session.getMetadata().get("orderId"));
        Order order = orderService
                .getById(orderId)
                .orElseThrow(() -> new CriticalSystemException("Not found order with id: " + orderId));
        if (!order.isProcessedByWebhook()) {
            orderService.makeOrderFailed(orderId);
            orderService.setProcessedByWebhook(orderId, true);
            log.debug("Handled checkout.session.expired event for order with id: {}", orderId);
        }

        return new StripeResponse(HttpStatus.OK);
    }

}
