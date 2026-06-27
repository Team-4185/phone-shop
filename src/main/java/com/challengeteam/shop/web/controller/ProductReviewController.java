package com.challengeteam.shop.web.controller;

import com.challengeteam.shop.dto.pagination.paginationResponse.PageResponseDto;
import com.challengeteam.shop.dto.phone.request.ProductReviewRequestDto;
import com.challengeteam.shop.dto.phone.response.ProductReviewResponseDto;
import com.challengeteam.shop.security.SimpleUserDetailsService.SimpleUserDetails;
import com.challengeteam.shop.service.ProductReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/phones/{phoneId:\\d+}/reviews")
@RequiredArgsConstructor
@Validated
public class ProductReviewController {

    private final ProductReviewService productReviewService;

    @Operation(summary = "Get product reviews", description = "Returns paginated public reviews for a phone.")
    @GetMapping
    public ResponseEntity<PageResponseDto<ProductReviewResponseDto>> getProductReviews(
            @PathVariable Long phoneId,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size) {
        return ResponseEntity.ok(productReviewService.getProductReviews(phoneId, PageRequest.of(page - 1, size)));
    }

    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Create own product review", description = "Creates one review per authenticated user.")
    @PostMapping
    public ResponseEntity<ProductReviewResponseDto> createReview(
            @PathVariable Long phoneId,
            @AuthenticationPrincipal SimpleUserDetails simpleUserDetails,
            @Valid @RequestBody ProductReviewRequestDto request) {
        return ResponseEntity.ok(productReviewService.createReview(
                phoneId,
                simpleUserDetails.getUserId(),
                request));
    }

    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Update own product review", description = "Updates the authenticated user's review.")
    @PatchMapping("/{reviewId:\\d+}")
    public ResponseEntity<ProductReviewResponseDto> updateReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal SimpleUserDetails simpleUserDetails,
            @Valid @RequestBody ProductReviewRequestDto request) {
        return ResponseEntity.ok(productReviewService.updateReview(
                reviewId,
                simpleUserDetails.getUserId(),
                request));
    }

    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Delete own product review", description = "Deletes the authenticated user's review.")
    @DeleteMapping("/{reviewId:\\d+}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal SimpleUserDetails simpleUserDetails) {
        productReviewService.deleteReview(reviewId, simpleUserDetails.getUserId());
        return ResponseEntity.noContent().build();
    }
}
