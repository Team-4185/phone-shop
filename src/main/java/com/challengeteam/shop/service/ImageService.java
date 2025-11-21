package com.challengeteam.shop.service;

import com.challengeteam.shop.dto.image.ImageDataDto;
import com.challengeteam.shop.entity.image.Image;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

public interface ImageService {

    Optional<ImageDataDto> downloadImageById(Long id);
    Optional<Image> getImageById(Long id);
    Long uploadImage(MultipartFile image);
    void deleteImage(Long imageId);
}
