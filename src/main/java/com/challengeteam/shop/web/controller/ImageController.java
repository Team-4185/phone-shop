package com.challengeteam.shop.web.controller;

import com.challengeteam.shop.dto.image.ImageDataDto;
import com.challengeteam.shop.dto.image.ImageMetadataResponseDto;
import com.challengeteam.shop.entity.image.Image;
import com.challengeteam.shop.exceptionHandling.exception.ResourceNotFoundException;
import com.challengeteam.shop.mapper.ImageMapper;
import com.challengeteam.shop.service.ImageService;
import com.challengeteam.shop.web.resolver.headerResolver.imageHeaderResolver.ImageHeadersResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/images")
@SecurityRequirement(name = "bearer-jwt")
public class ImageController {
    private final ImageHeadersResolver imageHeadersResolver;
    private final ImageService imageService;
    private final ImageMapper imageMapper;


    @Operation(
            summary = "Endpoint for retrieving in-line image",
            description = "Returns an image as a byte array by id. No additional information is returned."
    )
    @GetMapping("/{id}")
    public ResponseEntity<Resource> getImage(@PathVariable Long id) {
        ImageDataDto imageDataDto = imageService
                .downloadImageById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not found image with id: " + id));

        HttpHeaders headers = imageHeadersResolver.resolveHeaders(imageDataDto);
        Resource body = imageMapper.toResource(imageDataDto);
        return ResponseEntity
                .ok()
                .headers(headers)
                .body(body);
    }

    @Operation(
            summary = "Endpoint for retrieving image's metadata",
            description = "Returns data with common fields about image by id." +
                          " Also returns an URL for retrieving image in-line."
    )
    @GetMapping("/{id}/metadata")
    public ResponseEntity<ImageMetadataResponseDto> getImageMetadata(@PathVariable Long id) {
        Image image = imageService
                .getImageById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not found image with id: " + id));

        ImageMetadataResponseDto body = imageMapper.toMetadata(image);
        return ResponseEntity.ok(body);
    }

}
