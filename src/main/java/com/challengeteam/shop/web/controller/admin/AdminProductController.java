package com.challengeteam.shop.web.controller.admin;

import com.challengeteam.shop.dto.admin.product.AdminProductDetailsResponseDto;
import com.challengeteam.shop.dto.admin.product.AdminProductFilterDto;
import com.challengeteam.shop.dto.admin.product.AdminProductListItemResponseDto;
import com.challengeteam.shop.dto.pagination.PageRequestDto;
import com.challengeteam.shop.dto.pagination.PageResponseDto;
import com.challengeteam.shop.dto.pagination.PhoneFilterDto;
import com.challengeteam.shop.dto.phone.PhoneResponseDto;
import com.challengeteam.shop.entity.phone.Phone;
import com.challengeteam.shop.mapper.PhoneMapper;
import com.challengeteam.shop.service.PhoneService;
import com.challengeteam.shop.service.admin.AdminProductQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/products")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Validated
public class AdminProductController {
  private final AdminProductQueryService adminProductQueryService;

  @Operation(
      summary = "Get admin product list",
      description =
          "Returns a paginated list of products for admin section with admin-specific filtering and sorting.")
  @GetMapping
  public ResponseEntity<PageResponseDto<AdminProductListItemResponseDto>> getAllProducts(
      @Valid PageRequestDto pageRequestDto, @Valid AdminProductFilterDto filterDto) {
    int page = pageRequestDto.page() - 1;
    int size = pageRequestDto.size();

    Page<AdminProductListItemResponseDto> products =
        adminProductQueryService.getProducts(page, size, filterDto);

    return ResponseEntity.ok(PageResponseDto.of(products));
  }

  @NonNull
  public static ResponseEntity<PageResponseDto<PhoneResponseDto>> getPageResponseDtoResponseEntity(
      @Valid PageRequestDto pageRequestDto,
      @Valid PhoneFilterDto filterDto,
      PhoneService phoneService,
      PhoneMapper phoneMapper) {
    int page = pageRequestDto.page() - 1;
    int size = pageRequestDto.size();

    Page<Phone> phones = phoneService.getPhones(page, size, filterDto);
    Page<PhoneResponseDto> response = phones.map(phoneMapper::toResponse);

    return ResponseEntity.ok(PageResponseDto.of(response));
  }

  @Operation(
      summary = "Get admin product details by id",
      description =
          "Returns full product details required for admin overview and future edit form.")
  @GetMapping("/{id:\\d+}")
  public ResponseEntity<AdminProductDetailsResponseDto> getProductById(@PathVariable Long id) {
    return ResponseEntity.ok(adminProductQueryService.getProductById(id));
  }
}
