package com.challengeteam.shop.service.impl;

import com.challengeteam.shop.dto.phone.response.PhoneResponseDto;
import com.challengeteam.shop.dto.phone.response.ProductBadgeResponseDto;
import com.challengeteam.shop.entity.phone.Phone;
import com.challengeteam.shop.persistence.repository.OrderItemRepository;
import com.challengeteam.shop.persistence.repository.projection.PhoneLastPurchaseProjection;
import com.challengeteam.shop.persistence.repository.projection.PhonePurchaseQuantityProjection;
import com.challengeteam.shop.service.ProductBadgeService;
import com.challengeteam.shop.service.model.ProductBadgeInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductBadgeServiceImpl implements ProductBadgeService {
    private static final int NEW_PRODUCT_DAYS = 30;
    private static final int HIT_WINDOW_START_DAY = 5;
    private static final int SALE_10_PERCENT_START_DAY = 60;
    private static final int SALE_15_PERCENT_START_DAY = 90;
    private static final int SALE_20_PERCENT_START_DAY = 120;
    private static final int SALE_END_DAY = 150;
    private static final int TEN_PERCENT_DISCOUNT = 10;
    private static final int FIFTEEN_PERCENT_DISCOUNT = 15;
    private static final int TWENTY_PERCENT_DISCOUNT = 20;

    private final OrderItemRepository orderItemRepository;
    private final Clock clock;

    /**
     * Calculates badges for the provided phones in bulk to avoid per-product purchase queries.
     */
    @Override
    public Map<Long, ProductBadgeInfo> getBadges(List<Phone> phones) {
        Objects.requireNonNull(phones, "phones");

        List<Phone> phonesWithId = phones.stream()
                .filter(phone -> phone.getId() != null)
                .toList();
        if (phonesWithId.isEmpty()) {
            return Map.of();
        }

        LocalDate today = LocalDate.now(clock);
        ZoneId zone = clock.getZone();
        List<Long> phoneIds = phonesWithId.stream()
                .map(Phone::getId)
                .toList();
        Set<Long> hitPhoneIds = findHitPhoneIds(phoneIds, today, zone);
        Map<Long, Instant> lastPurchasesByPhoneId = findLastPurchases(phoneIds);

        Map<Long, ProductBadgeInfo> badgesByPhoneId = new HashMap<>();
        for (Phone phone : phonesWithId) {
            ProductBadgeInfo productBadgeInfo = resolveBadge(phone, today, hitPhoneIds, lastPurchasesByPhoneId);
            badgesByPhoneId.put(phone.getId(), productBadgeInfo);
        }

        log.debug("Resolved product badges for {} phones", badgesByPhoneId.size());
        return badgesByPhoneId;
    }

    @Override
    public PhoneResponseDto applyBadges(PhoneResponseDto phoneResponseDto, ProductBadgeInfo productBadgeInfo) {
        Objects.requireNonNull(phoneResponseDto, "phoneResponseDto");
        ProductBadgeInfo badgeInfo = productBadgeInfo == null ? ProductBadgeInfo.empty() : productBadgeInfo;

        return new PhoneResponseDto(
                phoneResponseDto.id(),
                phoneResponseDto.name(),
                phoneResponseDto.description(),
                phoneResponseDto.price(),
                phoneResponseDto.brand(),
                phoneResponseDto.releaseYear(),
                phoneResponseDto.cpu(),
                phoneResponseDto.coresNumber(),
                phoneResponseDto.screenSize(),
                phoneResponseDto.frontCamera(),
                phoneResponseDto.mainCamera(),
                phoneResponseDto.batteryCapacity(),
                phoneResponseDto.colors(),
                phoneResponseDto.storageCapacity(),
                phoneResponseDto.images(),
                badgeInfo.badges(),
                badgeInfo.discountPercent());
    }

    @Override
    public List<PhoneResponseDto> applyBadges(List<PhoneResponseDto> phoneResponseDtos, List<Phone> phones) {
        Objects.requireNonNull(phoneResponseDtos, "phoneResponseDtos");
        Objects.requireNonNull(phones, "phones");

        Map<Long, ProductBadgeInfo> badgesByPhoneId = getBadges(phones);
        return phoneResponseDtos.stream()
                .map(phoneResponseDto -> applyBadges(phoneResponseDto, badgesByPhoneId.get(phoneResponseDto.id())))
                .toList();
    }

    private Set<Long> findHitPhoneIds(Collection<Long> phoneIds, LocalDate today, ZoneId zone) {
        if (today.getDayOfMonth() < HIT_WINDOW_START_DAY) {
            return Set.of();
        }

        Instant start = today.withDayOfMonth(HIT_WINDOW_START_DAY).atStartOfDay(zone).toInstant();
        Instant end = today.plusDays(1).atStartOfDay(zone).toInstant();
        List<PhonePurchaseQuantityProjection> purchaseQuantities =
                orderItemRepository.sumPurchasedQuantitiesByPhoneIdsBetween(phoneIds, start, end);

        long maxQuantity = purchaseQuantities.stream()
                .map(PhonePurchaseQuantityProjection::getQuantity)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElse(0L);
        if (maxQuantity <= 0L) {
            return Set.of();
        }

        return purchaseQuantities.stream()
                .filter(quantity -> Long.valueOf(maxQuantity).equals(quantity.getQuantity()))
                .map(PhonePurchaseQuantityProjection::getPhoneId)
                .collect(Collectors.toSet());
    }

    private Map<Long, Instant> findLastPurchases(Collection<Long> phoneIds) {
        return orderItemRepository.findLastPurchaseByPhoneIds(phoneIds).stream()
                .collect(Collectors.toMap(
                        PhoneLastPurchaseProjection::getPhoneId,
                        PhoneLastPurchaseProjection::getLastPurchasedAt));
    }

    private ProductBadgeInfo resolveBadge(
            Phone phone,
            LocalDate today,
            Set<Long> hitPhoneIds,
            Map<Long, Instant> lastPurchasesByPhoneId) {
        EnumSet<ProductBadgeResponseDto> badges = EnumSet.noneOf(ProductBadgeResponseDto.class);
        int discountPercent = 0;

        LocalDate createdDate = toLocalDate(phone.getCreatedAt());
        if (createdDate != null && !createdDate.isBefore(today.minusDays(NEW_PRODUCT_DAYS))) {
            badges.add(ProductBadgeResponseDto.NEW);
        }

        if (hitPhoneIds.contains(phone.getId())) {
            badges.add(ProductBadgeResponseDto.HIT);
        }

        if (!lastPurchasesByPhoneId.containsKey(phone.getId())) {
            discountPercent = resolveSaleDiscountPercent(createdDate, today);
            if (discountPercent > 0) {
                badges.add(ProductBadgeResponseDto.SALE);
            }
        }

        return new ProductBadgeInfo(Set.copyOf(badges), discountPercent);
    }

    private LocalDate toLocalDate(Instant instant) {
        return instant == null ? null : LocalDate.ofInstant(instant, clock.getZone());
    }

    private int resolveSaleDiscountPercent(LocalDate createdDate, LocalDate today) {
        if (createdDate == null) {
            return 0;
        }

        long productAgeDays = ChronoUnit.DAYS.between(createdDate, today);
        if (productAgeDays >= SALE_10_PERCENT_START_DAY && productAgeDays < SALE_15_PERCENT_START_DAY) {
            return TEN_PERCENT_DISCOUNT;
        }
        if (productAgeDays >= SALE_15_PERCENT_START_DAY && productAgeDays < SALE_20_PERCENT_START_DAY) {
            return FIFTEEN_PERCENT_DISCOUNT;
        }
        if (productAgeDays >= SALE_20_PERCENT_START_DAY && productAgeDays <= SALE_END_DAY) {
            return TWENTY_PERCENT_DISCOUNT;
        }

        return 0;
    }
}
