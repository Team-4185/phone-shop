package com.challengeteam.shop.web.handler;

import com.challengeteam.shop.dto.webhook.StripeResponse;
import com.stripe.model.StripeObject;

public interface StripeWebhookHandler {

    StripeResponse handleEvent(String eventType, StripeObject stripeObject);

}
