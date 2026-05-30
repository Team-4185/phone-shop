package com.challengeteam.shop.service.admin;

import com.challengeteam.shop.dto.admin.customer.AdminCustomerStatus;
import com.challengeteam.shop.entity.order.Order;
import java.util.List;

/** Defines explicit backend rules for derived admin customer statuses. */
public interface AdminCustomerStatusService {

  AdminCustomerStatus resolveStatus(List<Order> orders);
}
