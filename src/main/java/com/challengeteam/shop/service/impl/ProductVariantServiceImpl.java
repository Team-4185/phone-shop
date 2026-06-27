package com.challengeteam.shop.service.impl;

import com.challengeteam.shop.dto.admin.product.AdminProductVariantRequestDto;
import com.challengeteam.shop.dto.admin.product.AdminProductVariantUpdateRequestDto;
import com.challengeteam.shop.entity.phone.Phone;
import com.challengeteam.shop.entity.phone.PhoneCharacteristics;
import com.challengeteam.shop.entity.phone.PhoneColor;
import com.challengeteam.shop.entity.phone.ProductVariant;
import com.challengeteam.shop.entity.phone.StorageCapacity;
import com.challengeteam.shop.exceptionHandling.exception.InvalidAPIRequestException;
import com.challengeteam.shop.exceptionHandling.exception.ResourceNotFoundException;
import com.challengeteam.shop.persistence.repository.PhoneRepository;
import com.challengeteam.shop.persistence.repository.ProductVariantRepository;
import com.challengeteam.shop.service.ProductVariantService;
import com.challengeteam.shop.utility.ProductStatusResolver;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductVariantServiceImpl implements ProductVariantService {

  private final ProductVariantRepository productVariantRepository;
  private final PhoneRepository phoneRepository;

  @Override
  @Transactional
  public List<ProductVariant> createInitialVariants(
      Phone phone, List<AdminProductVariantRequestDto> requests) {
    Objects.requireNonNull(phone, "phone");
    List<AdminProductVariantRequestDto> safeRequests =
        Optional.ofNullable(requests)
            .filter(list -> !list.isEmpty())
            .orElseGet(() -> List.of(defaultVariantRequest(phone)));

    validateNoDuplicateCombinations(safeRequests);
    List<ProductVariant> variants =
        safeRequests.stream().map(request -> createVariant(phone, request)).toList();
    syncPhoneDisplayFields(phone);
    return variants;
  }

  @Override
  @Transactional
  public ProductVariant createVariant(Phone phone, AdminProductVariantRequestDto request) {
    Objects.requireNonNull(phone, "phone");
    Objects.requireNonNull(request, "request");

    String sku = normalizeSku(request.sku());
    validateSkuAvailable(sku);
    validateCombinationAvailable(phone.getId(), request.color(), request.storageCapacity());

    ProductVariant variant =
        ProductVariant.builder()
            .phone(phone)
            .sku(sku)
            .color(request.color())
            .storageCapacity(request.storageCapacity())
            .price(request.price())
            .stock(request.stock())
            .status(ProductStatusResolver.resolve(request.stock()))
            .build();
    ProductVariant saved = productVariantRepository.save(variant);
    ensurePhoneOptions(phone, saved.getColor(), saved.getStorageCapacity());
    log.info("Created product variant id={} phoneId={} sku={}", saved.getId(), phone.getId(), sku);
    return saved;
  }

  @Override
  @Transactional
  public ProductVariant updateVariant(
      Long phoneId, Long variantId, AdminProductVariantUpdateRequestDto request) {
    Objects.requireNonNull(phoneId, "phoneId");
    Objects.requireNonNull(variantId, "variantId");
    Objects.requireNonNull(request, "request");

    ProductVariant variant = getOwnedVariant(phoneId, variantId);
    applySkuUpdate(variant, request, variantId);
    Optional.ofNullable(request.color()).ifPresent(variant::setColor);
    Optional.ofNullable(request.storageCapacity()).ifPresent(variant::setStorageCapacity);
    validateCombinationAvailableForUpdate(
        phoneId, variant.getColor(), variant.getStorageCapacity(), variantId);
    Optional.ofNullable(request.price()).ifPresent(variant::setPrice);
    Optional.ofNullable(request.stock()).ifPresent(stock -> applyStock(variant, stock));

    ProductVariant saved = productVariantRepository.save(variant);
    ensurePhoneOptions(saved.getPhone(), saved.getColor(), saved.getStorageCapacity());
    syncPhoneDisplayFields(saved.getPhone());
    log.info("Updated product variant id={} phoneId={} sku={}", saved.getId(), phoneId, saved.getSku());
    return saved;
  }

  @Override
  @Transactional
  public void deleteVariant(Long phoneId, Long variantId) {
    ProductVariant variant = getOwnedVariant(phoneId, variantId);
    productVariantRepository.delete(variant);
    syncPhoneDisplayFields(variant.getPhone());
    log.info("Deleted product variant id={} phoneId={}", variantId, phoneId);
  }

  @Override
  @Transactional(readOnly = true)
  public ProductVariant getById(Long id) {
    return productVariantRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Product variant with id " + id + " not found"));
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<ProductVariant> findByPhoneColorAndStorage(
      Long phoneId, PhoneColor color, StorageCapacity storageCapacity) {
    return productVariantRepository.findByPhoneIdAndColorAndStorageCapacity(
        phoneId, color, storageCapacity);
  }

  @Override
  @Transactional
  public void syncSingleVariantFromPhone(Phone phone) {
    List<ProductVariant> variants = productVariantRepository.findAllByPhoneIdOrderByPriceAscIdAsc(phone.getId());
    if (variants.size() != 1) {
      return;
    }
    ProductVariant variant = variants.getFirst();
    variant.setPrice(phone.getPrice());
    variant.setStock(phone.getStock());
    variant.setStatus(ProductStatusResolver.resolve(phone.getStock()));
    variant.setSku(defaultVariantSku(phone.getSku(), variant.getColor(), variant.getStorageCapacity()));
    productVariantRepository.save(variant);
  }

  @Override
  @Transactional
  public void syncPhoneDisplayFields(Phone phone) {
    List<ProductVariant> variants = productVariantRepository.findAllByPhoneIdOrderByPriceAscIdAsc(phone.getId());
    if (variants.isEmpty()) {
      return;
    }
    int stock = variants.stream().mapToInt(ProductVariant::getStock).sum();
    BigDecimal price = variants.getFirst().getPrice();
    phone.setPrice(price);
    phone.setStock(stock);
    phone.setStatus(ProductStatusResolver.resolve(stock));
    phoneRepository.save(phone);
  }

  private ProductVariant getOwnedVariant(Long phoneId, Long variantId) {
    ProductVariant variant = getById(variantId);
    requireVariantBelongsToPhone(variant, phoneId, variantId);
    return variant;
  }

  private AdminProductVariantRequestDto defaultVariantRequest(Phone phone) {
    PhoneCharacteristics characteristics = phone.getPhoneCharacteristics();
    PhoneColor color = firstOption(characteristics == null ? null : characteristics.getPhoneColors(), PhoneColor.BLACK);
    StorageCapacity storage =
        firstOption(
            characteristics == null ? null : characteristics.getStorageCapacities(),
            StorageCapacity.CAPACITY_128GB);
    return new AdminProductVariantRequestDto(
        defaultVariantSku(phone.getSku(), color, storage),
        color,
        storage,
        phone.getPrice(),
        phone.getStock(),
        phone.getStatus());
  }

  private String defaultVariantSku(String phoneSku, PhoneColor color, StorageCapacity storage) {
    String sku =
        phoneSku
            + "-"
            + color.name()
            + "-"
            + storage.name().replace("CAPACITY_", "");
    return sku.length() <= 64 ? sku : sku.substring(0, 64);
  }

  private void validateNoDuplicateCombinations(List<AdminProductVariantRequestDto> requests) {
    requests.stream()
        .collect(Collectors.groupingBy(this::combinationKey, Collectors.counting()))
        .entrySet()
        .stream()
        .filter(entry -> entry.getValue() > 1)
        .findFirst()
        .ifPresent(entry -> {
          throw new InvalidAPIRequestException(
              "Duplicate variant combination for color and storage: " + entry.getKey());
        });
  }

  private void ensurePhoneOptions(Phone phone, PhoneColor color, StorageCapacity storageCapacity) {
    PhoneCharacteristics characteristics =
        Optional.ofNullable(phone.getPhoneCharacteristics())
            .orElseGet(() -> {
              PhoneCharacteristics created = new PhoneCharacteristics();
              phone.setPhoneCharacteristics(created);
              return created;
            });
    characteristics.setPhoneColors(
        new HashSet<>(Optional.ofNullable(characteristics.getPhoneColors()).orElse(Set.of())));
    characteristics.setStorageCapacities(
        new HashSet<>(Optional.ofNullable(characteristics.getStorageCapacities()).orElse(Set.of())));
    characteristics.getPhoneColors().add(color);
    characteristics.getStorageCapacities().add(storageCapacity);
  }

  private void validateSkuAvailable(String sku) {
    if (productVariantRepository.existsBySku(sku) || phoneRepository.existsBySku(sku)) {
      throw new InvalidAPIRequestException("Product variant with sku '%s' already exists".formatted(sku));
    }
  }

  private void validateSkuAvailableForUpdate(String sku, Long variantId) {
    if (productVariantRepository.existsBySkuAndIdNot(sku, variantId) || phoneRepository.existsBySku(sku)) {
      throw new InvalidAPIRequestException("Product variant with sku '%s' already exists".formatted(sku));
    }
  }

  private void validateCombinationAvailable(
      Long phoneId, PhoneColor color, StorageCapacity storageCapacity) {
    Optional.ofNullable(phoneId)
        .filter(id -> productVariantRepository.existsByPhoneIdAndColorAndStorageCapacity(id, color, storageCapacity))
        .ifPresent(id -> {
          throw duplicateCombinationException(color, storageCapacity);
        });
  }

  private void validateCombinationAvailableForUpdate(
      Long phoneId, PhoneColor color, StorageCapacity storageCapacity, Long variantId) {
    if (productVariantRepository.existsByPhoneIdAndColorAndStorageCapacityAndIdNot(
        phoneId, color, storageCapacity, variantId)) {
      throw duplicateCombinationException(color, storageCapacity);
    }
  }

  private InvalidAPIRequestException duplicateCombinationException(
      PhoneColor color, StorageCapacity storageCapacity) {
    return new InvalidAPIRequestException(
        "Variant for color %s and storage %s already exists".formatted(color, storageCapacity));
  }

  private void applySkuUpdate(
      ProductVariant variant, AdminProductVariantUpdateRequestDto request, Long variantId) {
    Optional.ofNullable(request.sku())
        .map(this::normalizeSku)
        .ifPresent(sku -> {
          validateSkuAvailableForUpdate(sku, variantId);
          variant.setSku(sku);
        });
  }

  private void applyStock(ProductVariant variant, int stock) {
    variant.setStock(stock);
    variant.setStatus(ProductStatusResolver.resolve(stock));
  }

  private void requireVariantBelongsToPhone(ProductVariant variant, Long phoneId, Long variantId) {
    if (!variant.getPhone().getId().equals(phoneId)) {
      throw new ResourceNotFoundException(
          "Product variant with id %s was not found for product id %s".formatted(variantId, phoneId));
    }
  }

  private <T> T firstOption(Set<T> options, T fallback) {
    return Optional.ofNullable(options).stream()
        .flatMap(Set::stream)
        .findFirst()
        .orElse(fallback);
  }

  private String combinationKey(AdminProductVariantRequestDto request) {
    return request.color() + "|" + request.storageCapacity();
  }

  private String normalizeSku(String sku) {
    return sku.trim().toUpperCase();
  }
}
