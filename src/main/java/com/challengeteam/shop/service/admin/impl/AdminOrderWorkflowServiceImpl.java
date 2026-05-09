package com.challengeteam.shop.service.admin.impl;

import com.challengeteam.shop.entity.order.OrderStatus;
import com.challengeteam.shop.exceptionHandling.exception.InvalidOrderStatusTransitionException;
import com.challengeteam.shop.service.admin.AdminOrderWorkflowService;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/** Central order workflow definition used by admin action endpoints and response metadata. */
@Service
public class AdminOrderWorkflowServiceImpl implements AdminOrderWorkflowService {

  private static final Map<OrderStatus, Map<String, OrderStatus>> TRANSITIONS =
      Map.of(
          OrderStatus.NEW, Map.of("confirm", OrderStatus.CONFIRMED, "cancel", OrderStatus.CANCELLED),
          OrderStatus.CONFIRMED,
              Map.of("process", OrderStatus.PROCESSING, "cancel", OrderStatus.CANCELLED),
          OrderStatus.PROCESSING,
              Map.of("ship", OrderStatus.SHIPPED, "cancel", OrderStatus.CANCELLED),
          OrderStatus.SHIPPED, Map.of("deliver", OrderStatus.DELIVERED),
          OrderStatus.DELIVERED, Map.of(),
          OrderStatus.CANCELLED, Map.of());

  @Override
  public List<String> getAvailableActions(OrderStatus currentStatus) {
    return TRANSITIONS.getOrDefault(currentStatus, Map.of()).keySet().stream().sorted().toList();
  }

  @Override
  public OrderStatus resolveTargetStatus(OrderStatus currentStatus, String action) {
    OrderStatus targetStatus = TRANSITIONS.getOrDefault(currentStatus, Map.of()).get(action);

    if (targetStatus == null) {
      throw new InvalidOrderStatusTransitionException(
          "Order status %s does not support action '%s'".formatted(currentStatus, action));
    }

    return targetStatus;
  }
}
