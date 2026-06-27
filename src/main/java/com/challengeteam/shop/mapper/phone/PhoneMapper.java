package com.challengeteam.shop.mapper.phone;

import com.challengeteam.shop.dto.phone.response.PhoneColorResponseDto;
import com.challengeteam.shop.dto.phone.response.PhoneResponseDto;
import com.challengeteam.shop.dto.phone.response.StorageCapacityResponseDto;
import com.challengeteam.shop.dto.image.ImageMetadataResponseDto;
import com.challengeteam.shop.entity.image.Image;
import com.challengeteam.shop.entity.phone.Phone;
import com.challengeteam.shop.entity.phone.PhoneColor;
import com.challengeteam.shop.entity.phone.StorageCapacity;
import com.challengeteam.shop.mapper.image.ImageMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * Mapper interface for converting between {@link Phone} entities and {@link PhoneResponseDto} data transfer objects.
 * <p>
 * This MapStruct mapper is configured as a Spring component and uses {@link ImageMapper} for mapping
 * phone images. It handles the mapping of phone characteristics from the embedded {@code PhoneCharacteristics}
 * object to the flat structure of the response DTO.
 * </p>
 *
 * @see Phone
 * @see PhoneResponseDto
 * @see ImageMapper
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {ImageMapper.class, ProductVariantMapper.class})
public interface PhoneMapper {

    /**
     * Converts a {@link Phone} entity to a {@link PhoneResponseDto}.
     * <p>
     * This method maps phone characteristics from the embedded {@code phoneCharacteristics} object
     * to individual fields in the response DTO, including CPU, cores number, screen size, camera
     * specifications, and battery capacity. Phone images are mapped using the {@link ImageMapper}.
     * </p>
     *
     * @param phone the phone entity to convert
     * @return a {@link PhoneResponseDto} containing the phone data with flattened characteristics
     */
    @Mapping(source = "phoneCharacteristics.cpu", target = "cpu")
    @Mapping(source = "phoneCharacteristics.coresNumber", target = "coresNumber")
    @Mapping(source = "phoneCharacteristics.screenSize", target = "screenSize")
    @Mapping(source = "phoneCharacteristics.frontCamera", target = "frontCamera")
    @Mapping(source = "phoneCharacteristics.mainCamera", target = "mainCamera")
    @Mapping(source = "phoneCharacteristics.batteryCapacity", target = "batteryCapacity")
    @Mapping(source = "images", target = "images")
    @Mapping(source = "phoneCharacteristics.phoneColors", target = "colors")
    @Mapping(source = "phoneCharacteristics.storageCapacities", target = "storageCapacity")
    @Mapping(source = "variants", target = "variants")
    @Mapping(target = "previewImage", expression = "java(toPreviewImage(phone.getImages()))")
    @Mapping(target = "badges", expression = "java(java.util.Set.of())")
    @Mapping(target = "discountPercent", constant = "0")
    @Mapping(target = "averageRating", expression = "java(java.math.BigDecimal.ZERO)")
    @Mapping(target = "reviewsCount", constant = "0L")
    PhoneResponseDto toResponse(Phone phone);

    /**
     * Converts a list of {@link Phone} entities to a list of {@link PhoneResponseDto} objects.
     * <p>
     * This method applies the {@link #toResponse(Phone)} mapping to each phone in the provided list.
     * </p>
     *
     * @param phones the list of phone entities to convert
     * @return a list of {@link PhoneResponseDto} objects corresponding to the input phones
     */
    List<PhoneResponseDto> toResponseList(List<Phone> phones);

    default PhoneColorResponseDto toColorDto(PhoneColor color) {
        return new PhoneColorResponseDto(color.name(), color.getDisplayName(), color.getHexCode());
    }

    default StorageCapacityResponseDto toStorageCapacityDto(StorageCapacity capacity) {
        return new StorageCapacityResponseDto(capacity.name(), capacity.getValue(), capacity.getUnit());
    }

    default ImageMetadataResponseDto toPreviewImage(List<Image> images) {
        if (images == null || images.isEmpty()) {
            return null;
        }

        Image image = images.getFirst();
        URI uri = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/v1/images/{id}")
                .buildAndExpand(image.getId())
                .toUri();

        return new ImageMetadataResponseDto(
                image.getId(),
                image.getName(),
                uri.toString(),
                image.getSize(),
                image.getMimeType().getType());
    }
}
