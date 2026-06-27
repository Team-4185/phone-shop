package com.challengeteam.shop.service;

import com.challengeteam.shop.dto.admin.product.AdminProductVariantRequestDto;
import com.challengeteam.shop.dto.admin.product.AdminProductVariantUpdateRequestDto;
import com.challengeteam.shop.entity.phone.Phone;
import com.challengeteam.shop.entity.phone.PhoneColor;
import com.challengeteam.shop.entity.phone.ProductVariant;
import com.challengeteam.shop.entity.phone.StorageCapacity;
import java.util.List;
import java.util.Optional;

public interface ProductVariantService {

  List<ProductVariant> createInitialVariants(Phone phone, List<AdminProductVariantRequestDto> requests);

  ProductVariant createVariant(Phone phone, AdminProductVariantRequestDto request);

  ProductVariant updateVariant(Long phoneId, Long variantId, AdminProductVariantUpdateRequestDto request);

  void deleteVariant(Long phoneId, Long variantId);

  ProductVariant getById(Long id);

  Optional<ProductVariant> findByPhoneColorAndStorage(
      Long phoneId, PhoneColor color, StorageCapacity storageCapacity);

  void syncSingleVariantFromPhone(Phone phone);

  void syncPhoneDisplayFields(Phone phone);
}
