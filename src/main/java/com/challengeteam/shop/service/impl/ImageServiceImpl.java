package com.challengeteam.shop.service.impl;

import com.challengeteam.shop.dto.image.ImageDataDto;
import com.challengeteam.shop.entity.image.Image;
import com.challengeteam.shop.entity.image.MIMEType;
import com.challengeteam.shop.exceptionHandling.exception.CriticalSystemException;
import com.challengeteam.shop.exceptionHandling.exception.FileUtilityException;
import com.challengeteam.shop.exceptionHandling.exception.ImageStorageException;
import com.challengeteam.shop.exceptionHandling.exception.InvalidAPIRequestException;
import com.challengeteam.shop.persistence.repository.ImageRepository;
import com.challengeteam.shop.persistence.storage.ImageStorage;
import com.challengeteam.shop.service.ImageService;
import com.challengeteam.shop.service.MIMETypeService;
import com.challengeteam.shop.utility.FileUtility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class ImageServiceImpl implements ImageService {
    private final ImageRepository imageRepository;
    private final ImageStorage imageStorage;
    private final MIMETypeService mimeTypeService;


    @Transactional(readOnly = true)
    @Override
    public Optional<ImageDataDto> downloadImageById(Long id) {
        Objects.requireNonNull(id, "id");

        try {
            Optional<Image> optionalImage = imageRepository.findById(id);
            if (optionalImage.isPresent()) {
                Image image = optionalImage.get();
                MIMEType mimeType = image.getMimeType();
                String key = image.getStorageKey();
                byte[] bytes = imageStorage
                        .readImageByKey(key)
                        .orElseThrow(() -> new CriticalSystemException("Image doesnt exist in storage with key: " + key));

                var result = new ImageDataDto(
                        image.getName(),
                        bytes,
                        mimeType.getType(),
                        image.getSize()
                );

                log.debug("Successfully found image by id {}", id);
                return Optional.of(result);
            } else {
                log.debug("Not found image by id {}", id);
                return Optional.empty();
            }
        } catch (ImageStorageException e) {
            log.error("Failed to read image by id {}", id, e);
            throw new CriticalSystemException(e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Image> getImageById(Long id) {
        Objects.requireNonNull(id, "id");

        return imageRepository.findById(id);
    }

    @Transactional
    @Override
    public Long uploadImage(MultipartFile file) {
        Objects.requireNonNull(file, "file");

        try {
            Image image = createImage(file);
            imageStorage.putImage(image.getStorageKey(), file);

            log.debug("Successfully uploaded image with id {}", image.getId());
            return image.getId();
        } catch (ImageStorageException e) {
            throw new CriticalSystemException(e.getMessage(), e);
        } catch (FileUtilityException e) {
            throw new InvalidAPIRequestException("Incorrect file request", e);
        }
    }

    private Image createImage(MultipartFile file) throws FileUtilityException {
        MIMEType mimeType = mimeTypeService.createIfDoesntExist(file);
        String filename = FileUtility.getFilename(file);
        String key = FileUtility.transformFilenameToUnique(filename);
        long size = file.getSize();

        Image image = Image
                .builder()
                .mimeType(mimeType)
                .name(filename)
                .storageKey(key)
                .size(size)
                .build();

        image = imageRepository.save(image);
        log.debug("Image with name {} created in repository with id {}", filename, image.getId());

        return image;
    }

}
