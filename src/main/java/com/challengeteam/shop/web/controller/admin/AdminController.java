package com.challengeteam.shop.web.controller.admin;

import com.challengeteam.shop.dto.admin.AdminEntryResponseDto;
import com.challengeteam.shop.dto.admin.AdminSectionResponseDto;
import com.challengeteam.shop.dto.admin.AdminSidebarCountersResponseDto;
import com.challengeteam.shop.service.admin.AdminSidebarCountersService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@SecurityRequirement(name = "bearer-jwt")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

  private final AdminSidebarCountersService adminSidebarCountersService;

  @Operation(
      summary = "Get admin API entry point",
      description = "Returns available admin API sections.")
  @GetMapping
  public ResponseEntity<AdminEntryResponseDto> getAdminEntryPoint() {
    log.debug("Admin entry point request");
    AdminEntryResponseDto response =
        new AdminEntryResponseDto(
            List.of(
                new AdminSectionResponseDto("products", "/api/v1/admin/products", true),
                new AdminSectionResponseDto("orders", "/api/v1/admin/orders", true),
                new AdminSectionResponseDto("customers", "/api/v1/admin/customers", true),
                new AdminSectionResponseDto("dashboard", "/api/v1/admin/dashboard", true)));

    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "Get admin sidebar counters",
      description = "Returns counters required for admin sidebar navigation badges.")
  @GetMapping("/sidebar-counters")
  public ResponseEntity<AdminSidebarCountersResponseDto> getSidebarCounters() {
    log.debug("Admin sidebar counters request");
    return ResponseEntity.ok(adminSidebarCountersService.getSidebarCounters());
  }
}
