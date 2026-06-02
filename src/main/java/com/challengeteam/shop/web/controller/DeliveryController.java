package com.challengeteam.shop.web.controller;

import com.challengeteam.shop.dto.delivery.DeliveryProviderResponseDto;
import com.challengeteam.shop.dto.delivery.PickupPointResponseDto;
import com.challengeteam.shop.service.delivery.DeliveryService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/delivery")
@RequiredArgsConstructor
@Validated
public class DeliveryController {
    private final DeliveryService deliveryService;

    @Operation(summary = "Get delivery providers")
    @GetMapping("/providers")
    public ResponseEntity<List<DeliveryProviderResponseDto>> getProviders() {
        return ResponseEntity.ok(deliveryService.getProviders());
    }

    @Operation(summary = "Get pickup or post office points by city and provider")
    @GetMapping("/pickup-points")
    public ResponseEntity<List<PickupPointResponseDto>> getPickupPoints(
            @RequestParam(defaultValue = "mock") String provider,
            @RequestParam String city) {
        return ResponseEntity.ok(deliveryService.getPickupPoints(provider, city));
    }
}
