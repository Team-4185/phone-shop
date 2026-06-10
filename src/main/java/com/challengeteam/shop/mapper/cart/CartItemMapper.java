package com.challengeteam.shop.mapper.cart;

import com.challengeteam.shop.dto.cart.CartItemResponseDto;
import com.challengeteam.shop.dto.image.ImageMetadataResponseDto;
import com.challengeteam.shop.entity.cart.CartItem;
import com.challengeteam.shop.mapper.image.ImageMapper;
import com.challengeteam.shop.mapper.phone.ProductVariantMapper;
import com.challengeteam.shop.persistence.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class CartItemMapper {
    private final ImageMapper imageMapper;
    private final ProductVariantMapper productVariantMapper;
    private final ImageRepository imageRepository;

    public CartItemResponseDto toCartItemResponseDto(CartItem cartItem) {
        if (cartItem == null) {
            return null;
        }
        BigDecimal price = cartItem.getVariant() == null
                ? cartItem.getPhone().getPrice()
                : cartItem.getVariant().getPrice();
        BigDecimal lineTotal = price.multiply(BigDecimal.valueOf(cartItem.getAmount()));
        return new CartItemResponseDto(
                cartItem.getPhone().getId(),
                cartItem.getVariant() == null ? null : cartItem.getVariant().getId(),
                productVariantMapper.toResponse(cartItem.getVariant()),
                cartItem.getPhone().getName(),
                cartItem.getPhone().getBrand(),
                price,
                previewImage(cartItem),
                cartItem.getVariant() == null ? cartItem.getPhone().getStock() : cartItem.getVariant().getStock(),
                cartItem.getVariant() == null ? cartItem.getPhone().getStatus() : cartItem.getVariant().getStatus(),
                cartItem.getAmount(),
                cartItem.getAmount(),
                lineTotal
        );
    }

    private ImageMetadataResponseDto previewImage(CartItem cartItem) {
        return imageRepository.findFirstByPhone_IdOrderByIdAsc(cartItem.getPhone().getId())
                .map(imageMapper::toMetadata)
                .orElse(null);
    }

}
