package com.challengeteam.shop.web.controller.admin;

import com.challengeteam.shop.dto.admin.AdminEntryResponseDto;
import com.challengeteam.shop.dto.admin.AdminSectionResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@SecurityRequirement(name = "bearer-jwt")
public class AdminController {

  @Operation(
      summary = "Get admin API entry point",
      description = "Returns available admin API sections.")
  @GetMapping
  public ResponseEntity<AdminEntryResponseDto> getAdminEntryPoint() {
    AdminEntryResponseDto response =
        new AdminEntryResponseDto(
            List.of(
                new AdminSectionResponseDto("products", "/api/v1/admin/products", true),
                new AdminSectionResponseDto("orders", "/api/v1/admin/orders", true)));

    return ResponseEntity.ok(response);
  }
}
