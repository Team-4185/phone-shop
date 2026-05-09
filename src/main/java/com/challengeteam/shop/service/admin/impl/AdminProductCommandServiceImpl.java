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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;

/**
 * Default admin write-side implementation for products and their images.
 *
 * <p>The service performs SKU normalization/uniqueness checks, maps admin DTOs into the domain
 * model, and coordinates image persistence through {@link ImageService}.
 */
@Service
@RequiredArgsConstructor
@Slf4j
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
    log.debug("Creating admin product with sku={} and imageCount={}", normalizedSku, images.size());
    validateSkuForCreate(normalizedSku);

    Phone phone = phoneRepository.save(adminProductMapper.toEntity(request, normalizedSku));
    attachImages(phone, images);

    log.info(
        "Created admin product id={} sku={} imageCount={}",
        phone.getId(),
        normalizedSku,
        images.size());
    return phone.getId();
  }

  @Transactional
  @Override
  public void updateProduct(Long id, AdminProductUpdateRequestDto request) {
    Objects.requireNonNull(id, "id");
    Objects.requireNonNull(request, "request");

    log.debug("Updating admin product id={}", id);
    Phone phone = getProductEntity(id);
    String normalizedSku = null;
    if (request.sku() != null) {
      normalizedSku = normalizeSku(request.sku());
      validateSkuForUpdate(normalizedSku, id);
    }

    adminProductMapper.updateEntity(phone, request, normalizedSku);
    phoneRepository.save(phone);
    log.info("Updated admin product id={} sku={}", id, phone.getSku());
  }

  @Transactional
  @Override
  public void deleteProduct(Long id) {
    Objects.requireNonNull(id, "id");

    log.debug("Deleting admin product id={}", id);
    Phone phone = getProductEntity(id);
    List<Image> images = imageRepository.getImagesByPhone_Id(id);
    for (Image image : images) {
      imageService.deleteImage(image.getId());
    }
    phoneRepository.delete(phone);
    log.info("Deleted admin product id={} imageCount={}", id, images.size());
  }

  @Transactional(readOnly = true)
  @Override
  public List<Image> getProductImages(Long productId) {
    Objects.requireNonNull(productId, "productId");

    log.debug("Getting images for admin product id={}", productId);
    ensureProductExists(productId);
    return imageRepository.getImagesByPhone_Id(productId);
  }

  @Transactional
  @Override
  public void addProductImages(Long productId, List<MultipartFile> images) {
    Objects.requireNonNull(productId, "productId");
    Objects.requireNonNull(images, "images");

    log.debug("Adding images to admin product id={} imageCount={}", productId, images.size());
    Phone phone = getProductEntity(productId);
    attachImages(phone, images);
    log.info("Added images to admin product id={} imageCount={}", productId, images.size());
  }

  @Transactional
  @Override
  public void deleteProductImage(Long productId, Long imageId) {
    Objects.requireNonNull(productId, "productId");
    Objects.requireNonNull(imageId, "imageId");

    log.debug("Deleting image id={} from admin product id={}", imageId, productId);
    if (!phoneRepository.existsPhoneByIdWithImage(productId, imageId)) {
      log.warn("Cannot delete image id={} because product id={} does not contain it", imageId, productId);
      throw new ResourceNotFoundException(
          "Not found product with id: %s that contains image with id: %s"
              .formatted(productId, imageId));
    }

    try {
      imageService.deleteImage(imageId);
      log.info("Deleted image id={} from admin product id={}", imageId, productId);
    } catch (ResourceNotFoundException e) {
      log.warn("Verified image id={} for product id={} but image was missing", imageId, productId);
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
        .orElseThrow(
            () -> {
              log.warn("Admin product id={} was not found", id);
              return new ResourceNotFoundException("Not found product with id: " + id);
            });
  }

  private void ensureProductExists(Long id) {
    if (!phoneRepository.existsById(id)) {
      log.warn("Admin product id={} was not found", id);
      throw new ResourceNotFoundException("Not found product with id: " + id);
    }
  }

  private String normalizeSku(String sku) {
    return sku.trim().toUpperCase();
  }

  private void validateSkuForCreate(String sku) {
    if (phoneRepository.existsBySku(sku)) {
      log.warn("Cannot create admin product because sku={} already exists", sku);
      throw new InvalidAPIRequestException("Product with sku '%s' already exists".formatted(sku));
    }
  }

  private void validateSkuForUpdate(String sku, Long productId) {
    if (phoneRepository.existsBySkuAndIdNot(sku, productId)) {
      log.warn("Cannot update admin product id={} because sku={} already exists", productId, sku);
      throw new InvalidAPIRequestException("Product with sku '%s' already exists".formatted(sku));
    }
  }
}
