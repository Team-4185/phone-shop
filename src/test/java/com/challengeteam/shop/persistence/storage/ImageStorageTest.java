package com.challengeteam.shop.persistence.storage;

import com.challengeteam.shop.properties.MinioProperties;
import com.challengeteam.shop.testContainer.ContainerExtension;
import com.challengeteam.shop.testContainer.TestContextConfigurator;
import io.minio.GetObjectArgs;
import io.minio.ListObjectsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveBucketArgs;
import io.minio.RemoveObjectArgs;
import io.minio.Result;
import io.minio.messages.Item;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Optional;

import static com.challengeteam.shop.persistence.storage.ImageStorageTest.TestResources.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(ContainerExtension.class)
@SpringBootTest
class ImageStorageTest {

    @Autowired
    private MinioProperties minioProperties;
    @Autowired
    private ImageStorage imageStorage;

    @DynamicPropertySource
    private static void setContextProperties(DynamicPropertyRegistry propertyRegistry) {
        TestContextConfigurator.initRequiredProperties(propertyRegistry);
    }

    @BeforeEach
    public void setUpStorage() throws Exception {
        if (minioClient == null) {
            buildMinio(minioProperties);
            createBucket();
        }
    }

    @AfterEach
    public void cleanUpStorage() throws Exception {
        cleanStorage();
    }

    @AfterAll
    public static void removeStorage() throws Exception {
        removeBucket();
    }

    @Nested
    class ReadImageByKeyTest {

        @Test
        void whenImageExists_thenReturnOptionalWithBytes() throws Exception {
            // given
            var file = TestFile.FILE_1;
            loadImageToBucket(file);

            // when
            Optional<byte[]> result = imageStorage.readImageByKey(file.name);

            // then
            assertThat(result).isPresent();
        }

        @Test
        void whenImageNotExists_thenReturnEmptyOptional() throws Exception {
            // when
            var file = TestFile.FILE_1;
            Optional<byte[]> result = imageStorage.readImageByKey(file.name);

            // then
            assertThat(result).isNotPresent();
        }

        @Test
        void whenParameterKeyIsNull_thenThrowException() {
            // when + then
            assertThatThrownBy(() -> imageStorage.readImageByKey(null)).isInstanceOf(NullPointerException.class);
        }

    }

    @Nested
    class PutImageTest {

        @Test
        void whenStorageDoesntContainImageYet_thenPutImage() throws Exception {
            // given
            var file = TestFile.FILE_1;

            // when
            imageStorage.putImage(file.name, buildMultipartFile(file));

            // then
            assertThat(verifyFileExists(file)).isTrue();
        }

        @Test
        void whenStorageAlreadyContainsImage_thenOverwrite() throws Exception {
            // given
            var file1 = TestFile.FILE_1;
            var file2 = TestFile.FILE_2;
            MultipartFile multipartFile = buildMultipartFile(file1);
            loadImageToBucket(file2);

            // when
            imageStorage.putImage(file2.name, multipartFile);

            // then
            assertThat(compareImages(file1, file2)).isTrue();
        }

        @Test
        void whenParameterKeyIsNull_thenThrowException() {
            // given
            var file = TestFile.FILE_1;
            MultipartFile multipartFile = buildMultipartFile(file);

            // when + then
            assertThatThrownBy(() -> imageStorage.putImage(null, multipartFile))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void whenParameterMultipartFileIsNull_thenThrowException() {
            // given
            var file = TestFile.FILE_1;

            // when + then
            assertThatThrownBy(() -> imageStorage.putImage(file.name, null))
                    .isInstanceOf(NullPointerException.class);
        }

    }

    @Nested
    class DeleteImageTest {

        @Test
        void whenImageDoesExist_thenRemoveFromBucket() throws Exception {
            // given
            var file = TestFile.FILE_1;
            loadImageToBucket(file);
            assertThat(verifyFileExists(file)).isTrue();

            // when
            imageStorage.deleteImage(file.name);

            // then
            assertThat(verifyFileExists(file)).isFalse();
        }

        @Test
        void whenImageDoesntExist_thenDoNothing() throws Exception {
            // given
            var file = TestFile.FILE_1;
            assertThat(verifyFileExists(file)).isFalse();

            // when
            imageStorage.deleteImage(file.name);

            // then
            assertThat(verifyFileExists(file)).isFalse();
        }

        @Test
        void whenParameterKeyIsNull_thenThrowException() throws Exception {
            // when + then
            assertThatThrownBy(() -> imageStorage.deleteImage(null)).isInstanceOf(NullPointerException.class);
        }

    }

    static class TestResources {
        static final String IMAGE_BUCKET_NAME = "images";

        static MinioClient minioClient;

        static boolean compareImages(TestFile local, TestFile storage) throws Exception {
            var args = GetObjectArgs.builder()
                    .bucket(IMAGE_BUCKET_NAME)
                    .object(storage.name)
                    .build();

            byte[] s = minioClient.getObject(args).readAllBytes();
            byte[] l = buildMultipartFile(local).getBytes();

            return Arrays.equals(l, s);
        }

        static boolean verifyFileExists(TestFile file) throws Exception {
            var args = ListObjectsArgs.builder()
                    .bucket(IMAGE_BUCKET_NAME)
                    .prefix(file.name)
                    .maxKeys(1)
                    .build();

            Iterable<Result<Item>> results = minioClient.listObjects(args);

            for (Result<Item> result : results) {
                if (result.get().objectName().equals(file.name)) {
                    return true;
                }
            }

            return false;
        }

        static MultipartFile buildMultipartFile(TestFile file) {
            try (InputStream is = TestResources.class.getClassLoader().getResourceAsStream(file.path)) {
                if (is == null) {
                    throw new IllegalStateException("File not found");
                }

                return new MockMultipartFile(
                        "file",
                        file.name,
                        file.mimeType,
                        is.readAllBytes()
                );
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        static void loadImageToBucket(TestFile file) throws Exception {
            try (InputStream is = TestResources.class.getClassLoader().getResourceAsStream(file.path)) {
                if (is == null) {
                    throw new IllegalStateException("Not found file by path: " + file.path);
                }

                byte[] bytes = is.readAllBytes();
                var args = PutObjectArgs.builder()
                        .bucket(IMAGE_BUCKET_NAME)
                        .object(file.name)
                        .stream(new ByteArrayInputStream(bytes), bytes.length, -1)
                        .build();

                minioClient.putObject(args);
            }
        }

        static void createBucket() throws Exception {
            var args = MakeBucketArgs.builder()
                    .bucket(IMAGE_BUCKET_NAME)
                    .build();

            minioClient.makeBucket(args);
        }

        static void removeBucket() throws Exception {
            if (minioClient == null) return;

            var args = RemoveBucketArgs.builder()
                    .bucket(IMAGE_BUCKET_NAME)
                    .build();

            minioClient.removeBucket(args);
        }

        static void buildMinio(MinioProperties minioProperties) {
            String url = minioProperties.getUrl();
            String username = minioProperties.getUsername();
            String password = minioProperties.getPassword();

            minioClient = MinioClient.builder()
                    .endpoint(url)
                    .credentials(username, password)
                    .build();
        }

        static void cleanStorage() throws Exception {
            var args = ListObjectsArgs.builder()
                    .bucket(IMAGE_BUCKET_NAME)
                    .build();
            Iterable<Result<Item>> items = minioClient.listObjects(args);
            items.forEach(item -> {
                try {
                    String name = item.get().objectName();
                    var removeObjectArgs = RemoveObjectArgs.builder()
                            .bucket(IMAGE_BUCKET_NAME)
                            .object(name)
                            .build();

                    minioClient.removeObject(removeObjectArgs);
                } catch (Exception ignore) {}
            });
        }

    }

    private enum TestFile {
        FILE_1(
                "persistence/storage/mageStorage/image_1.jpg",
                "image_1.jpg",
                MediaType.IMAGE_JPEG.toString()
        ),
        FILE_2(
                "persistence/storage/mageStorage/image_2.jpeg",
                "image_2.jpeg",
                MediaType.IMAGE_JPEG.toString()
        );

        private final String path;
        private final String name;
        private final String mimeType;

        TestFile(String path, String name, String mimeType) {
            this.path = path;
            this.name = name;
            this.mimeType = mimeType;
        }
    }

}