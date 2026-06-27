package com.challengeteam.shop.service.order.customer;

import com.challengeteam.shop.dto.order.OrderResponseDto;
import com.challengeteam.shop.dto.order.request.checkout.CheckoutRequestDto;
import org.springframework.security.core.Authentication;

public interface CustomerOrderService {

    OrderResponseDto checkoutFromCart(CheckoutRequestDto request, Authentication authentication);

    OrderResponseDto cancel(long orderId, Authentication authentication);
}
