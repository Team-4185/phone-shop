package com.challengeteam.shop.web.controller;

import com.challengeteam.shop.service.TestDataService;
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

    @PostMapping("/generate-users/{amount}")
    public ResponseEntity<Void> createUsers(@PathVariable Long amount) {
        // todo: implement
        return null;
    }

    @PostMapping("/generate-phones/{amount}")
    public ResponseEntity<Void> createPhones(@PathVariable Long amount) {
        // todo: implement
        return null;
    }

}
