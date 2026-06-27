package com.challengeteam.shop.service.impl;

import com.challengeteam.shop.dto.phone.response.PhoneResponseDto;
import com.challengeteam.shop.dto.phone.response.ProductBadgeResponseDto;
import com.challengeteam.shop.entity.phone.Phone;
import com.challengeteam.shop.persistence.repository.OrderItemRepository;
import com.challengeteam.shop.persistence.repository.projection.PhoneLastPurchaseProjection;
import com.challengeteam.shop.persistence.repository.projection.PhonePurchaseQuantityProjection;
import com.challengeteam.shop.service.model.ProductBadgeInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductBadgeServiceImplTest {
    private static final Instant NOW = Instant.parse("2026-06-20T10:00:00Z");
    private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);

    private final OrderItemRepository orderItemRepository = mock(OrderItemRepository.class);
    private final ProductBadgeServiceImpl productBadgeService =
            new ProductBadgeServiceImpl(orderItemRepository, CLOCK);

    @Test
    void whenProductCreatedWithinLastThirtyDays_thenReturnNewBadge() {
        Phone phone = buildPhone(1L, Instant.parse("2026-06-01T00:00:00Z"));
        when(orderItemRepository.sumPurchasedQuantitiesByPhoneIdsBetween(any(), any(), any()))
                .thenReturn(List.of());
        when(orderItemRepository.findLastPurchaseByPhoneIds(any()))
                .thenReturn(List.of());

        Map<Long, ProductBadgeInfo> result = productBadgeService.getBadges(List.of(phone));

        assertThat(result.get(phone.getId()).badges()).containsExactly(ProductBadgeResponseDto.NEW);
        assertThat(result.get(phone.getId()).discountPercent()).isZero();
    }

    @Test
    void whenProductHasTopMonthlyPurchaseQuantity_thenReturnHitBadge() {
        Phone firstPhone = buildPhone(1L, Instant.parse("2026-01-01T00:00:00Z"));
        Phone secondPhone = buildPhone(2L, Instant.parse("2026-01-01T00:00:00Z"));
        when(orderItemRepository.sumPurchasedQuantitiesByPhoneIdsBetween(any(), any(), any()))
                .thenReturn(List.of(purchaseQuantity(1L, 7L), purchaseQuantity(2L, 3L)));
        when(orderItemRepository.findLastPurchaseByPhoneIds(any()))
                .thenReturn(List.of(lastPurchase(1L), lastPurchase(2L)));

        Map<Long, ProductBadgeInfo> result = productBadgeService.getBadges(List.of(firstPhone, secondPhone));

        assertThat(result.get(firstPhone.getId()).badges()).containsExactly(ProductBadgeResponseDto.HIT);
        assertThat(result.get(secondPhone.getId()).badges()).isEmpty();
    }

    @Test
    void whenProductWasNeverPurchasedAndIsInSaleWindow_thenReturnSaleBadgeAndDiscount() {
        Phone phone = buildPhone(1L, Instant.parse("2026-02-01T00:00:00Z"));
        when(orderItemRepository.sumPurchasedQuantitiesByPhoneIdsBetween(any(), any(), any()))
                .thenReturn(List.of());
        when(orderItemRepository.findLastPurchaseByPhoneIds(any()))
                .thenReturn(List.of());

        Map<Long, ProductBadgeInfo> result = productBadgeService.getBadges(List.of(phone));

        assertThat(result.get(phone.getId()).badges()).containsExactly(ProductBadgeResponseDto.SALE);
        assertThat(result.get(phone.getId()).discountPercent()).isEqualTo(20);
    }

    @Test
    void whenProductIsOlderThanSaleWindow_thenRemoveSaleBadgeAndDiscount() {
        Phone phone = buildPhone(1L, Instant.parse("2026-01-01T00:00:00Z"));
        when(orderItemRepository.sumPurchasedQuantitiesByPhoneIdsBetween(any(), any(), any()))
                .thenReturn(List.of());
        when(orderItemRepository.findLastPurchaseByPhoneIds(any()))
                .thenReturn(List.of());

        Map<Long, ProductBadgeInfo> result = productBadgeService.getBadges(List.of(phone));

        assertThat(result.get(phone.getId()).badges()).isEmpty();
        assertThat(result.get(phone.getId()).discountPercent()).isZero();
    }

    @Test
    void whenApplyingBadges_thenPreservePhoneResponseData() {
        PhoneResponseDto responseDto = new PhoneResponseDto(
                1L,
                "iPhone",
                "Phone",
                new BigDecimal("999.99"),
                "Apple",
                2026,
                null,
                null,
                null,
                null,
                null,
                null,
                Set.of(),
                Set.of(),
                List.of());
        ProductBadgeInfo badgeInfo = new ProductBadgeInfo(Set.of(ProductBadgeResponseDto.SALE), 10);

        PhoneResponseDto result = productBadgeService.applyBadges(responseDto, badgeInfo);

        assertThat(result.id()).isEqualTo(responseDto.id());
        assertThat(result.badges()).containsExactly(ProductBadgeResponseDto.SALE);
        assertThat(result.discountPercent()).isEqualTo(10);
    }

    private Phone buildPhone(Long id, Instant createdAt) {
        return Phone.builder()
                .id(id)
                .createdAt(createdAt)
                .name("Phone " + id)
                .price(new BigDecimal("999.99"))
                .brand("Apple")
                .releaseYear(2026)
                .sku("SKU-" + id)
                .stock(10)
                .build();
    }

    private PhonePurchaseQuantityProjection purchaseQuantity(Long phoneId, Long quantity) {
        return new PhonePurchaseQuantityProjection() {
            @Override
            public Long getPhoneId() {
                return phoneId;
            }

            @Override
            public Long getQuantity() {
                return quantity;
            }
        };
    }

    private PhoneLastPurchaseProjection lastPurchase(Long phoneId) {
        return new PhoneLastPurchaseProjection() {
            @Override
            public Long getPhoneId() {
                return phoneId;
            }

            @Override
            public Instant getLastPurchasedAt() {
                return NOW;
            }
        };
    }
}
