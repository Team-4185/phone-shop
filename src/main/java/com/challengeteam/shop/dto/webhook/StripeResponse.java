package com.challengeteam.shop.dto.webhook;

import org.springframework.http.HttpStatus;

public record StripeResponse(
        HttpStatus status
) {
}
