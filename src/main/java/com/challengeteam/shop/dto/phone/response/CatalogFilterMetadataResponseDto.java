package com.challengeteam.shop.dto.phone.response;

import java.util.List;

public record CatalogFilterMetadataResponseDto(
        List<String> brands,
        List<PhoneColorResponseDto> colors,
        List<StorageCapacityResponseDto> storageCapacities,
        CatalogPriceRangeResponseDto priceRange,
        List<String> stockOptions,
        List<ProductBadgeResponseDto> badges
) {
}
