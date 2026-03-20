package com.challengeteam.shop.web.controller.admin;

import com.challengeteam.shop.dto.pagination.PageRequestDto;
import com.challengeteam.shop.dto.pagination.PageResponseDto;
import com.challengeteam.shop.dto.pagination.PhoneFilterDto;
import com.challengeteam.shop.dto.phone.PhoneResponseDto;
import com.challengeteam.shop.entity.phone.Phone;
import com.challengeteam.shop.exceptionHandling.exception.ResourceNotFoundException;
import com.challengeteam.shop.mapper.PhoneMapper;
import com.challengeteam.shop.service.PhoneService;
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
  private final PhoneService phoneService;
  private final PhoneMapper phoneMapper;

  @Operation(
      summary = "Get admin product list",
      description = "Returns a paginated list of products for admin section.")
  @GetMapping
  public ResponseEntity<PageResponseDto<PhoneResponseDto>> getAllProducts(
      @Valid PageRequestDto pageRequestDto, @Valid PhoneFilterDto filterDto) {
    return getPageResponseDtoResponseEntity(pageRequestDto, filterDto, phoneService, phoneMapper);
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
      summary = "Get admin product by id",
      description = "Returns product details for admin section.")
  @GetMapping("/{id:\\d+}")
  public ResponseEntity<PhoneResponseDto> getProductById(@PathVariable Long id) {
    Phone phone =
        phoneService
            .getById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Not found product with id: " + id));

    return ResponseEntity.ok(phoneMapper.toResponse(phone));
  }
}
