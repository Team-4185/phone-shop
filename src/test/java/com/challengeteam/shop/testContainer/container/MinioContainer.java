package com.challengeteam.shop.testContainer.container;

import com.challengeteam.shop.testContainer.TestContextConfigurator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.MinIOContainer;

@Slf4j
public class MinioContainer {
    public final static String MINIO_DOCKER_IMAGE = "minio/minio";
    public final static String MINIO_USERNAME = "test-username";
    public final static String MINIO_PASSWORD = "test-password";
    private final static MinIOContainer minio;

    static {
        minio = new MinIOContainer(MINIO_DOCKER_IMAGE)
                .withUserName(MINIO_USERNAME)
                .withPassword(MINIO_PASSWORD);

        minio.start();
        log.info("TestContainer MinIO initiated and started");

        Runtime.getRuntime().addShutdownHook(new Thread(minio::stop));
    }

    public static void init() {
        // NOOP
    }

    public static void setMinioProperties(DynamicPropertyRegistry propertyRegistry) {
        propertyRegistry.add("minio.url", minio::getS3URL);
        propertyRegistry.add("minio.username", minio::getUserName);
        propertyRegistry.add("minio.password", minio::getPassword);
    }

}
