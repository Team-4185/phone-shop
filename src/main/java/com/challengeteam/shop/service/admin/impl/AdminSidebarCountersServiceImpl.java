package com.challengeteam.shop.service.admin.impl;

import com.challengeteam.shop.dto.admin.AdminSidebarCountersResponseDto;
import com.challengeteam.shop.entity.order.OrderStatus;
import com.challengeteam.shop.persistence.repository.OrderRepository;
import com.challengeteam.shop.persistence.repository.PhoneRepository;
import com.challengeteam.shop.persistence.repository.UserRepository;
import com.challengeteam.shop.service.admin.AdminSidebarCountersService;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminSidebarCountersServiceImpl implements AdminSidebarCountersService {

  private static final String CUSTOMER_ROLE = "USER";
  private static final Set<OrderStatus> ACTIVE_ORDER_STATUSES =
      Set.of(OrderStatus.NEW, OrderStatus.CONFIRMED, OrderStatus.PROCESSING, OrderStatus.SHIPPED);

  private final PhoneRepository phoneRepository;
  private final OrderRepository orderRepository;
  private final UserRepository userRepository;

  @Override
  public AdminSidebarCountersResponseDto getSidebarCounters() {
    return new AdminSidebarCountersResponseDto(
        phoneRepository.count(),
        orderRepository.countByStatusIn(ACTIVE_ORDER_STATUSES),
        userRepository.countByRole_Name(CUSTOMER_ROLE));
  }
}
