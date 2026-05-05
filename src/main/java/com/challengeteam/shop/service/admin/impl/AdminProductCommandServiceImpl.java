package com.challengeteam.shop.service.admin.impl;

import com.challengeteam.shop.dto.admin.product.AdminProductCreateRequestDto;
import com.challengeteam.shop.dto.admin.product.AdminProductUpdateRequestDto;
import com.challengeteam.shop.entity.image.Image;
import com.challengeteam.shop.entity.phone.Phone;
import com.challengeteam.shop.exceptionHandling.exception.CriticalSystemException;
import com.challengeteam.shop.exceptionHandling.exception.InvalidAPIRequestException;
import com.challengeteam.shop.exceptionHandling.exception.ResourceNotFoundException;
import com.challengeteam.shop.mapper.AdminProductMapper;
import com.challengeteam.shop.persistence.repository.ImageRepository;
import com.challengeteam.shop.persistence.repository.PhoneRepository;
import com.challengeteam.shop.service.ImageService;
import com.challengeteam.shop.service.admin.AdminProductCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AdminProductCommandServiceImpl implements AdminProductCommandService {
  private final PhoneRepository phoneRepository;
  private final ImageRepository imageRepository;
  private final ImageService imageService;
  private final AdminProductMapper adminProductMapper;

  @Transactional
  @Override
  public Long createProduct(AdminProductCreateRequestDto request, List<MultipartFile> images) {
    Objects.requireNonNull(request, "request");
    Objects.requireNonNull(images, "images");

    String normalizedSku = normalizeSku(request.sku());
    validateSkuForCreate(normalizedSku);

    Phone phone = phoneRepository.save(adminProductMapper.toEntity(request, normalizedSku));
    attachImages(phone, images);

    return phone.getId();
  }

  @Transactional
  @Override
  public void updateProduct(Long id, AdminProductUpdateRequestDto request) {
    Objects.requireNonNull(id, "id");
    Objects.requireNonNull(request, "request");

    Phone phone = getProductEntity(id);
    String normalizedSku = null;
    if (request.sku() != null) {
      normalizedSku = normalizeSku(request.sku());
      validateSkuForUpdate(normalizedSku, id);
    }

    adminProductMapper.updateEntity(phone, request, normalizedSku);
    phoneRepository.save(phone);
  }

  @Transactional
  @Override
  public void deleteProduct(Long id) {
    Objects.requireNonNull(id, "id");

    Phone phone = getProductEntity(id);
    for (Image image : imageRepository.getImagesByPhone_Id(id)) {
      imageService.deleteImage(image.getId());
    }
    phoneRepository.delete(phone);
  }

  @Transactional(readOnly = true)
  @Override
  public List<Image> getProductImages(Long productId) {
    Objects.requireNonNull(productId, "productId");

    ensureProductExists(productId);
    return imageRepository.getImagesByPhone_Id(productId);
  }

  @Transactional
  @Override
  public void addProductImages(Long productId, List<MultipartFile> images) {
    Objects.requireNonNull(productId, "productId");
    Objects.requireNonNull(images, "images");

    Phone phone = getProductEntity(productId);
    attachImages(phone, images);
  }

  @Transactional
  @Override
  public void deleteProductImage(Long productId, Long imageId) {
    Objects.requireNonNull(productId, "productId");
    Objects.requireNonNull(imageId, "imageId");

    if (!phoneRepository.existsPhoneByIdWithImage(productId, imageId)) {
      throw new ResourceNotFoundException(
          "Not found product with id: %s that contains image with id: %s"
              .formatted(productId, imageId));
    }

    try {
      imageService.deleteImage(imageId);
    } catch (ResourceNotFoundException e) {
      throw new CriticalSystemException(
          "Not found image by id: %s after verifying".formatted(imageId), e);
    }
  }

  private void attachImages(Phone phone, List<MultipartFile> images) {
    for (MultipartFile file : images) {
      Image image = imageService.uploadImage(file);
      image.setPhone(phone);
      imageRepository.save(image);
    }
  }

  private Phone getProductEntity(Long id) {
    return phoneRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Not found product with id: " + id));
  }

  private void ensureProductExists(Long id) {
    if (!phoneRepository.existsById(id)) {
      throw new ResourceNotFoundException("Not found product with id: " + id);
    }
  }

  private String normalizeSku(String sku) {
    return sku.trim().toUpperCase();
  }

  private void validateSkuForCreate(String sku) {
    if (phoneRepository.existsBySku(sku)) {
      throw new InvalidAPIRequestException("Product with sku '%s' already exists".formatted(sku));
    }
  }

  private void validateSkuForUpdate(String sku, Long productId) {
    if (phoneRepository.existsBySkuAndIdNot(sku, productId)) {
      throw new InvalidAPIRequestException("Product with sku '%s' already exists".formatted(sku));
    }
  }
}
