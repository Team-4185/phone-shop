package com.challengeteam.shop.dto.order.request;

import com.challengeteam.shop.dto.order.request.payment.PaymentDetailsRequestDto;
import com.challengeteam.shop.dto.order.request.shippingAddress.ShippingAddressRequestDto;
import com.challengeteam.shop.entity.order.DeliveryMethod;
import com.challengeteam.shop.entity.order.payment.PaymentMethod;

public interface OrderDetailsRequest {

    PaymentMethod paymentMethod();

    PaymentDetailsRequestDto paymentDetails();

    DeliveryMethod deliveryMethod();

    ShippingAddressRequestDto shippingAddress();
}
