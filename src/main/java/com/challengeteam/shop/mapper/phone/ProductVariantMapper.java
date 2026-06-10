package com.challengeteam.shop.mapper.phone;

import com.challengeteam.shop.dto.phone.response.ProductVariantResponseDto;
import com.challengeteam.shop.entity.phone.ProductVariant;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ProductVariantMapper {

  public ProductVariantResponseDto toResponse(ProductVariant variant) {
    if (variant == null) {
      return null;
    }
    return new ProductVariantResponseDto(
        variant.getId(),
        variant.getSku(),
        variant.getColor(),
        variant.getStorageCapacity(),
        variant.getPrice(),
        variant.getStock(),
        variant.getStatus());
  }

  public List<ProductVariantResponseDto> toResponses(List<ProductVariant> variants) {
    if (variants == null || variants.isEmpty()) {
      return List.of();
    }
    return variants.stream().map(this::toResponse).toList();
  }
}
