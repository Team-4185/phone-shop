package com.challengeteam.shop.web.controller.admin;

import com.challengeteam.shop.dto.admin.dashboard.AdminDashboardLowStockAlertResponseDto;
import com.challengeteam.shop.dto.admin.dashboard.AdminDashboardRecentOrderResponseDto;
import com.challengeteam.shop.dto.admin.dashboard.AdminDashboardSalesByBrandResponseDto;
import com.challengeteam.shop.dto.admin.dashboard.AdminDashboardSalesPointResponseDto;
import com.challengeteam.shop.dto.admin.dashboard.AdminDashboardSummaryResponseDto;
import com.challengeteam.shop.dto.admin.dashboard.AdminDashboardTopProductResponseDto;
import com.challengeteam.shop.service.admin.AdminDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST API for the admin Dashboard section. */
@RestController
@RequestMapping("/api/v1/admin/dashboard")
@SecurityRequirement(name = "bearer-jwt")
@RequiredArgsConstructor
@Slf4j
public class AdminDashboardController {

  private final AdminDashboardService adminDashboardService;

  @Operation(summary = "Get dashboard summary cards")
  @GetMapping("/summary")
  public ResponseEntity<AdminDashboardSummaryResponseDto> getSummary() {
    log.debug("Admin dashboard summary request");
    return ResponseEntity.ok(adminDashboardService.getSummary());
  }

  @Operation(summary = "Get dashboard sales analytics chart")
  @GetMapping("/sales-analytics")
  public ResponseEntity<List<AdminDashboardSalesPointResponseDto>> getSalesAnalytics() {
    log.debug("Admin dashboard sales analytics request");
    return ResponseEntity.ok(adminDashboardService.getSalesAnalytics());
  }

  @Operation(summary = "Get dashboard sales by brand")
  @GetMapping("/sales-by-brand")
  public ResponseEntity<List<AdminDashboardSalesByBrandResponseDto>> getSalesByBrand() {
    log.debug("Admin dashboard sales by brand request");
    return ResponseEntity.ok(adminDashboardService.getSalesByBrand());
  }

  @Operation(summary = "Get dashboard top selling products")
  @GetMapping("/top-selling-products")
  public ResponseEntity<List<AdminDashboardTopProductResponseDto>> getTopSellingProducts() {
    log.debug("Admin dashboard top selling products request");
    return ResponseEntity.ok(adminDashboardService.getTopSellingProducts());
  }

  @Operation(summary = "Get dashboard recent orders")
  @GetMapping("/recent-orders")
  public ResponseEntity<List<AdminDashboardRecentOrderResponseDto>> getRecentOrders() {
    log.debug("Admin dashboard recent orders request");
    return ResponseEntity.ok(adminDashboardService.getRecentOrders());
  }

  @Operation(summary = "Get dashboard low stock alerts")
  @GetMapping("/low-stock-alerts")
  public ResponseEntity<List<AdminDashboardLowStockAlertResponseDto>> getLowStockAlerts() {
    log.debug("Admin dashboard low stock alerts request");
    return ResponseEntity.ok(adminDashboardService.getLowStockAlerts());
  }
}
