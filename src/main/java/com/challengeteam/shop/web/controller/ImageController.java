package com.challengeteam.shop.web.controller;

import com.challengeteam.shop.dto.image.ImageDataDto;
import com.challengeteam.shop.dto.image.ImageMetadataResponseDto;
import com.challengeteam.shop.entity.image.Image;
import com.challengeteam.shop.exceptionHandling.exception.ResourceNotFoundException;
import com.challengeteam.shop.mapper.ImageMapper;
import com.challengeteam.shop.service.ImageService;
import com.challengeteam.shop.web.resolver.headerResolver.HeadersResolver;
import com.challengeteam.shop.web.resolver.headerResolver.imageHeaderResolver.ImageHeadersResolver;
import com.challengeteam.shop.web.validator.image.ImageRequestValidator;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/images")
@Tag(name = "Images")
public class ImageController {
    private final ImageHeadersResolver imageHeadersResolver;
    private final ImageRequestValidator imageRequestValidator;
    private final ImageService imageService;
    private final ImageMapper imageMapper;

    // todo: assign for documentation, that endpoint is secure with Bearer Token
    @GetMapping("/{id}")
    public ResponseEntity<Resource> getImageById(@PathVariable Long id) {
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

    // todo: assign for documentation, that endpoint is secure with Bearer Token
    @GetMapping("/{id}/metadata")
    public ResponseEntity<ImageMetadataResponseDto> getImageMetadataById(@PathVariable Long id) {
        Image image = imageService
                .getImageById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not found image with id: " + id));

        ImageMetadataResponseDto body = imageMapper.toMetadata(image);
        return ResponseEntity.ok(body);
    }

    // Temporal endpoint for testing
    // Image is going to be a part of a phone.
    // Creating images will be with creating a phone. So this controller is only for getting images.
    // todo: assign for documentation, that endpoint is secure with Bearer Token
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> uploadImageTemporalEndpoint(@RequestPart("image") MultipartFile image) {
        imageRequestValidator.validate(image);
        imageService.uploadImage(image);

        return ResponseEntity.noContent().build();
    }

}
