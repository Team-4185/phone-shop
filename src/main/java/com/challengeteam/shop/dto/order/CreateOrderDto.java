package com.challengeteam.shop.dto.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record CreateOrderDto(
        @Valid
        @NotNull(message = "Recipient is required")
        OrderRecipient recipient,

        @Valid
        @NotNull(message = "Destination is required")
        OrderDestination destination,

        @Valid
        @NotNull(message = "Payment detail is required")
        OrderPaymentDetail paymentDetail
) {
}
