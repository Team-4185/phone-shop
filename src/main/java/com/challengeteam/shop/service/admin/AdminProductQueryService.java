package com.challengeteam.shop.service.admin;

import com.challengeteam.shop.dto.admin.product.AdminProductDetailsResponseDto;
import com.challengeteam.shop.dto.admin.product.AdminProductFilterDto;
import com.challengeteam.shop.dto.admin.product.AdminProductListItemResponseDto;
import org.springframework.data.domain.Page;

/**
 * Read-side service for the admin Product Management UI.
 *
 * <p>Provides list and details contracts tailored for the admin panel, independent of public
 * catalog product responses.
 */
public interface AdminProductQueryService {
  Page<AdminProductListItemResponseDto> getProducts(
      int page, int size, AdminProductFilterDto filterDto);

  AdminProductDetailsResponseDto getProductById(Long id);
}
