package com.challengeteam.shop.web.controller.admin;

import com.challengeteam.shop.dto.admin.product.AdminProductCreateRequestDto;
import com.challengeteam.shop.dto.admin.product.AdminProductDetailsResponseDto;
import com.challengeteam.shop.dto.admin.product.AdminProductFilterDto;
import com.challengeteam.shop.dto.admin.product.AdminProductListItemResponseDto;
import com.challengeteam.shop.dto.admin.product.AdminProductUpdateRequestDto;
import com.challengeteam.shop.dto.image.ImageMetadataResponseDto;
import com.challengeteam.shop.dto.pagination.paginationRequest.PageRequestDto;
import com.challengeteam.shop.dto.pagination.paginationResponse.PageResponseDto;
import com.challengeteam.shop.entity.image.Image;
import com.challengeteam.shop.exceptionHandling.exception.InvalidPriceRangeException;
import com.challengeteam.shop.mapper.image.ImageMapper;
import com.challengeteam.shop.service.admin.AdminProductCommandService;
import com.challengeteam.shop.service.admin.AdminProductQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Admin Product Management API.
 *
 * <p>Exposes admin-only product list/details contracts plus product mutation and image-management
 * endpoints. Public catalog product endpoints intentionally use separate DTOs and controllers.
 */
@RestController
@RequestMapping("/api/v1/admin/products")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Validated
@Slf4j
public class AdminProductController {

    private final AdminProductQueryService adminProductQueryService;
    private final AdminProductCommandService adminProductCommandService;
    private final ImageMapper imageMapper;

    @Operation(
            summary = "Get admin product list",
            description =
                    "Returns a paginated list of products for admin section with admin-specific filtering and sorting.")
    @GetMapping
    public ResponseEntity<PageResponseDto<AdminProductListItemResponseDto>> getAllProducts(
            @Valid PageRequestDto pageRequestDto, @Valid AdminProductFilterDto filterDto) {
        int page = pageRequestDto.page() - 1;
        int size = pageRequestDto.size();
        log.debug("Get admin products page={} size={} filters={}", page, size, filterDto);

        if (filterDto.minPrice() != null && filterDto.maxPrice() != null && filterDto.minPrice().compareTo(filterDto.maxPrice()) > 0)
            throw new InvalidPriceRangeException("minPrice cannot be greater than maxPrice");

        Page<AdminProductListItemResponseDto> products =
                adminProductQueryService.getProducts(page, size, filterDto);

        return ResponseEntity.ok(PageResponseDto.of(products));
    }

    @Operation(
            summary = "Get admin product details by id",
            description =
                    "Returns full product details required for admin overview and future edit form.")
    @GetMapping("/{id:\\d+}")
    public ResponseEntity<AdminProductDetailsResponseDto> getProductById(@PathVariable Long id) {
        log.debug("Get admin product details id={}", id);
        return ResponseEntity.ok(adminProductQueryService.getProductById(id));
    }

    @Operation(
            summary = "Create admin product",
            description = "Creates a product for Product Management with optional product images.")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> createProduct(
            @Valid @RequestPart("product") AdminProductCreateRequestDto request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        List<MultipartFile> safeImages = images == null ? new ArrayList<>() : images;
        log.debug("Create admin product request sku={} imageCount={}", request.sku(), safeImages.size());
        Long id = adminProductCommandService.createProduct(request, safeImages);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();

        return ResponseEntity.created(location).build();
    }

    @Operation(
            summary = "Update admin product",
            description = "Partially updates product fields used by Product Management.")
    @PutMapping("/{id:\\d+}")
    public ResponseEntity<Void> updateProduct(
            @PathVariable Long id, @Valid @RequestBody AdminProductUpdateRequestDto request) {
        log.debug("Update admin product request id={}", id);
        adminProductCommandService.updateProduct(id, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Delete admin product", description = "Deletes a product by id.")
    @DeleteMapping("/{id:\\d+}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        log.debug("Delete admin product request id={}", id);
        adminProductCommandService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Get admin product images",
            description = "Returns image metadata for a product.")
    @GetMapping("/{id:\\d+}/images")
    public ResponseEntity<List<ImageMetadataResponseDto>> getProductImages(@PathVariable Long id) {
        log.debug("Get admin product images request id={}", id);
        List<Image> images = adminProductCommandService.getProductImages(id);
        return ResponseEntity.ok(imageMapper.toListOfMetadata(images));
    }

    @Operation(
            summary = "Add admin product images",
            description = "Adds one or more images to a product.")
    @PostMapping(value = "/{id:\\d+}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> addProductImages(
            @PathVariable Long id, @RequestPart("images") List<MultipartFile> images) {
        log.debug("Add admin product images request id={} imageCount={}", id, images.size());
        adminProductCommandService.addProductImages(id, images);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Delete admin product image", description = "Deletes a product image by id.")
    @DeleteMapping("/{id:\\d+}/images/{imageId:\\d+}")
    public ResponseEntity<Void> deleteProductImage(@PathVariable Long id, @PathVariable Long imageId) {
        log.debug("Delete admin product image request productId={} imageId={}", id, imageId);
        adminProductCommandService.deleteProductImage(id, imageId);
        return ResponseEntity.noContent().build();
    }
}
