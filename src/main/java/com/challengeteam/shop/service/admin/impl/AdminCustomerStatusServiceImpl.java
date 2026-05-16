package com.challengeteam.shop.service.admin.impl;

import com.challengeteam.shop.dto.admin.customer.AdminCustomerStatus;
import com.challengeteam.shop.entity.order.Order;
import com.challengeteam.shop.service.admin.AdminCustomerStatusService;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.stereotype.Service;

/** Default customer status rules for the admin Customers table. */
@Service
public class AdminCustomerStatusServiceImpl implements AdminCustomerStatusService {

  private static final long ACTIVE_WINDOW_DAYS = 90;
  private final Clock clock;

  public AdminCustomerStatusServiceImpl() {
    this.clock = Clock.systemUTC();
  }

  AdminCustomerStatusServiceImpl(Clock clock) {
    this.clock = clock;
  }

  @Override
  public AdminCustomerStatus resolveStatus(List<Order> orders) {
    if (orders.isEmpty()) {
      return AdminCustomerStatus.NEW;
    }

    Instant activeSince = Instant.now(clock).minus(ACTIVE_WINDOW_DAYS, ChronoUnit.DAYS);
    boolean hasRecentOrder =
        orders.stream().anyMatch(order -> !order.getCreatedAt().isBefore(activeSince));

    return hasRecentOrder ? AdminCustomerStatus.ACTIVE : AdminCustomerStatus.INACTIVE;
  }
}
