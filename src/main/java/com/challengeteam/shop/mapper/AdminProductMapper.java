package com.challengeteam.shop.mapper;

import com.challengeteam.shop.dto.admin.product.AdminProductDetailsResponseDto;
import com.challengeteam.shop.dto.admin.product.AdminProductCreateRequestDto;
import com.challengeteam.shop.dto.admin.product.AdminProductListItemResponseDto;
import com.challengeteam.shop.dto.admin.product.AdminProductUpdateRequestDto;
import com.challengeteam.shop.dto.image.ImageMetadataResponseDto;
import com.challengeteam.shop.entity.image.Image;
import com.challengeteam.shop.entity.phone.Phone;
import com.challengeteam.shop.entity.phone.PhoneCharacteristics;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

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

  public Phone toEntity(AdminProductCreateRequestDto request, String normalizedSku) {
    Objects.requireNonNull(request, "request");
    Objects.requireNonNull(normalizedSku, "normalizedSku");

    return Phone.builder()
        .name(request.name().trim())
        .description(request.description())
        .price(request.price())
        .brand(request.brand().trim())
        .releaseYear(request.releaseYear())
        .sku(normalizedSku)
        .stock(request.stock())
        .status(request.status())
        .phoneCharacteristics(
            PhoneCharacteristics.builder()
                .cpu(request.cpu().trim())
                .coresNumber(request.coresNumber())
                .screenSize(request.screenSize().trim())
                .frontCamera(request.frontCamera().trim())
                .mainCamera(request.mainCamera().trim())
                .batteryCapacity(request.batteryCapacity().trim())
                .build())
        .build();
  }

  public void updateEntity(
      Phone phone, AdminProductUpdateRequestDto request, String normalizedSku) {
    Objects.requireNonNull(phone, "phone");
    Objects.requireNonNull(request, "request");

    if (request.name() != null) phone.setName(request.name().trim());
    if (request.description() != null) phone.setDescription(request.description());
    if (request.price() != null) phone.setPrice(request.price());
    if (request.brand() != null) phone.setBrand(request.brand().trim());
    if (request.releaseYear() != null) phone.setReleaseYear(request.releaseYear());
    if (normalizedSku != null) phone.setSku(normalizedSku);
    if (request.stock() != null) phone.setStock(request.stock());
    if (request.status() != null) phone.setStatus(request.status());

    PhoneCharacteristics characteristics = phone.getPhoneCharacteristics();
    if (characteristics == null) {
      characteristics = new PhoneCharacteristics();
      phone.setPhoneCharacteristics(characteristics);
    }

    if (request.cpu() != null) characteristics.setCpu(request.cpu().trim());
    if (request.coresNumber() != null) characteristics.setCoresNumber(request.coresNumber());
    if (request.screenSize() != null) characteristics.setScreenSize(request.screenSize().trim());
    if (request.frontCamera() != null) characteristics.setFrontCamera(request.frontCamera().trim());
    if (request.mainCamera() != null) characteristics.setMainCamera(request.mainCamera().trim());
    if (request.batteryCapacity() != null)
      characteristics.setBatteryCapacity(request.batteryCapacity().trim());
  }

  private ImageMetadataResponseDto toPreviewImage(List<Image> images) {
    if (images == null || images.isEmpty()) return null;

    return imageMapper.toMetadata(images.getFirst());
  }
}
