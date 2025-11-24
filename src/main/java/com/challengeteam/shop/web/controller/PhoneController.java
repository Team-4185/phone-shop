package com.challengeteam.shop.web.controller;


import com.challengeteam.shop.dto.image.ImageMetadataResponseDto;
import com.challengeteam.shop.dto.pagination.PageResponseDto;
import com.challengeteam.shop.dto.phone.PhoneCreateRequestDto;
import com.challengeteam.shop.dto.phone.PhoneResponseDto;
import com.challengeteam.shop.dto.phone.PhoneUpdateRequestDto;
import com.challengeteam.shop.entity.image.Image;
import com.challengeteam.shop.entity.phone.Phone;
import com.challengeteam.shop.exceptionHandling.exception.ResourceNotFoundException;
import com.challengeteam.shop.mapper.ImageMapper;
import com.challengeteam.shop.mapper.PhoneMapper;
import com.challengeteam.shop.service.PhoneService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

@RestController
@RequestMapping("/api/v1/phones")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Validated
public class PhoneController {
    private final PhoneService phoneService;
    private final PhoneMapper phoneMapper;
    private final ImageMapper imageMapper;


    @Operation(
            summary = "Get paginated list of phones",
            description = "Returns a paginated list of phones. " +
                          "Use 'page' and 'size' query parameters to control pagination."
    )
    @GetMapping
    public ResponseEntity<PageResponseDto<PhoneResponseDto>> getAllPhones(
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "Page must be >= 0") int page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "Size must be >= 1") int size
    ) {
        Page<Phone> phones = phoneService.getPhones(page, size);
        Page<PhoneResponseDto> response = phones.map(phoneMapper::toResponse);
        return ResponseEntity.ok(PageResponseDto.of(response));
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
            description = "Creates a new phone based on input data and also adds provided images. " +
                          "Images are optional."
    )
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> createPhone(@Valid @RequestPart("phone") PhoneCreateRequestDto phoneCreateRequestDto,
                                            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        images = images != null ? images : new ArrayList<>();

        Long id = phoneService.create(phoneCreateRequestDto, images);
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
                                            @Valid @RequestBody PhoneUpdateRequestDto phoneUpdateRequestDto) {
        phoneService.update(id, phoneUpdateRequestDto);

        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Delete phone by id",
            description = "Deletes phone by id."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePhone(@PathVariable Long id) {
        phoneService.delete(id);

        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Get phone's images",
            description = "Returns all images of specified phone."
    )
    @GetMapping("/{id}/images")
    public ResponseEntity<List<ImageMetadataResponseDto>> getPhoneImages(@PathVariable Long id) {
        List<Image> images = phoneService.getPhoneImages(id);
        List<ImageMetadataResponseDto> response = imageMapper.toListOfMetadata(images);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Add image for phone",
            description = "Adds one image for specified phone."
    )
    @PostMapping(value = "/{id}/add-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> addImageToPhone(@PathVariable Long id,
                                                @RequestParam("image") MultipartFile image) {
        phoneService.addImageToPhone(id, image);
        return ResponseEntity
                .noContent()
                .build();
    }

}
