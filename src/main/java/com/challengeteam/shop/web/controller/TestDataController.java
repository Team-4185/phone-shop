package com.challengeteam.shop.web.controller;

import com.challengeteam.shop.dto.testData.TestDataResponse;
import com.challengeteam.shop.service.TestDataService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/test-data")
public class TestDataController {
    private final TestDataService testDataService;

    @Operation(
            deprecated = true,
            summary = "temporal: Endpoint for creating fake users",
            description = "Creates amount of fake users. This endpoint is going to be removed" +
                          " in the future. There is a limit to create at the same time: 1_000."
    )
    @PostMapping("/generate-users/{amount}")
    public ResponseEntity<TestDataResponse> createUsers(@PathVariable int amount) {
        int createdCount = testDataService.generateUsers(amount);
        var response = new TestDataResponse(createdCount, "users");

        return ResponseEntity.ok(response);
    }

    @Operation(
            deprecated = true,
            summary = "temporal: Endpoint for creating fake phones",
            description = "Creates amount of fake phones. This endpoint is going to be removed" +
                          " in the future. There is a limit to create at the same time: 10_000."
    )
    @PostMapping("/generate-phones/{amount}")
    public ResponseEntity<TestDataResponse> createPhones(@PathVariable int amount) {
        int createdCount = testDataService.generatePhones(amount);
        var response = new TestDataResponse(createdCount, "phones");

        return ResponseEntity.ok(response);
    }

}