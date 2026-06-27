package com.challengeteam.shop.service.admin.impl;

import com.challengeteam.shop.constants.notification.type.Notification_type;
import com.challengeteam.shop.dto.admin.order.AdminOrderDetailsResponseDto;
import com.challengeteam.shop.dto.admin.order.AdminOrderFilterDto;
import com.challengeteam.shop.dto.admin.order.AdminOrderKpiResponseDto;
import com.challengeteam.shop.dto.admin.order.AdminOrderListItemResponseDto;
import com.challengeteam.shop.dto.email.Notification;
import com.challengeteam.shop.dto.payment.TransactionResult;
import com.challengeteam.shop.entity.order.Order;
import com.challengeteam.shop.entity.order.OrderStatus;
import com.challengeteam.shop.entity.order.payment.PaymentMethod;
import com.challengeteam.shop.entity.order.payment.PaymentStatus;
import com.challengeteam.shop.exceptionHandling.exception.InvalidPriceRangeException;
import com.challengeteam.shop.exceptionHandling.exception.ResourceNotFoundException;
import com.challengeteam.shop.exceptionHandling.exception.order.InvalidOrderStatusTransitionException;
import com.challengeteam.shop.exceptionHandling.exception.order.PaymentFailedException;
import com.challengeteam.shop.mapper.admin.AdminOrderMapper;
import com.challengeteam.shop.persistence.repository.OrderRepository;
import com.challengeteam.shop.persistence.specification.AdminOrderSpecification;
import com.challengeteam.shop.service.admin.AdminOrderService;
import com.challengeteam.shop.service.admin.AdminOrderWorkflowService;
import com.challengeteam.shop.service.notification.NotificationSenderService;
import com.challengeteam.shop.service.payment.PaymentProviderResolver;
import com.challengeteam.shop.utility.notification.email.OrderStatusEmailBuilder;
import java.util.EnumMap;
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
  private final NotificationSenderService notificationSenderService;
  private final OrderStatusEmailBuilder orderStatusEmailBuilder;
  private final PaymentProviderResolver paymentProviderResolver;

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
  public AdminOrderKpiResponseDto getOrderKpi() {
    Map<OrderStatus, Long> countsByStatus = new EnumMap<>(OrderStatus.class);
    orderRepository
        .countOrdersByStatus()
        .forEach(row -> countsByStatus.put((OrderStatus) row[0], (Long) row[1]));

    return new AdminOrderKpiResponseDto(
        countsByStatus.getOrDefault(OrderStatus.CONFIRMED, 0L),
        countsByStatus.getOrDefault(OrderStatus.PROCESSING, 0L),
        countsByStatus.getOrDefault(OrderStatus.DELIVERED, 0L),
        countsByStatus.getOrDefault(OrderStatus.CANCELLED, 0L));
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
    refundIfNeeded(order, targetStatus);
    Order savedOrder = orderRepository.save(order);
    sendStatusChangedNotification(savedOrder, previousStatus, targetStatus);
    log.info(
        "Admin order status changed orderId={} action={} from={} to={}",
        id,
        action,
        previousStatus,
        targetStatus);

    return adminOrderMapper.toDetails(savedOrder);
  }

  @Override
  @Transactional
  public AdminOrderDetailsResponseDto shipOrder(Long id, String trackingNumber) {
    Order order = findOrderWithDetails(id);
    OrderStatus previousStatus = order.getStatus();
    OrderStatus targetStatus =
        adminOrderWorkflowService.resolveTargetStatus(previousStatus, "ship");

    if (trackingNumber != null && !trackingNumber.isBlank()) {
      if (order.getShippingAddress() == null) {
        throw new InvalidOrderStatusTransitionException(
            "Cannot assign tracking number to an order without shipping address");
      }
      order.getShippingAddress().setTrackingNumber(trackingNumber.strip());
    }

    order.setStatus(targetStatus);
    Order savedOrder = orderRepository.save(order);
    sendStatusChangedNotification(savedOrder, previousStatus, targetStatus);
    return adminOrderMapper.toDetails(savedOrder);
  }

  private void refundIfNeeded(Order order, OrderStatus targetStatus) {
    if (targetStatus != OrderStatus.CANCELLED
        || order.getPaymentMethod() == PaymentMethod.CASH_ON_DELIVERY
        || order.getPaymentDetails().getPaymentStatus() != PaymentStatus.PAID) {
      return;
    }

    String transactionId = order.getPaymentDetails().getTransactionId();
    if (transactionId == null || transactionId.isBlank()) {
      throw new PaymentFailedException("Cannot refund paid order without payment transaction id");
    }

    TransactionResult refundResult =
        paymentProviderResolver
        .getProvider(order.getPaymentProvider())
        .refund(order.getPaymentDetails().getTransactionId(), order.getTotal());

    if (refundResult.paymentStatus() != PaymentStatus.REFUNDED) {
      String message =
          refundResult.errorMessage() == null || refundResult.errorMessage().isBlank()
              ? "Refund was not completed"
              : refundResult.errorMessage();
      log.error(
          "Refund failed for orderId={} provider={} transactionId={} status={} error={}",
          order.getId(),
          order.getPaymentProvider(),
          transactionId,
          refundResult.paymentStatus(),
          message);
      throw new PaymentFailedException(message);
    }

    order.getPaymentDetails().setPaymentStatus(PaymentStatus.REFUNDED);
  }

  private void sendStatusChangedNotification(
      Order order, OrderStatus previousStatus, OrderStatus targetStatus) {
    Notification notification =
        orderStatusEmailBuilder.buildOrderStatusChangedNotification(order, previousStatus, targetStatus);
    notificationSenderService.sendNotification(notification, Notification_type.EMAIL);
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
