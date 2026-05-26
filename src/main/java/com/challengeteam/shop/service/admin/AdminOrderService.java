package com.challengeteam.shop.service.admin;

import com.challengeteam.shop.dto.admin.order.AdminOrderDetailsResponseDto;
import com.challengeteam.shop.dto.admin.order.AdminOrderFilterDto;
import com.challengeteam.shop.dto.admin.order.AdminOrderKpiResponseDto;
import com.challengeteam.shop.dto.admin.order.AdminOrderListItemResponseDto;
import org.springframework.data.domain.Page;

/** Read and workflow operations used by the admin Order Management API. */
public interface AdminOrderService {

  Page<AdminOrderListItemResponseDto> getOrders(int page, int size, AdminOrderFilterDto filterDto);

  AdminOrderKpiResponseDto getOrderKpi();

  AdminOrderDetailsResponseDto getOrderById(Long id);

  AdminOrderDetailsResponseDto applyAction(Long id, String action);
}
