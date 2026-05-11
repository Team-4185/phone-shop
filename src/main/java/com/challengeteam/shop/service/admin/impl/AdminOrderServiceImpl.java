package com.challengeteam.shop.service.admin.impl;

import com.challengeteam.shop.dto.admin.order.AdminOrderDetailsResponseDto;
import com.challengeteam.shop.dto.admin.order.AdminOrderFilterDto;
import com.challengeteam.shop.dto.admin.order.AdminOrderListItemResponseDto;
import com.challengeteam.shop.entity.order.Order;
import com.challengeteam.shop.entity.order.OrderStatus;
import com.challengeteam.shop.exceptionHandling.exception.InvalidPriceRangeException;
import com.challengeteam.shop.exceptionHandling.exception.ResourceNotFoundException;
import com.challengeteam.shop.mapper.AdminOrderMapper;
import com.challengeteam.shop.persistence.repository.OrderRepository;
import com.challengeteam.shop.persistence.specification.AdminOrderSpecification;
import com.challengeteam.shop.service.admin.AdminOrderService;
import com.challengeteam.shop.service.admin.AdminOrderWorkflowService;
import java.util.List;
import java.util.Map;
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
 * Default admin order service.
 *
 * <p>Pagination is applied before collection fetches, so the list endpoint keeps stable page
 * boundaries while still returning item counts for the admin table.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AdminOrderServiceImpl implements AdminOrderService {

  private final OrderRepository orderRepository;
  private final AdminOrderMapper adminOrderMapper;
  private final AdminOrderWorkflowService adminOrderWorkflowService;

  @Override
  public Page<AdminOrderListItemResponseDto> getOrders(
      int page, int size, AdminOrderFilterDto filterDto) {
    validateTotalRange(filterDto);
    log.debug("Get admin orders page={} size={} filters={}", page, size, filterDto);

    Pageable pageable = PageRequest.of(page, size, buildSort(filterDto.sort()));
    Specification<Order> specification = AdminOrderSpecification.build(filterDto);
    Page<Order> ordersPage = orderRepository.findAll(specification, pageable);

    if (ordersPage.isEmpty()) {
      log.debug(
          "No admin orders found for page={} size={} filters={} specification={}",
          page,
          size,
          filterDto,
          specification);
      return ordersPage.map(adminOrderMapper::toListItem);
    }

    List<Order> ordersWithDetails = orderRepository.findAllWithDetails(ordersPage.getContent());
    Map<Long, Order> ordersById =
        ordersWithDetails.stream().collect(Collectors.toMap(Order::getId, order -> order));
    List<AdminOrderListItemResponseDto> orderedOrders =
        ordersPage.getContent().stream()
            .map(order -> ordersById.getOrDefault(order.getId(), order))
            .map(adminOrderMapper::toListItem)
            .toList();

    log.debug(
        "Found admin orders page={} size={} totalElements={}",
        page,
        size,
        ordersPage.getTotalElements());
    return new PageImpl<>(orderedOrders, pageable, ordersPage.getTotalElements());
  }

  @Override
  public AdminOrderDetailsResponseDto getOrderById(Long id) {
    log.debug("Get admin order details id={}", id);
    return adminOrderMapper.toDetails(findOrderWithDetails(id));
  }

  @Override
  @Transactional
  public AdminOrderDetailsResponseDto applyAction(Long id, String action) {
    log.debug("Apply admin order action orderId={} action={}", id, action);
    Order order = findOrderWithDetails(id);
    OrderStatus previousStatus = order.getStatus();
    OrderStatus targetStatus = adminOrderWorkflowService.resolveTargetStatus(previousStatus, action);

    order.setStatus(targetStatus);
    Order savedOrder = orderRepository.save(order);
    log.info(
        "Admin order status changed orderId={} action={} from={} to={}",
        id,
        action,
        previousStatus,
        targetStatus);

    return adminOrderMapper.toDetails(savedOrder);
  }

  private Order findOrderWithDetails(Long id) {
    return orderRepository
        .findByIdWithDetails(id)
        .orElseThrow(
            () -> {
              log.warn("Admin order id={} was not found", id);
              return new ResourceNotFoundException("Not found order with id: " + id);
            });
  }

  private void validateTotalRange(AdminOrderFilterDto filterDto) {
    if (filterDto.minTotal() != null
        && filterDto.maxTotal() != null
        && filterDto.minTotal().compareTo(filterDto.maxTotal()) > 0) {
      throw new InvalidPriceRangeException("minTotal cannot be greater than maxTotal");
    }
  }

  private Sort buildSort(String sort) {
    return switch (sort) {
      case "createdAt_asc" -> Sort.by("createdAt").ascending();
      case "total_asc" -> Sort.by("total").ascending();
      case "total_desc" -> Sort.by("total").descending();
      case "status_asc" -> Sort.by("status").ascending();
      case "status_desc" -> Sort.by("status").descending();
      case "paymentStatus_asc" -> Sort.by("paymentStatus").ascending();
      case "paymentStatus_desc" -> Sort.by("paymentStatus").descending();
      case "customerEmail_asc" -> Sort.by("customerEmail").ascending();
      case "customerEmail_desc" -> Sort.by("customerEmail").descending();
      case "id_asc" -> Sort.by("id").ascending();
      case "id_desc" -> Sort.by("id").descending();
      default -> Sort.by("createdAt").descending();
    };
  }
}
