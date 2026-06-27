package com.challengeteam.shop.service.admin;

import com.challengeteam.shop.dto.admin.product.AdminProductCreateRequestDto;
import com.challengeteam.shop.dto.admin.product.AdminProductUpdateRequestDto;
import com.challengeteam.shop.dto.admin.product.AdminProductVariantRequestDto;
import com.challengeteam.shop.dto.admin.product.AdminProductVariantUpdateRequestDto;
import com.challengeteam.shop.entity.image.Image;
import com.challengeteam.shop.entity.phone.ProductVariant;
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

  ProductVariant addProductVariant(Long productId, AdminProductVariantRequestDto request);

  ProductVariant updateProductVariant(
      Long productId, Long variantId, AdminProductVariantUpdateRequestDto request);

  void deleteProductVariant(Long productId, Long variantId);

  List<Image> getProductImages(Long productId);

  void addProductImages(Long productId, List<MultipartFile> images);

  void deleteProductImage(Long productId, Long imageId);
}
