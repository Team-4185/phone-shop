package com.challengeteam.shop.dto.admin.product;

import com.challengeteam.shop.entity.phone.ProductStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

/**
 * Admin-only request contract for creating products from the Product Management panel.
 *
 * <p>This DTO intentionally stays separate from public catalog DTOs so admin workflows can evolve
 * without changing storefront response/request contracts.
 */
public record AdminProductCreateRequestDto(
    @NotBlank(message = "Name must not be empty")
        @Size(min = 3, max = 255, message = "Name must be between {min} and {max} characters")
        String name,
    @Size(max = 1000, message = "Description must be at most {max} characters long")
        String description,
    @NotNull(message = "Price must not be null")
        @DecimalMin(value = "0.00", message = "Price must be greater than {value}")
        BigDecimal price,
    @NotBlank(message = "Brand must not be empty")
        @Size(min = 3, max = 255, message = "Brand must be between {min} and {max} characters")
        String brand,
    @NotNull(message = "Release year must not be null")
        @Min(value = 1970, message = "Release year must be no earlier than {value}")
        @Max(value = 2026, message = "Release year must be no later than {value}")
        Integer releaseYear,
    @NotBlank(message = "Sku must not be empty")
        @Size(min = 3, max = 64, message = "Sku must be between {min} and {max} characters")
        @Pattern(
            regexp = "^[A-Z0-9]+(?:-[A-Z0-9]+)*$",
            message = "Sku must contain only uppercase letters, numbers and hyphens")
        String sku,
    @NotNull(message = "Stock must not be null")
        @Min(value = 0, message = "Stock must be greater than or equal to {value}")
        Integer stock,
    @NotNull(message = "Status must not be null") ProductStatus status,
    @NotBlank(message = "Cpu must not be empty")
        @Pattern(
            regexp = "^[A-Za-z0-9\\s\\-]+$",
            message = "CPU must contain only letters, numbers, spaces and hyphens")
        @Size(max = 30, message = "CPU must be at most {max} characters long")
        String cpu,
    @NotNull(message = "Cores number must be not null")
        @Min(value = 1, message = "Number of cores must be at least {value}")
        @Max(value = 32, message = "Number of cores must be at most {value}")
        Integer coresNumber,
    @NotBlank(message = "Screen size must not be empty")
        @Pattern(regexp = "^\\d+(\\.\\d+)?\"$", message = "Format: 6.7\"")
        @Size(max = 5, message = "Screen size must be at most {max} characters long")
        String screenSize,
    @NotBlank(message = "Front camera must not be empty")
        @Pattern(regexp = "^\\d+ MP$", message = "Format: 12 MP")
        @Size(max = 10, message = "Front camera must be at most {max} characters long")
        String frontCamera,
    @NotBlank(message = "Main camera must not be empty")
        @Pattern(regexp = "^\\d+(-\\d+)* MP$", message = "Format: 48-12-12 MP")
        @Size(max = 20, message = "Main camera must be at most {max} characters long")
        String mainCamera,
    @NotBlank(message = "Battery capacity must not be empty")
        @Pattern(regexp = "^\\d+ mAh$", message = "Format: 4323 mAh")
        @Size(max = 10, message = "Battery capacity must be at most {max} characters long")
        String batteryCapacity,
    @jakarta.validation.Valid List<AdminProductVariantRequestDto> variants) {
  public AdminProductCreateRequestDto(
      String name,
      String description,
      BigDecimal price,
      String brand,
      Integer releaseYear,
      String sku,
      Integer stock,
      ProductStatus status,
      String cpu,
      Integer coresNumber,
      String screenSize,
      String frontCamera,
      String mainCamera,
      String batteryCapacity) {
    this(
        name,
        description,
        price,
        brand,
        releaseYear,
        sku,
        stock,
        status,
        cpu,
        coresNumber,
        screenSize,
        frontCamera,
        mainCamera,
        batteryCapacity,
        List.of());
  }
}
