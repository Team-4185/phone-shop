package com.challengeteam.shop.dto.order;

import com.challengeteam.shop.entity.order.PaymentMethod;
import jakarta.validation.constraints.NotNull;

public record OrderPaymentDetail(
        @NotNull(message = "Payment method is not valid. Please choose one of provided")
        PaymentMethod paymentMethod
) {
}
