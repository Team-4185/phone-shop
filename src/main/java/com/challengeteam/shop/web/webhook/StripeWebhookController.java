package com.challengeteam.shop.web.webhook;

import com.challengeteam.shop.dto.webhook.StripeResponse;
import com.challengeteam.shop.exceptionHandling.exception.StripeWebhookException;
import com.challengeteam.shop.properties.StripeProperties;
import com.challengeteam.shop.web.handler.StripeWebhookHandler;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.StripeObject;
import com.stripe.net.Webhook;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/webhook/stripe")
@Tag(name = "Webhooks")
public class StripeWebhookController {
    private final StripeProperties stripeProperties;
    private final StripeWebhookHandler stripeWebhookHandler;

    @Operation(
            summary = "Handle Stripe webhook events",
            description = """
                    Receives and processes webhook events from Stripe payment gateway.
                    This endpoint is called by Stripe's servers when payment events occur.
                    
                    Important:
                    - This endpoint does NOT require JWT authentication
                    - Authentication is done via Stripe-Signature header verification
                    - Must respond quickly (within 5 seconds) to avoid timeouts
                    - Events may be delivered multiple times - ensure idempotent processing
                    
                    Common event types:
                    - checkout.session.completed
                    - payment_intent.succeeded
                    """
    )
    @PostMapping
    public ResponseEntity<Void> handleWebhook(@RequestBody String payload,
                                              @RequestHeader("Stripe-Signature") String stripeSing) {
        try {
            Event event = authenticateStripe(payload, stripeSing, stripeProperties.getWebhookSecret());
            StripeObject object = event.getDataObjectDeserializer()
                    .getObject()
                    .orElseThrow(() -> new StripeWebhookException(HttpStatus.BAD_REQUEST));
            StripeResponse response = stripeWebhookHandler.handleEvent(event.getType(), object);
            return ResponseEntity
                    .status(response.status())
                    .build();
        } catch (StripeWebhookException e) {
            return ResponseEntity
                    .status(e.getStatus())
                    .build();
        }
    }

    private Event authenticateStripe(String payload, String stripeSign, String webhookSecret) throws StripeWebhookException {
        if (stripeSign != null && payload != null) {
            Event event;

            try {
                event = Webhook.constructEvent(payload, stripeSign, webhookSecret);
            } catch (SignatureVerificationException e) {
                throw new StripeWebhookException(HttpStatus.UNAUTHORIZED);
            }

            log.debug("Successfully authenticated webhook request");
            return event;
        } else {
            throw new StripeWebhookException(HttpStatus.BAD_REQUEST);
        }
    }

}
