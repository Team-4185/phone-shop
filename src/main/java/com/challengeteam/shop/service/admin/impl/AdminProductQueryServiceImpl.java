package com.challengeteam.shop.service.admin.impl;

import com.challengeteam.shop.dto.admin.product.AdminProductDetailsResponseDto;
import com.challengeteam.shop.dto.admin.product.AdminProductFilterDto;
import com.challengeteam.shop.dto.admin.product.AdminProductListItemResponseDto;
import com.challengeteam.shop.entity.phone.Phone;
import com.challengeteam.shop.exceptionHandling.exception.ResourceNotFoundException;
import com.challengeteam.shop.mapper.AdminProductMapper;
import com.challengeteam.shop.persistence.repository.PhoneRepository;
import com.challengeteam.shop.persistence.specification.AdminProductSpecification;
import com.challengeteam.shop.service.admin.AdminProductQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminProductQueryServiceImpl implements AdminProductQueryService {

  private final PhoneRepository phoneRepository;
  private final AdminProductMapper adminProductMapper;

  @Override
  public Page<AdminProductListItemResponseDto> getProducts(
      int page, int size, AdminProductFilterDto filterDto) {
    Pageable pageable = PageRequest.of(page, size, buildSort(filterDto.sort()));
    Specification<Phone> specification = AdminProductSpecification.build(filterDto);

    return phoneRepository.findAll(specification, pageable).map(adminProductMapper::toListItem);
  }

  @Override
  public AdminProductDetailsResponseDto getProductById(Long id) {
    Phone phone =
        phoneRepository
            .findByIdWithImages(id)
            .orElseThrow(() -> new ResourceNotFoundException("Not found product with id: " + id));

    return adminProductMapper.toDetails(phone);
  }

  private Sort buildSort(String sort) {
    return switch (sort) {
      case "name_desc" -> Sort.by("name").descending();
      case "price_asc" -> Sort.by("price").ascending();
      case "price_desc" -> Sort.by("price").descending();
      case "releaseYear_asc" -> Sort.by("releaseYear").ascending();
      case "releaseYear_desc" -> Sort.by("releaseYear").descending();
      case "brand_asc" -> Sort.by("brand").ascending();
      case "brand_desc" -> Sort.by("brand").descending();
      case "sku_asc" -> Sort.by("sku").ascending();
      case "sku_desc" -> Sort.by("sku").descending();
      case "stock_asc" -> Sort.by("stock").ascending();
      case "stock_desc" -> Sort.by("stock").descending();
      default -> Sort.by("name").ascending();
    };
  }
}
