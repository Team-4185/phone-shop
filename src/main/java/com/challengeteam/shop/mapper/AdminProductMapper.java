package com.challengeteam.shop.mapper;

import com.challengeteam.shop.dto.admin.product.AdminProductDetailsResponseDto;
import com.challengeteam.shop.dto.admin.product.AdminProductListItemResponseDto;
import com.challengeteam.shop.dto.image.ImageMetadataResponseDto;
import com.challengeteam.shop.entity.image.Image;
import com.challengeteam.shop.entity.phone.Phone;
import com.challengeteam.shop.entity.phone.PhoneCharacteristics;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AdminProductMapper {
  private final ImageMapper imageMapper;

  public AdminProductListItemResponseDto toListItem(Phone phone) {
    return new AdminProductListItemResponseDto(
        phone.getId(),
        phone.getName(),
        phone.getSku(),
        phone.getBrand(),
        phone.getPrice(),
        phone.getStock(),
        phone.getStatus(),
        toPreviewImage(phone.getImages()));
  }

  public AdminProductDetailsResponseDto toDetails(Phone phone) {
    PhoneCharacteristics characteristics = phone.getPhoneCharacteristics();

    return new AdminProductDetailsResponseDto(
        phone.getId(),
        phone.getName(),
        phone.getSku(),
        phone.getDescription(),
        phone.getPrice(),
        phone.getBrand(),
        phone.getReleaseYear(),
        phone.getStock(),
        phone.getStatus(),
        characteristics != null ? characteristics.getCpu() : null,
        characteristics != null ? characteristics.getCoresNumber() : null,
        characteristics != null ? characteristics.getScreenSize() : null,
        characteristics != null ? characteristics.getFrontCamera() : null,
        characteristics != null ? characteristics.getMainCamera() : null,
        characteristics != null ? characteristics.getBatteryCapacity() : null,
        phone.getImages() == null ? List.of() : imageMapper.toListOfMetadata(phone.getImages()));
  }

  private ImageMetadataResponseDto toPreviewImage(List<Image> images) {
    if (images == null || images.isEmpty()) return null;

    return imageMapper.toMetadata(images.getFirst());
  }
}
