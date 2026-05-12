package com.challengeteam.shop.service.admin.impl;

import com.challengeteam.shop.dto.admin.product.AdminProductDetailsResponseDto;
import com.challengeteam.shop.dto.admin.product.AdminProductFilterDto;
import com.challengeteam.shop.dto.admin.product.AdminProductListItemResponseDto;
import com.challengeteam.shop.entity.phone.Phone;
import com.challengeteam.shop.exceptionHandling.exception.ResourceNotFoundException;
import com.challengeteam.shop.mapper.admin.AdminProductMapper;
import com.challengeteam.shop.persistence.repository.PhoneRepository;
import com.challengeteam.shop.persistence.specification.AdminProductSpecification;
import com.challengeteam.shop.service.admin.AdminProductQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Default read-side service for the admin Product Management section.
 *
 * <p>Builds admin filters/sorts and loads images in a second query to keep database pagination
 * applied to products instead of paginating a collection fetch in memory.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AdminProductQueryServiceImpl implements AdminProductQueryService {

  private final PhoneRepository phoneRepository;
  private final AdminProductMapper adminProductMapper;

  @Override
  public Page<AdminProductListItemResponseDto> getProducts(
      int page, int size, AdminProductFilterDto filterDto) {
    log.debug("Get admin products page={} size={} filters={}", page, size, filterDto);
    Pageable pageable = PageRequest.of(page, size, buildSort(filterDto.sort()));
    Specification<Phone> specification = AdminProductSpecification.build(filterDto);
    Page<Phone> productsPage = phoneRepository.findAll(specification, pageable);

    if (productsPage.isEmpty()) {
      log.debug("No admin products found for page={} size={}", page, size);
      return productsPage.map(adminProductMapper::toListItem);
    }

    List<Phone> productsWithImages = phoneRepository.findAllWithImages(productsPage.getContent());
    Map<Long, Phone> productsById =
        productsWithImages.stream().collect(Collectors.toMap(Phone::getId, product -> product));
    List<AdminProductListItemResponseDto> orderedProducts =
        productsPage.getContent().stream()
            .map(product -> productsById.getOrDefault(product.getId(), product))
            .map(adminProductMapper::toListItem)
            .toList();

    log.debug(
        "Found admin products page={} size={} totalElements={}",
        page,
        size,
        productsPage.getTotalElements());
    return new PageImpl<>(orderedProducts, pageable, productsPage.getTotalElements());
  }

  @Override
  public AdminProductDetailsResponseDto getProductById(Long id) {
    log.debug("Get admin product details id={}", id);
    Phone phone =
        phoneRepository
            .findByIdWithImages(id)
            .orElseThrow(
                () -> {
                  log.warn("Admin product id={} was not found", id);
                  return new ResourceNotFoundException("Not found product with id: " + id);
                });

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
