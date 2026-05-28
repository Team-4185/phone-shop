package com.challengeteam.shop.web.controller.admin;

import com.challengeteam.shop.dto.admin.order.AdminOrderDetailsResponseDto;
import com.challengeteam.shop.dto.admin.order.AdminOrderFilterDto;
import com.challengeteam.shop.dto.admin.order.AdminOrderKpiResponseDto;
import com.challengeteam.shop.dto.admin.order.AdminOrderListItemResponseDto;
import com.challengeteam.shop.dto.pagination.paginationRequest.PageRequestDto;
import com.challengeteam.shop.dto.pagination.paginationResponse.PageResponseDto;
import com.challengeteam.shop.service.admin.AdminOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST API for the admin Order Management section. */
@RestController
@RequestMapping("/api/v1/admin/orders")
@SecurityRequirement(name = "bearer-jwt")
@RequiredArgsConstructor
@Slf4j
@Validated
public class AdminOrderController {

  private final AdminOrderService adminOrderService;

  @Operation(
      summary = "Get admin orders list",
      description = "Returns paginated orders for admin Order Management with filters and sorting.")
  @GetMapping
  public ResponseEntity<PageResponseDto<AdminOrderListItemResponseDto>> getAllOrders(
      @Valid PageRequestDto pageRequestDto, @Valid AdminOrderFilterDto filterDto) {
    int page = pageRequestDto.page() - 1;
    int size = pageRequestDto.size();

    log.debug("Admin order list request page={} size={} filters={}", page, size, filterDto);
    Page<AdminOrderListItemResponseDto> orders = adminOrderService.getOrders(page, size, filterDto);

    return ResponseEntity.ok(PageResponseDto.of(orders));
  }

  @Operation(
      summary = "Get admin order KPI cards",
      description = "Returns order counts required for admin Order Management KPI cards.")
  @GetMapping("/kpi")
  public ResponseEntity<AdminOrderKpiResponseDto> getOrderKpi() {
    log.debug("Admin order KPI request");
    return ResponseEntity.ok(adminOrderService.getOrderKpi());
  }

  @Operation(
      summary = "Get admin order by id",
      description = "Returns full order details for admin Order Management.")
  @GetMapping("/{id:\\d+}")
  public ResponseEntity<AdminOrderDetailsResponseDto> getOrderById(@PathVariable Long id) {
    log.debug("Admin order details request id={}", id);
    return ResponseEntity.ok(adminOrderService.getOrderById(id));
  }

  @Operation(summary = "Confirm admin order", description = "Moves an order from NEW to CONFIRMED.")
  @PostMapping("/{id:\\d+}/confirm")
  public ResponseEntity<AdminOrderDetailsResponseDto> confirmOrder(@PathVariable Long id) {
    return applyAction(id, "confirm");
  }

  @Operation(
      summary = "Process admin order",
      description = "Moves an order from CONFIRMED to PROCESSING.")
  @PostMapping("/{id:\\d+}/process")
  public ResponseEntity<AdminOrderDetailsResponseDto> processOrder(@PathVariable Long id) {
    return applyAction(id, "process");
  }

  @Operation(summary = "Ship admin order", description = "Moves an order from PROCESSING to SHIPPED.")
  @PostMapping("/{id:\\d+}/ship")
  public ResponseEntity<AdminOrderDetailsResponseDto> shipOrder(@PathVariable Long id) {
    return applyAction(id, "ship");
  }

  @Operation(summary = "Deliver admin order", description = "Moves an order from SHIPPED to DELIVERED.")
  @PostMapping("/{id:\\d+}/deliver")
  public ResponseEntity<AdminOrderDetailsResponseDto> deliverOrder(@PathVariable Long id) {
    return applyAction(id, "deliver");
  }

  @Operation(summary = "Cancel admin order", description = "Cancels an order before delivery.")
  @PostMapping("/{id:\\d+}/cancel")
  public ResponseEntity<AdminOrderDetailsResponseDto> cancelOrder(@PathVariable Long id) {
    return applyAction(id, "cancel");
  }

  private ResponseEntity<AdminOrderDetailsResponseDto> applyAction(Long id, String action) {
    log.debug("Admin order action request id={} action={}", id, action);
    return ResponseEntity.ok(adminOrderService.applyAction(id, action));
  }
}
