package com.challengeteam.shop.mapper.cart;

import com.challengeteam.shop.dto.cart.CartItemResponseDto;
import com.challengeteam.shop.dto.image.ImageMetadataResponseDto;
import com.challengeteam.shop.entity.cart.CartItem;
import com.challengeteam.shop.mapper.image.ImageMapper;
import com.challengeteam.shop.persistence.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class CartItemMapper {
    private final ImageMapper imageMapper;
    private final ImageRepository imageRepository;

    public CartItemResponseDto toCartItemResponseDto(CartItem cartItem) {
        if (cartItem == null) {
            return null;
        }
        BigDecimal price = cartItem.getPhone().getPrice();
        BigDecimal lineTotal = price.multiply(BigDecimal.valueOf(cartItem.getAmount()));
        return new CartItemResponseDto(
                cartItem.getPhone().getId(),
                cartItem.getPhone().getName(),
                cartItem.getPhone().getBrand(),
                price,
                previewImage(cartItem),
                cartItem.getPhone().getStock(),
                cartItem.getPhone().getStatus(),
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
