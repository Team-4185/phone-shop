package com.challengeteam.shop.service.admin.impl;

import com.challengeteam.shop.dto.admin.customer.AdminCustomerDetailsResponseDto;
import com.challengeteam.shop.dto.admin.customer.AdminCustomerFilterDto;
import com.challengeteam.shop.dto.admin.customer.AdminCustomerListItemResponseDto;
import com.challengeteam.shop.entity.order.Order;
import com.challengeteam.shop.entity.user.User;
import com.challengeteam.shop.exceptionHandling.exception.ResourceNotFoundException;
import com.challengeteam.shop.mapper.AdminCustomerMapper;
import com.challengeteam.shop.persistence.repository.OrderRepository;
import com.challengeteam.shop.persistence.repository.UserRepository;
import com.challengeteam.shop.persistence.specification.AdminCustomerSpecification;
import com.challengeteam.shop.service.admin.AdminCustomerService;
import com.challengeteam.shop.service.admin.AdminCustomerStatusService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default read-side service for admin Customers.
 *
 * <p>Customer status and purchase metrics are derived from orders, keeping public user flows
 * separated from admin read models.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AdminCustomerServiceImpl implements AdminCustomerService {

  private static final int CUSTOMER_DETAILS_RECENT_ORDERS_LIMIT = 5;

  private final UserRepository userRepository;
  private final OrderRepository orderRepository;
  private final AdminCustomerMapper adminCustomerMapper;
  private final AdminCustomerStatusService adminCustomerStatusService;

  @Override
  public Page<AdminCustomerListItemResponseDto> getCustomers(
      int page, int size, AdminCustomerFilterDto filterDto) {
    log.debug("Get admin customers page={} size={} filters={}", page, size, filterDto);
    Pageable pageable = PageRequest.of(page, size, buildRepositorySort(filterDto.sort()));
    Specification<User> specification = AdminCustomerSpecification.build(filterDto);
    Page<User> customersPage = userRepository.findAll(specification, pageable);

    if (customersPage.isEmpty()) {
      log.debug("No admin customers found for page={} size={} filters={}", page, size, filterDto);
      return customersPage.map(user -> adminCustomerMapper.toListItem(user, List.of(), null));
    }

    Map<Long, List<Order>> ordersByCustomerId =
        orderRepository.findAllByUsers(customersPage.getContent()).stream()
            .collect(Collectors.groupingBy(order -> order.getUser().getId()));

    List<AdminCustomerListItemResponseDto> customers =
        customersPage.getContent().stream()
            .map(
                customer -> {
                  List<Order> orders = ordersByCustomerId.getOrDefault(customer.getId(), List.of());
                  return adminCustomerMapper.toListItem(
                      customer, orders, adminCustomerStatusService.resolveStatus(orders));
                })
            .toList();

    customers = applyDerivedSort(customers, filterDto.sort());

    log.debug(
        "Found admin customers page={} size={} totalElements={}",
        page,
        size,
        customersPage.getTotalElements());
    return new PageImpl<>(customers, pageable, customersPage.getTotalElements());
  }

  @Override
  public AdminCustomerDetailsResponseDto getCustomerById(Long id) {
    log.debug("Get admin customer details id={}", id);
    User customer =
        userRepository
            .findById(id)
            .filter(user -> "USER".equals(user.getRole().getName()))
            .orElseThrow(
                () -> {
                  log.warn("Admin customer id={} was not found", id);
                  return new ResourceNotFoundException("Not found customer with id: " + id);
                });
    List<Order> orders = orderRepository.findAllByUsers(List.of(customer));
    List<Order> recentOrders =
        orderRepository.findRecentByUserId(
            customer.getId(), PageRequest.of(0, CUSTOMER_DETAILS_RECENT_ORDERS_LIMIT));

    return adminCustomerMapper.toDetails(
        customer, orders, recentOrders, adminCustomerStatusService.resolveStatus(orders));
  }

  private Sort buildRepositorySort(String sort) {
    return switch (sort) {
      case "email_desc" -> Sort.by("email").descending();
      case "email_asc" -> Sort.by("email").ascending();
      case "createdAt_asc" -> Sort.by("createdAt").ascending();
      default -> Sort.by("createdAt").descending();
    };
  }

  private List<AdminCustomerListItemResponseDto> applyDerivedSort(
      List<AdminCustomerListItemResponseDto> customers, String sort) {
    Comparator<AdminCustomerListItemResponseDto> comparator =
        switch (sort) {
          case "totalOrders_asc" -> Comparator.comparing(AdminCustomerListItemResponseDto::totalOrders);
          case "totalOrders_desc" ->
              Comparator.comparing(AdminCustomerListItemResponseDto::totalOrders).reversed();
          case "totalSpent_asc" -> Comparator.comparing(AdminCustomerListItemResponseDto::totalSpent);
          case "totalSpent_desc" ->
              Comparator.comparing(AdminCustomerListItemResponseDto::totalSpent).reversed();
          case "lastOrderAt_asc" ->
              Comparator.comparing(
                  AdminCustomerListItemResponseDto::lastOrderAt,
                  Comparator.nullsLast(Instant::compareTo));
          case "lastOrderAt_desc" ->
              Comparator.comparing(
                      AdminCustomerListItemResponseDto::lastOrderAt,
                      Comparator.nullsLast(Instant::compareTo))
                  .reversed();
          default -> null;
        };

    return comparator == null ? customers : customers.stream().sorted(comparator).toList();
  }
}
