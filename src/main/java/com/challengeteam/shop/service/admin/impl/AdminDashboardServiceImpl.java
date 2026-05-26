package com.challengeteam.shop.service.admin.impl;

import com.challengeteam.shop.dto.admin.dashboard.AdminDashboardLowStockAlertResponseDto;
import com.challengeteam.shop.dto.admin.dashboard.AdminDashboardRecentOrderResponseDto;
import com.challengeteam.shop.dto.admin.dashboard.AdminDashboardSalesByBrandResponseDto;
import com.challengeteam.shop.dto.admin.dashboard.AdminDashboardSalesPointResponseDto;
import com.challengeteam.shop.dto.admin.dashboard.AdminDashboardSalesPeriod;
import com.challengeteam.shop.dto.admin.dashboard.AdminDashboardSummaryResponseDto;
import com.challengeteam.shop.dto.admin.dashboard.AdminDashboardTopProductResponseDto;
import com.challengeteam.shop.entity.order.OrderStatus;
import com.challengeteam.shop.entity.phone.ProductStatus;
import com.challengeteam.shop.entity.order.Order;
import com.challengeteam.shop.mapper.AdminDashboardMapper;
import com.challengeteam.shop.persistence.repository.OrderRepository;
import com.challengeteam.shop.persistence.repository.PhoneRepository;
import com.challengeteam.shop.persistence.repository.UserRepository;
import com.challengeteam.shop.service.admin.AdminDashboardService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
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
  private static final String CUSTOMER_ROLE = "USER";

  private final OrderRepository orderRepository;
  private final UserRepository userRepository;
  private final PhoneRepository phoneRepository;
  private final AdminDashboardMapper adminDashboardMapper;

  @Override
  public AdminDashboardSummaryResponseDto getSummary() {
    log.debug("Get admin dashboard summary");
    Instant now = Instant.now();
    Instant currentPeriodStart = now.minus(java.time.Duration.ofDays(30));
    Instant previousPeriodStart = currentPeriodStart.minus(java.time.Duration.ofDays(30));
    LocalDate currentMonth = LocalDate.now(ZoneOffset.UTC).withDayOfMonth(1);
    LocalDate nextMonth = currentMonth.plusMonths(1);
    LocalDate previousMonth = currentMonth.minusMonths(1);

    BigDecimal currentRevenue = orderRepository.sumRevenueBetween(currentPeriodStart, now);
    BigDecimal previousRevenue =
        orderRepository.sumRevenueBetween(previousPeriodStart, currentPeriodStart);
    long currentOrders =
        orderRepository.countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(
            currentPeriodStart, now);
    long previousOrders =
        orderRepository.countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(
            previousPeriodStart, currentPeriodStart);
    long newClients =
        userRepository.countByRoleNameAndCreatedAtBetween(
            CUSTOMER_ROLE, toInstant(currentMonth), toInstant(nextMonth));
    long previousNewClients =
        userRepository.countByRoleNameAndCreatedAtBetween(
            CUSTOMER_ROLE, toInstant(previousMonth), toInstant(currentMonth));

    return new AdminDashboardSummaryResponseDto(
        currentRevenue,
        calculateChangePercent(currentRevenue, previousRevenue),
        currentOrders,
        calculateChangePercent(currentOrders, previousOrders),
        orderRepository.countByStatus(OrderStatus.PROCESSING),
        phoneRepository.sumStock(),
        null,
        phoneRepository.countByStockLessThanEqual(LOW_STOCK_THRESHOLD),
        newClients,
        calculateChangePercent(newClients, previousNewClients));
  }

  @Override
  public List<AdminDashboardSalesPointResponseDto> getSalesAnalytics(
      AdminDashboardSalesPeriod period) {
    log.debug("Get admin dashboard sales analytics period={}", period);
    LocalDate today = LocalDate.now(ZoneOffset.UTC);
    LocalDate startDate =
        switch (period) {
          case WEEK -> today.minusDays(6);
          case MONTH -> today.minusDays(29);
          case YEAR -> today.withDayOfMonth(1).minusMonths(11);
        };
    LocalDate endDate = period == AdminDashboardSalesPeriod.YEAR
        ? today.withDayOfMonth(1).plusMonths(1)
        : today.plusDays(1);

    List<Object[]> rows =
        period == AdminDashboardSalesPeriod.YEAR
            ? orderRepository.aggregateSalesByMonthBetween(toInstant(startDate), toInstant(endDate))
            : orderRepository.aggregateSalesByDateBetween(toInstant(startDate), toInstant(endDate));

    return rows.stream()
        .map(
            row ->
                new AdminDashboardSalesPointResponseDto(
                    toLocalDate(row[0]),
                    toBigDecimal(row[1]),
                    toBigDecimal(row[1]),
                    toLong(row[2]),
                    toLong(row[2])))
        .toList();
  }

  @Override
  public List<AdminDashboardSalesByBrandResponseDto> getSalesByBrand() {
    log.debug("Get admin dashboard sales by brand");
    List<Object[]> rows = orderRepository.aggregateSalesByBrand();
    BigDecimal totalRevenue =
        rows.stream()
            .map(row -> toBigDecimal(row[1]))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    return rows.stream()
        .map(
            row ->
                new AdminDashboardSalesByBrandResponseDto(
                    (String) row[0],
                    toBigDecimal(row[1]),
                    toLong(row[2]),
                    calculateSharePercent(toBigDecimal(row[1]), totalRevenue)))
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
                    toBigDecimal(row[4]),
                    toNullableInteger(row[5]),
                    toNullableProductStatus(row[6]),
                    null))
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
        .map(phone -> adminDashboardMapper.toLowStockAlert(phone, LOW_STOCK_THRESHOLD))
        .toList();
  }

  private Instant toInstant(LocalDate date) {
    return date.atStartOfDay().toInstant(ZoneOffset.UTC);
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

  private Integer toNullableInteger(Object value) {
    return value == null ? null : ((Number) value).intValue();
  }

  private ProductStatus toNullableProductStatus(Object value) {
    return value == null ? null : ProductStatus.valueOf(value.toString());
  }

  private BigDecimal calculateChangePercent(long current, long previous) {
    return calculateChangePercent(BigDecimal.valueOf(current), BigDecimal.valueOf(previous));
  }

  private BigDecimal calculateChangePercent(BigDecimal current, BigDecimal previous) {
    if (previous.compareTo(BigDecimal.ZERO) == 0) {
      return current.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : BigDecimal.valueOf(100);
    }
    return current
        .subtract(previous)
        .multiply(BigDecimal.valueOf(100))
        .divide(previous, 2, RoundingMode.HALF_UP);
  }

  private BigDecimal calculateSharePercent(BigDecimal value, BigDecimal total) {
    if (total.compareTo(BigDecimal.ZERO) == 0) {
      return BigDecimal.ZERO;
    }
    return value.multiply(BigDecimal.valueOf(100)).divide(total, 2, RoundingMode.HALF_UP);
  }
}
