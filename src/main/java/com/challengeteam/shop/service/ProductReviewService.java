package com.challengeteam.shop.service;

import com.challengeteam.shop.dto.pagination.paginationResponse.PageResponseDto;
import com.challengeteam.shop.dto.phone.request.ProductReviewRequestDto;
import com.challengeteam.shop.dto.phone.response.ProductReviewResponseDto;
import org.springframework.data.domain.Pageable;

public interface ProductReviewService {

    PageResponseDto<ProductReviewResponseDto> getProductReviews(Long phoneId, Pageable pageable);

    ProductReviewResponseDto createReview(Long phoneId, Long userId, ProductReviewRequestDto request);

    ProductReviewResponseDto updateReview(Long reviewId, Long userId, ProductReviewRequestDto request);

    void deleteReview(Long reviewId, Long userId);
}
