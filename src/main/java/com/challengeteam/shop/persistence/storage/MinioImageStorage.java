package com.challengeteam.shop.persistence.storage;

import com.challengeteam.shop.exceptionHandling.exception.CriticalSystemException;
import com.challengeteam.shop.exceptionHandling.exception.ImageStorageException;
import com.challengeteam.shop.properties.MinioProperties;
import io.minio.*;
import io.minio.errors.ErrorResponseException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
public class MinioImageStorage implements ImageStorage {
    public static final String IMAGE_BUCKET_NAME = "images";

    private final MinioProperties minioProperties;

    private MinioClient minioClient;


    @PostConstruct
    public void init() {
        var url = minioProperties.getUrl();
        var username = minioProperties.getUsername();
        var password = minioProperties.getPassword();

        minioClient = MinioClient
                .builder()
                .endpoint(url)
                .credentials(username, password)
                .build();
        log.info("MinioImageStorage successfully initiated");
    }

    @PreDestroy
    public void cleanUp() {
        try {
            minioClient.close();
            log.debug("Minio client closed successfully");
        } catch (Exception e) {
            log.error("Failed to close minio client");
        }
    }

    @Override
    public Optional<byte[]> readImageByKey(String key) throws ImageStorageException {
        Objects.requireNonNull(key, "key");
        try {
            var args = GetObjectArgs
                    .builder()
                    .object(key)
                    .bucket(IMAGE_BUCKET_NAME)
                    .build();

            GetObjectResponse result = minioClient.getObject(args);
            log.debug("Found image with key {}", key);

            return Optional.of(result.readAllBytes());
        } catch (ErrorResponseException e) {
            log.debug("Not found image with key: {}", key);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Failed to read image by key: {}", key, e);
            throw new ImageStorageException("Failed to read image with key: " + key, e);
        }
    }

    @Override
    public void putImage(String key, MultipartFile image) throws ImageStorageException {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(image, "image");

        try (InputStream stream = image.getInputStream()) {
            long size = image.getSize();

            var args = PutObjectArgs
                    .builder()
                    .bucket(IMAGE_BUCKET_NAME)
                    .object(key)
                    .stream(stream, size, -1)
                    .contentType(image.getContentType())
                    .build();

            minioClient.putObject(args);
            log.debug("Successfully putted image with key {} into bucket {}", key, IMAGE_BUCKET_NAME);
        } catch (Exception e) {
            log.error("Failed to put image", e);
            throw new ImageStorageException("Failed to put image into storage with key: " + key, e);
        }
    }

    @Override
    public void deleteImage(String key) throws ImageStorageException {
        Objects.requireNonNull(key, "key");

        try {
            var args = RemoveObjectArgs
                    .builder()
                    .bucket(IMAGE_BUCKET_NAME)
                    .object(key)
                    .build();

            minioClient.removeObject(args);
            log.debug("Image with key {} was successfully deleted from bucket {}", key, IMAGE_BUCKET_NAME);
        } catch (Exception e) {
            log.error("Failed to delete image with key {}", key, e);
            throw new ImageStorageException("Failed to delete image with key: " + key, e);
        }
    }

}
