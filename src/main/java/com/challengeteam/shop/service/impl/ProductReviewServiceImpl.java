package com.challengeteam.shop.service.impl;

import com.challengeteam.shop.dto.pagination.paginationResponse.PageResponseDto;
import com.challengeteam.shop.dto.phone.request.ProductReviewRequestDto;
import com.challengeteam.shop.dto.phone.response.ProductReviewResponseDto;
import com.challengeteam.shop.entity.phone.Phone;
import com.challengeteam.shop.entity.review.ProductReview;
import com.challengeteam.shop.entity.user.User;
import com.challengeteam.shop.exceptionHandling.exception.InvalidAPIRequestException;
import com.challengeteam.shop.exceptionHandling.exception.ResourceNotFoundException;
import com.challengeteam.shop.mapper.phone.ProductReviewMapper;
import com.challengeteam.shop.persistence.repository.PhoneRepository;
import com.challengeteam.shop.persistence.repository.ProductReviewRepository;
import com.challengeteam.shop.persistence.repository.UserRepository;
import com.challengeteam.shop.service.ProductReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductReviewServiceImpl implements ProductReviewService {

    private final ProductReviewRepository productReviewRepository;
    private final PhoneRepository phoneRepository;
    private final UserRepository userRepository;
    private final ProductReviewMapper productReviewMapper;

    @Transactional(readOnly = true)
    @Override
    public PageResponseDto<ProductReviewResponseDto> getProductReviews(Long phoneId, Pageable pageable) {
        Objects.requireNonNull(phoneId, "phoneId");
        Objects.requireNonNull(pageable, "pageable");

        ensurePhoneExists(phoneId);
        Page<ProductReviewResponseDto> reviews = productReviewRepository.findAllByPhoneId(phoneId, pageable)
                .map(productReviewMapper::toResponse);

        log.debug("Found {} reviews for phoneId={}", reviews.getTotalElements(), phoneId);
        return PageResponseDto.of(reviews);
    }

    @Transactional
    @Override
    public ProductReviewResponseDto createReview(Long phoneId, Long userId, ProductReviewRequestDto request) {
        Objects.requireNonNull(phoneId, "phoneId");
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(request, "request");

        if (productReviewRepository.existsByPhoneIdAndUserId(phoneId, userId)) {
            throw new InvalidAPIRequestException("User has already reviewed this product");
        }

        Phone phone = phoneRepository.findById(phoneId)
                .orElseThrow(() -> new ResourceNotFoundException("Phone with id " + phoneId + " not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + userId + " not found"));

        ProductReview review = productReviewRepository.save(ProductReview.builder()
                .phone(phone)
                .user(user)
                .rating(request.rating())
                .comment(normalizeComment(request.comment()))
                .build());

        log.debug("Created product review id={} for phoneId={} by userId={}", review.getId(), phoneId, userId);
        return productReviewMapper.toResponse(review);
    }

    @Transactional
    @Override
    public ProductReviewResponseDto updateReview(Long reviewId, Long userId, ProductReviewRequestDto request) {
        Objects.requireNonNull(reviewId, "reviewId");
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(request, "request");

        ProductReview review = findOwnReview(reviewId, userId);
        review.setRating(request.rating());
        review.setComment(normalizeComment(request.comment()));

        log.debug("Updated product review id={} by userId={}", reviewId, userId);
        return productReviewMapper.toResponse(productReviewRepository.save(review));
    }

    @Transactional
    @Override
    public void deleteReview(Long reviewId, Long userId) {
        Objects.requireNonNull(reviewId, "reviewId");
        Objects.requireNonNull(userId, "userId");

        ProductReview review = findOwnReview(reviewId, userId);
        productReviewRepository.delete(review);
        log.debug("Deleted product review id={} by userId={}", reviewId, userId);
    }

    private void ensurePhoneExists(Long phoneId) {
        if (!phoneRepository.existsById(phoneId)) {
            throw new ResourceNotFoundException("Phone with id " + phoneId + " not found");
        }
    }

    private ProductReview findOwnReview(Long reviewId, Long userId) {
        return productReviewRepository.findByIdAndUserId(reviewId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Review with id " + reviewId + " not found"));
    }

    private String normalizeComment(String comment) {
        if (comment == null) {
            return null;
        }

        String trimmed = comment.trim();
        return trimmed.isBlank() ? null : trimmed;
    }
}
