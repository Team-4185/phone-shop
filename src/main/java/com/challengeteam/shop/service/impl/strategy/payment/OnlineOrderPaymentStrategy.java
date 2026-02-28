package com.challengeteam.shop.service.impl.strategy.payment;

import com.challengeteam.shop.dto.order.CreateOrderResponseDto;
import com.challengeteam.shop.dto.order.OrderResponseDto;
import com.challengeteam.shop.entity.order.Order;
import com.challengeteam.shop.entity.order.OrderItem;
import com.challengeteam.shop.entity.order.PaymentMethod;
import com.challengeteam.shop.entity.phone.Phone;
import com.challengeteam.shop.exceptionHandling.exception.CriticalSystemException;
import com.challengeteam.shop.mapper.OrderMapper;
import com.challengeteam.shop.properties.StripeProperties;
import com.challengeteam.shop.service.OrderService;
import com.challengeteam.shop.service.PriceService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.stripe.param.checkout.SessionCreateParams.*;

@Slf4j
@RequiredArgsConstructor
@Component
public class OnlineOrderPaymentStrategy implements OrderPaymentStrategy {
    private final static PaymentMethod SUPPORTED_PAYMENT_METHOD = PaymentMethod.ONLINE;
    private final static long STRIPE_SUPPORTED_EXPIRATION_TIME_MIN = 60 * 30;
    private final static long STRIPE_SUPPORTED_EXPIRATION_TIME_MAX = 60 * 60 * 24;
    private final StripeCheckoutSessionCreator sessionCreator;
    private final OrderMapper orderMapper;
    private final OrderService orderService;

    @Override
    public PaymentMethod getSupportedMethod() {
        return SUPPORTED_PAYMENT_METHOD;
    }

    @Override
    public CreateOrderResponseDto processPayment(Order order) {
        Objects.requireNonNull(order, "order");

        Session session = sessionCreator.createCheckoutSession(order);
        order = orderService.setCheckoutUrl(order.getId(), session.getUrl());
        OrderResponseDto orderResponse = orderMapper.toResponse(order);

        return new CreateOrderResponseDto(orderResponse, "Please follow the link to pay.");
    }

    @RequiredArgsConstructor
    @Component
    private static class StripeCheckoutSessionCreator {
        private final static String DEFAULT_CURRENCY = "UAH";
        private final static int DEFAULT_CURRENCY_FRACTION_DIGITS = 2;
        private final StripeProperties stripeProperties;

        @PostConstruct
        public void setup() {
            Stripe.apiKey = stripeProperties.getPrivateKey();
        }

        public Session createCheckoutSession(Order order) {
            try {
                List<LineItem> items = buildLineItems(order.getOrderItems());
                Long expirationTime = countExpirationTime(stripeProperties.getCheckoutExpirationTime());
                SessionCreateParams sessionCreateParams = builder()
                        .setMode(Mode.PAYMENT)
                        .setUiMode(UiMode.HOSTED)
                        .addAllLineItem(items)
                        .setExpiresAt(expirationTime)
                        .setSuccessUrl(stripeProperties.getPaymentSuccessUrl())
                        .putMetadata("orderId", order.getId().toString())
                        .setPaymentIntentData(SessionCreateParams.PaymentIntentData.builder()
                                .putMetadata("orderId", order.getId().toString())
                                .build()
                        )
                        .build();

                Session session = Session.create(sessionCreateParams);

                if (session.getUrl() == null) {
                    throw new CriticalSystemException("Expected checkout url, but it is 'null'");
                }

                return session;
            } catch (StripeException e) {
                throw new CriticalSystemException("Failed to create checkout session for order with id: " + order.getId());
            }
        }

        private Long countExpirationTime(Integer checkoutExpirationTime) {
            if (checkoutExpirationTime < STRIPE_SUPPORTED_EXPIRATION_TIME_MIN
                || checkoutExpirationTime > STRIPE_SUPPORTED_EXPIRATION_TIME_MAX) {
                throw new IllegalArgumentException("Expiration time out of bounds");
            }

            return Instant.now().getEpochSecond() + checkoutExpirationTime;
        }

        private List<LineItem> buildLineItems(Set<OrderItem> items) {
            return items.stream()
                    .map(i -> LineItem.builder()
                            .setPriceData(buildPrice(i))
                            .setQuantity((long) i.getAmount())
                            .build()
                    )
                    .toList();
        }

        private LineItem.PriceData buildPrice(OrderItem item) {
            Phone phone = item.getPhone();
            BigDecimal price = phone.getPrice().movePointRight(DEFAULT_CURRENCY_FRACTION_DIGITS);
            var productData = LineItem.PriceData.ProductData.builder()
                    .setName(phone.getName())
                    .setDescription(phone.getDescription())
                    .build();

            return LineItem.PriceData.builder()
                    .setCurrency(DEFAULT_CURRENCY)
                    .setUnitAmountDecimal(price)
                    .setProductData(productData)
                    .build();
        }
    }
}
