package com.challengeteam.shop.persistence.storage;

import com.challengeteam.shop.exceptionHandling.exception.ImageStorageException;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

public interface ImageStorage {

    Optional<byte[]> readImageByKey(String key) throws ImageStorageException;
    void putImage(String key, MultipartFile image) throws ImageStorageException;
    void deleteImage(String key) throws ImageStorageException;

}
