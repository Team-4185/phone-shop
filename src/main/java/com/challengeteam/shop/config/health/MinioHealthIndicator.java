package com.challengeteam.shop.config.health;

import com.challengeteam.shop.persistence.storage.MinioImageStorage;
import com.challengeteam.shop.properties.MinioProperties;
import io.minio.BucketExistsArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component("minio")
@RequiredArgsConstructor
public class MinioHealthIndicator implements HealthIndicator {

    private final MinioProperties minioProperties;

    @Override
    public Health health() {
        try (MinioClient minioClient = MinioClient.builder()
                .endpoint(minioProperties.getUrl())
                .credentials(minioProperties.getUsername(), minioProperties.getPassword())
                .build()) {
            boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(MinioImageStorage.IMAGE_BUCKET_NAME)
                    .build());

            if (bucketExists) {
                return Health.up()
                        .withDetail("bucket", MinioImageStorage.IMAGE_BUCKET_NAME)
                        .build();
            }
            return Health.down()
                    .withDetail("bucket", MinioImageStorage.IMAGE_BUCKET_NAME)
                    .withDetail("reason", "Bucket is missing")
                    .build();
        } catch (Exception e) {
            return Health.down(e).build();
        }
    }
}
