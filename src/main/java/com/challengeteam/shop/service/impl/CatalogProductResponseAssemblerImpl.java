package com.challengeteam.shop.service.impl;

import com.challengeteam.shop.dto.phone.response.PhoneResponseDto;
import com.challengeteam.shop.entity.phone.Phone;
import com.challengeteam.shop.entity.phone.ProductVariant;
import com.challengeteam.shop.mapper.phone.ProductVariantMapper;
import com.challengeteam.shop.mapper.phone.PhoneMapper;
import com.challengeteam.shop.persistence.repository.ProductVariantRepository;
import com.challengeteam.shop.persistence.repository.ProductReviewRepository;
import com.challengeteam.shop.persistence.repository.projection.PhoneReviewSummaryProjection;
import com.challengeteam.shop.service.CatalogProductResponseAssembler;
import com.challengeteam.shop.service.ProductBadgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CatalogProductResponseAssemblerImpl implements CatalogProductResponseAssembler {

    private final PhoneMapper phoneMapper;
    private final ProductVariantMapper productVariantMapper;
    private final ProductBadgeService productBadgeService;
    private final ProductReviewRepository productReviewRepository;
    private final ProductVariantRepository productVariantRepository;

    @Override
    public PhoneResponseDto toResponse(Phone phone) {
        Objects.requireNonNull(phone, "phone");

        return toResponses(List.of(phone)).getFirst();
    }

    @Override
    public List<PhoneResponseDto> toResponses(List<Phone> phones) {
        Objects.requireNonNull(phones, "phones");
        if (phones.isEmpty()) {
            return List.of();
        }

        List<PhoneResponseDto> responses = productBadgeService.applyBadges(phoneMapper.toResponseList(phones), phones);
        Map<Long, PhoneReviewSummaryProjection> summaries = reviewSummaries(phones);
        Map<Long, List<ProductVariant>> variantsByPhoneId = variantsByPhoneId(phones);

        return responses.stream()
                .map(response -> applyVariants(response, variantsByPhoneId.getOrDefault(response.id(), List.of())))
                .map(response -> applyReviewSummary(response, summaries.get(response.id())))
                .toList();
    }

    private Map<Long, List<ProductVariant>> variantsByPhoneId(List<Phone> phones) {
        List<Long> phoneIds = phones.stream()
                .map(Phone::getId)
                .filter(Objects::nonNull)
                .toList();
        if (phoneIds.isEmpty()) {
            return Map.of();
        }

        return productVariantRepository.findAllByPhoneIdIn(phoneIds).stream()
                .collect(Collectors.groupingBy(variant -> variant.getPhone().getId()));
    }

    private Map<Long, PhoneReviewSummaryProjection> reviewSummaries(List<Phone> phones) {
        List<Long> phoneIds = phones.stream()
                .map(Phone::getId)
                .filter(Objects::nonNull)
                .toList();
        if (phoneIds.isEmpty()) {
            return Map.of();
        }

        return productReviewRepository.findSummariesByPhoneIds(phoneIds).stream()
                .collect(Collectors.toMap(PhoneReviewSummaryProjection::getPhoneId, Function.identity()));
    }

    private PhoneResponseDto applyReviewSummary(
            PhoneResponseDto response,
            PhoneReviewSummaryProjection summary) {
        BigDecimal averageRating = summary == null || summary.getAverageRating() == null
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(summary.getAverageRating()).setScale(1, RoundingMode.HALF_UP);
        Long reviewsCount = summary == null || summary.getReviewsCount() == null ? 0L : summary.getReviewsCount();

        return new PhoneResponseDto(
                response.id(),
                response.name(),
                response.description(),
                response.price(),
                response.brand(),
                response.stock(),
                response.status(),
                response.previewImage(),
                response.releaseYear(),
                response.cpu(),
                response.coresNumber(),
                response.screenSize(),
                response.frontCamera(),
                response.mainCamera(),
                response.batteryCapacity(),
                response.colors(),
                response.storageCapacity(),
                response.variants(),
                response.images(),
                response.badges(),
                response.discountPercent(),
                averageRating,
                reviewsCount);
    }

    private PhoneResponseDto applyVariants(PhoneResponseDto response, List<ProductVariant> variants) {
        if (variants.isEmpty()) {
            return response;
        }

        BigDecimal displayPrice = variants.stream()
                .map(ProductVariant::getPrice)
                .min(BigDecimal::compareTo)
                .orElse(response.price());
        int displayStock = variants.stream()
                .mapToInt(ProductVariant::getStock)
                .sum();

        return new PhoneResponseDto(
                response.id(),
                response.name(),
                response.description(),
                displayPrice,
                response.brand(),
                displayStock,
                com.challengeteam.shop.utility.ProductStatusResolver.resolve(displayStock),
                response.previewImage(),
                response.releaseYear(),
                response.cpu(),
                response.coresNumber(),
                response.screenSize(),
                response.frontCamera(),
                response.mainCamera(),
                response.batteryCapacity(),
                response.colors(),
                response.storageCapacity(),
                productVariantMapper.toResponses(variants),
                response.images(),
                response.badges(),
                response.discountPercent(),
                response.averageRating(),
                response.reviewsCount());
    }
}
