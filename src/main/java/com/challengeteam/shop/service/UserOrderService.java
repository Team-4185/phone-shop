package com.challengeteam.shop.service;

import com.challengeteam.shop.dto.order.CreateOrderDto;
import com.challengeteam.shop.dto.order.CreateOrderResponseDto;

public interface UserOrderService {

    CreateOrderResponseDto executeOrderWorkflow(Long userId, CreateOrderDto createOrderDto);

}
