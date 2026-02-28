package com.challengeteam.shop.mapper;

import com.challengeteam.shop.dto.image.ImageDataDto;
import com.challengeteam.shop.dto.image.ImageMetadataResponseDto;
import com.challengeteam.shop.entity.image.Image;
import org.mapstruct.Mapper;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ImageMapper {

    default Resource toResource(ImageDataDto dto) {
        return new ByteArrayResource(dto.imageBytes());
    }

    default ImageMetadataResponseDto toMetadata(Image image) {
        return new ImageMetadataResponseDto(
                image.getId(),
                image.getName(),
                mapImageUrl(image),
                image.getSize(),
                image.getMimeType().getType()
        );
    }

    default String mapImageUrl(Image image) {
        return ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/v1/images/{id}")
                .buildAndExpand(image.getId())
                .toUri()
                .toString();
    }

    List<ImageMetadataResponseDto> toListOfMetadata(List<Image> images);
}
