package com.challengeteam.shop.web.controller;

import com.challengeteam.shop.dto.phone.PhoneCreateRequestDto;
import com.challengeteam.shop.dto.phone.PhoneResponseDto;
import com.challengeteam.shop.dto.phone.PhoneUpdateRequestDto;
import com.challengeteam.shop.entity.phone.Phone;
import com.challengeteam.shop.exceptionHandling.exception.ResourceNotFoundException;
import com.challengeteam.shop.mapper.PhoneMapper;
import com.challengeteam.shop.service.PhoneService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/phones")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
public class PhoneController {

    private final PhoneService phoneService;

    private final PhoneMapper phoneMapper;

    @Operation(
            summary = "Get paginated list of phones",
            description = "Returns a paginated list of phones. " +
                    "Use 'page' and 'size' query parameters to control pagination."
    )
    @GetMapping
    public ResponseEntity<Page<PhoneResponseDto>> getAllPhones(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<Phone> phones = phoneService.getAllPhones(page, size);
        Page<PhoneResponseDto> responses = phones.map(phoneMapper::toResponse);

        return ResponseEntity.ok(responses);
    }

    @Operation(
            summary = "Get phone by id",
            description = "Returns a phone by id."
    )
    @GetMapping("/{id}")
    public ResponseEntity<PhoneResponseDto> getPhoneById(@PathVariable Long id) {
        Phone phone = phoneService
                .getById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not found phone with id: " + id));
        PhoneResponseDto response = phoneMapper.toResponse(phone);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Create new phone",
            description = "Creates a new phone based on input data."
    )
    @PostMapping
    public ResponseEntity<Void> createPhone(@RequestBody PhoneCreateRequestDto phoneCreateRequestDto) {
        Long id = phoneService.create(phoneCreateRequestDto);
        URI newPhoneLocation = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/v1/phones/{id}")
                .buildAndExpand(id)
                .toUri();

        return ResponseEntity.created(newPhoneLocation).build();
    }

    @Operation(
            summary = "Update phone by id",
            description = "Updates phone by id, based on input data. Where field is empty," +
                          " there will be no changes in this field."
    )
    @PutMapping("/{id}")
    public ResponseEntity<Void> updatePhone(@PathVariable Long id,
                                            @RequestBody PhoneUpdateRequestDto phoneUpdateRequestDto) {
        phoneService.update(id, phoneUpdateRequestDto);

        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Delete phone by id",
            description = "Deletes phone by id"
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePhone(@PathVariable Long id) {
        phoneService.delete(id);

        return ResponseEntity.noContent().build();
    }

}
