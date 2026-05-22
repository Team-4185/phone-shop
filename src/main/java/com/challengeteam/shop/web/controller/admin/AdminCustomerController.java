package com.challengeteam.shop.web.controller.admin;

import com.challengeteam.shop.dto.admin.customer.AdminCustomerDetailsResponseDto;
import com.challengeteam.shop.dto.admin.customer.AdminCustomerFilterDto;
import com.challengeteam.shop.dto.admin.customer.AdminCustomerListItemResponseDto;
import com.challengeteam.shop.dto.pagination.paginationRequest.PageRequestDto;
import com.challengeteam.shop.dto.pagination.paginationResponse.PageResponseDto;
import com.challengeteam.shop.service.admin.AdminCustomerService;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST API for the admin Customers section. */
@RestController
@RequestMapping("/api/v1/admin/customers")
@SecurityRequirement(name = "bearer-jwt")
@RequiredArgsConstructor
@Validated
@Slf4j
public class AdminCustomerController {

  private final AdminCustomerService adminCustomerService;

  @Operation(
      summary = "Get admin customers list",
      description = "Returns paginated customers with purchase metrics and derived status.")
  @GetMapping
  public ResponseEntity<PageResponseDto<AdminCustomerListItemResponseDto>> getAllCustomers(
      @Valid PageRequestDto pageRequestDto, @Valid AdminCustomerFilterDto filterDto) {
    int page = pageRequestDto.page() - 1;
    int size = pageRequestDto.size();

    log.debug("Admin customers list request page={} size={} filters={}", page, size, filterDto);
    Page<AdminCustomerListItemResponseDto> customers =
        adminCustomerService.getCustomers(page, size, filterDto);

    return ResponseEntity.ok(PageResponseDto.of(customers));
  }

  @Operation(
      summary = "Get admin customer details",
      description = "Returns customer profile and recent purchase history for admin section.")
  @GetMapping("/{id:\\d+}")
  public ResponseEntity<AdminCustomerDetailsResponseDto> getCustomerById(@PathVariable Long id) {
    log.debug("Admin customer details request id={}", id);
    return ResponseEntity.ok(adminCustomerService.getCustomerById(id));
  }
}
