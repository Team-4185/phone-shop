package com.challengeteam.shop.service.admin;

import com.challengeteam.shop.dto.admin.product.AdminProductCreateRequestDto;
import com.challengeteam.shop.dto.admin.product.AdminProductUpdateRequestDto;
import com.challengeteam.shop.entity.image.Image;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Write-side service for admin product management.
 *
 * <p>Owns product mutations and product image management for the admin panel while keeping admin
 * commands separate from storefront phone services.
 */
public interface AdminProductCommandService {
  Long createProduct(AdminProductCreateRequestDto request, List<MultipartFile> images);

  void updateProduct(Long id, AdminProductUpdateRequestDto request);

  void deleteProduct(Long id);

  List<Image> getProductImages(Long productId);

  void addProductImages(Long productId, List<MultipartFile> images);

  void deleteProductImage(Long productId, Long imageId);
}
