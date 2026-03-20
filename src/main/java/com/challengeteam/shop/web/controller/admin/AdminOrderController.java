package com.challengeteam.shop.web.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/orders")
@SecurityRequirement(name = "bearer-jwt")
public class AdminOrderController {

  @Operation(
      summary = "Get admin orders list",
      description = "Order admin section is prepared, but business logic is not implemented yet.")
  @GetMapping
  public ResponseEntity<Void> getAllOrders() {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
  }

  @Operation(
      summary = "Get admin order by id",
      description = "Order admin section is prepared, but business logic is not implemented yet.")
  @GetMapping("/{id:\\d+}")
  public ResponseEntity<Void> getOrderById(@PathVariable Long id) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
  }
}
