package com.challengeteam.shop.service.admin;

import com.challengeteam.shop.entity.order.OrderStatus;
import java.util.List;

/** Defines supported admin order workflow transitions and API action names. */
public interface AdminOrderWorkflowService {

  List<String> getAvailableActions(OrderStatus currentStatus);

  OrderStatus resolveTargetStatus(OrderStatus currentStatus, String action);
}
