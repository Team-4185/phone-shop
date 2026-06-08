package com.challengeteam.shop.service.impl;

import com.challengeteam.shop.dto.phone.response.CatalogFilterMetadataResponseDto;
import com.challengeteam.shop.dto.phone.response.CatalogPriceRangeResponseDto;
import com.challengeteam.shop.dto.phone.response.PhoneColorResponseDto;
import com.challengeteam.shop.dto.phone.response.ProductBadgeResponseDto;
import com.challengeteam.shop.dto.phone.response.StorageCapacityResponseDto;
import com.challengeteam.shop.entity.phone.PhoneColor;
import com.challengeteam.shop.entity.phone.StorageCapacity;
import com.challengeteam.shop.persistence.repository.PhoneRepository;
import com.challengeteam.shop.service.CatalogFilterMetadataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CatalogFilterMetadataServiceImpl implements CatalogFilterMetadataService {

    private static final List<String> STOCK_OPTIONS = List.of("IN_STOCK", "PREORDER");

    private final PhoneRepository phoneRepository;

    @Transactional(readOnly = true)
    @Override
    public CatalogFilterMetadataResponseDto getMetadata() {
        BigDecimal minPrice = phoneRepository.findCatalogMinPrice().orElse(BigDecimal.ZERO);
        BigDecimal maxPrice = phoneRepository.findCatalogMaxPrice().orElse(BigDecimal.ZERO);

        return new CatalogFilterMetadataResponseDto(
                phoneRepository.findDistinctBrands(),
                phoneRepository.findDistinctColors().stream()
                        .map(this::toColorDto)
                        .toList(),
                phoneRepository.findDistinctStorageCapacities().stream()
                        .map(this::toStorageCapacityDto)
                        .toList(),
                new CatalogPriceRangeResponseDto(minPrice, maxPrice),
                STOCK_OPTIONS,
                Arrays.asList(ProductBadgeResponseDto.values()));
    }

    private PhoneColorResponseDto toColorDto(PhoneColor color) {
        return new PhoneColorResponseDto(color.name(), color.getDisplayName(), color.getHexCode());
    }

    private StorageCapacityResponseDto toStorageCapacityDto(StorageCapacity capacity) {
        return new StorageCapacityResponseDto(capacity.name(), capacity.getValue(), capacity.getUnit());
    }
}
