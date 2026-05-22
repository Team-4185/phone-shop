package com.challengeteam.shop.service.admin.impl;

import com.challengeteam.shop.dto.admin.dashboard.AdminDashboardLowStockAlertResponseDto;
import com.challengeteam.shop.dto.admin.dashboard.AdminDashboardRecentOrderResponseDto;
import com.challengeteam.shop.dto.admin.dashboard.AdminDashboardSalesByBrandResponseDto;
import com.challengeteam.shop.dto.admin.dashboard.AdminDashboardSalesPointResponseDto;
import com.challengeteam.shop.dto.admin.dashboard.AdminDashboardSummaryResponseDto;
import com.challengeteam.shop.dto.admin.dashboard.AdminDashboardTopProductResponseDto;
import com.challengeteam.shop.entity.order.Order;
import com.challengeteam.shop.mapper.AdminDashboardMapper;
import com.challengeteam.shop.persistence.repository.OrderRepository;
import com.challengeteam.shop.persistence.repository.PhoneRepository;
import com.challengeteam.shop.persistence.repository.UserRepository;
import com.challengeteam.shop.service.admin.AdminDashboardService;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Default read-side service for admin Dashboard analytics. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AdminDashboardServiceImpl implements AdminDashboardService {

  private static final int LOW_STOCK_THRESHOLD = 10;
  private static final int DASHBOARD_LIST_LIMIT = 5;

  private final OrderRepository orderRepository;
  private final UserRepository userRepository;
  private final PhoneRepository phoneRepository;
  private final AdminDashboardMapper adminDashboardMapper;

  @Override
  public AdminDashboardSummaryResponseDto getSummary() {
    log.debug("Get admin dashboard summary");
    return new AdminDashboardSummaryResponseDto(
        orderRepository.sumTotalRevenue(),
        orderRepository.count(),
        userRepository.countByRole_Name("USER"),
        phoneRepository.countByStockLessThanEqual(LOW_STOCK_THRESHOLD));
  }

  @Override
  public List<AdminDashboardSalesPointResponseDto> getSalesAnalytics() {
    log.debug("Get admin dashboard sales analytics");
    return orderRepository.aggregateSalesByDate().stream()
        .map(
            row ->
                new AdminDashboardSalesPointResponseDto(
                    toLocalDate(row[0]), toBigDecimal(row[1]), toLong(row[2])))
        .toList();
  }

  @Override
  public List<AdminDashboardSalesByBrandResponseDto> getSalesByBrand() {
    log.debug("Get admin dashboard sales by brand");
    return orderRepository.aggregateSalesByBrand().stream()
        .map(
            row ->
                new AdminDashboardSalesByBrandResponseDto(
                    (String) row[0], toBigDecimal(row[1]), toLong(row[2])))
        .toList();
  }

  @Override
  public List<AdminDashboardTopProductResponseDto> getTopSellingProducts() {
    log.debug("Get admin dashboard top selling products");
    return orderRepository.findTopSellingProducts(DASHBOARD_LIST_LIMIT).stream()
        .map(
            row ->
                new AdminDashboardTopProductResponseDto(
                    toNullableLong(row[0]),
                    (String) row[1],
                    (String) row[2],
                    toLong(row[3]),
                    toBigDecimal(row[4])))
        .toList();
  }

  @Override
  public List<AdminDashboardRecentOrderResponseDto> getRecentOrders() {
    log.debug("Get admin dashboard recent orders");
    List<Order> recentOrders = orderRepository.findRecentOrders(PageRequest.of(0, DASHBOARD_LIST_LIMIT));
    return recentOrders.stream().map(adminDashboardMapper::toRecentOrder).toList();
  }

  @Override
  public List<AdminDashboardLowStockAlertResponseDto> getLowStockAlerts() {
    log.debug("Get admin dashboard low stock alerts threshold={}", LOW_STOCK_THRESHOLD);
    return phoneRepository
        .findLowStockProducts(LOW_STOCK_THRESHOLD, PageRequest.of(0, DASHBOARD_LIST_LIMIT))
        .stream()
        .map(adminDashboardMapper::toLowStockAlert)
        .toList();
  }

  private LocalDate toLocalDate(Object value) {
    if (value instanceof LocalDate localDate) {
      return localDate;
    }
    if (value instanceof Date date) {
      return date.toLocalDate();
    }
    return LocalDate.parse(value.toString());
  }

  private BigDecimal toBigDecimal(Object value) {
    return value instanceof BigDecimal bigDecimal ? bigDecimal : new BigDecimal(value.toString());
  }

  private Long toLong(Object value) {
    return ((Number) value).longValue();
  }

  private Long toNullableLong(Object value) {
    return value == null ? null : toLong(value);
  }
}
