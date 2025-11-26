package com.challengeteam.shop.service.impl;

import com.challengeteam.shop.dto.image.ImageDataDto;
import com.challengeteam.shop.entity.image.Image;
import com.challengeteam.shop.entity.image.MIMEType;
import com.challengeteam.shop.exceptionHandling.exception.CriticalSystemException;
import com.challengeteam.shop.exceptionHandling.exception.ImageStorageException;
import com.challengeteam.shop.exceptionHandling.exception.ResourceNotFoundException;
import com.challengeteam.shop.exceptionHandling.exception.UnsupportedImageContentTypeException;
import com.challengeteam.shop.persistence.repository.ImageRepository;
import com.challengeteam.shop.persistence.storage.ImageStorage;
import com.challengeteam.shop.service.MIMETypeService;
import com.challengeteam.shop.service.impl.validator.ImageValidator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;
import java.util.Optional;

import static com.challengeteam.shop.service.impl.ImageServiceImplTest.TestResources.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class ImageServiceImplTest {
    @Mock
    private ImageStorage imageStorage;
    @Mock
    private ImageRepository imageRepository;
    @Mock
    private MIMETypeService mimeTypeService;
    @Mock
    private ImageValidator imageValidator;
    @InjectMocks
    private ImageServiceImpl imageService;

    @Nested
    class DownloadImageByIdTest {

        @Test
        void whenImageFoundById_thenReturnOptionalWithImageDataDto() throws Exception {
            // mockito
            Mockito.when(imageRepository.findById(ID))
                    .thenReturn(Optional.of(buildImage()));
            Mockito.when(imageStorage.readImageByKey(KEY))
                    .thenReturn(Optional.of(BYTES));

            // when
            Optional<ImageDataDto> result = imageService.downloadImageById(ID);

            // then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(buildImageDataDto());
        }

        @Test
        void whenImageNotFoundById_thenReturnEmptyOptional() {
            // mockito
            Mockito.when(imageRepository.findById(ID))
                    .thenReturn(Optional.empty());

            // when
            Optional<ImageDataDto> result = imageService.downloadImageById(ID);

            // then
            assertThat(result).isNotPresent();
        }

        @Test
        void whenImageExistsInRepositoryButDoesntInStorage_thenThrowCriticalException() throws Exception {
            // mockito
            Mockito.when(imageRepository.findById(ID))
                    .thenReturn(Optional.of(buildImage()));
            Mockito.when(imageStorage.readImageByKey(KEY))
                    .thenReturn(Optional.empty());

            // when + then
            assertThatThrownBy(() -> imageService.downloadImageById(ID)).isInstanceOf(CriticalSystemException.class);
        }

        @Test
        void whenIdIsNull_thenThrowException() {
            // when + then
            assertThatThrownBy(() -> imageService.downloadImageById(null)).isInstanceOf(NullPointerException.class);
        }

    }

    @Nested
    class GetImageByIdTest {

        @Test
        void whenImageExists_thenReturnOptionalWithImage() {
            // mockito
            Mockito.when(imageRepository.findById(ID))
                    .thenReturn(Optional.of(buildImage()));

            // when
            Optional<Image> result = imageService.getImageById(ID);

            // then
            assertThat(result).isPresent();
            assertThat(isImagesEquals(result.get(), buildImage())).isTrue();
        }

        @Test
        void whenImageDoesntExist_thenThrowException() {
            // mockito
            Mockito.when(imageRepository.findById(ID))
                    .thenReturn(Optional.empty());

            // when
            Optional<Image> result = imageService.getImageById(ID);

            // then
            assertThat(result).isNotPresent();
        }

        @Test
        void whenParameterIdIsNull_thenThrowException() {
            // when + then
            assertThatThrownBy(() -> imageService.getImageById(null)).isInstanceOf(NullPointerException.class);
        }

    }

    @Nested
    class UploadImageTest {

        @Test
        void whenImageDoesntExist_thenCreateAndStoreImage() {
            // given
            MultipartFile multipartFile = buildMultipartFile();

            // mockito
            Mockito.when(mimeTypeService.createIfDoesntExist(multipartFile))
                    .thenReturn(MIME_TYPE);
            Mockito.when(imageRepository.save(buildImageWithoutId()))
                    .thenReturn(buildImage());

            // when
            Image result = imageService.uploadImage(multipartFile);

            // then
            assertThat(result.getId()).isEqualTo(ID);
        }

        @Test
        void whenImageIsNotValid_thenThrowException() {
            // mockito
            Mockito.doThrow(new UnsupportedImageContentTypeException("invalid/type"))
                    .when(imageValidator)
                    .validate(any(MultipartFile.class));

            // when + then
            assertThatThrownBy(() -> imageService.uploadImage(buildInvalidMultipartFile()))
                    .isInstanceOf(UnsupportedImageContentTypeException.class);
        }

        @Test
        void whenParameterMultipartFileIsNull_thenThrowException() {
            // when + then
            assertThatThrownBy(() -> imageService.uploadImage(null)).isInstanceOf(NullPointerException.class);
        }

    }

    @Nested
    class DeleteImage {

        @Test
        void whenImageExists_thenDeleteImage() throws ImageStorageException {
            // mockito
            Mockito.when(imageRepository.findById(ID))
                    .thenReturn(Optional.of(buildImage()));

            // when
            imageService.deleteImage(ID);

            // then
            Mockito.verify(imageStorage).deleteImage(KEY);
            Mockito.verify(imageRepository).deleteById(ID);
        }

        @Test
        void whenImageDoesntExist_thenThrowException() {
            // mockito
            Mockito.when(imageRepository.findById(ID))
                    .thenReturn(Optional.empty());

            // when + then
            assertThatThrownBy(() -> imageService.deleteImage(ID)).isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        void whenParameterImageIdIsNull_thenThrowException() {
            // when + then
            assertThatThrownBy(() -> imageService.deleteImage(null)).isInstanceOf(NullPointerException.class);
        }

    }

    static class TestResources {
        static final long ID = 10L;
        static final String KEY = "image.jpg";
        static final String NAME = "image.jpg";
        static final MIMEType MIME_TYPE = buildMIMEType();
        static final long SIZE = buildBytes().length;
        static final byte[] BYTES = buildBytes();

        static final String INVALID_MIME_TYPE = "image/invalid-image-type";

        static MultipartFile buildMultipartFile() {
            return new MockMultipartFile(
                    "file",
                    NAME,
                    MIME_TYPE.getType(),
                    BYTES
            );
        }

        static MultipartFile buildInvalidMultipartFile() {
            return new MockMultipartFile(
                    "file",
                    NAME,
                    INVALID_MIME_TYPE,
                    BYTES
            );
        }

        static boolean isImagesEquals(Image image1, Image image2) {
            if (!image1.equals(image2)) {
                return false;
            }

            if (!image1.getName().equals(image2.getName())) {
                return false;
            }

            if (!image1.getStorageKey().equals(image2.getStorageKey())) {
                return false;
            }
            if (!Objects.equals(image1.getSize(), image2.getSize())) {
                return false;
            }
            if (!isMIMETypesEquals(image1.getMimeType(), image2.getMimeType())) {
                return false;
            }

            return true;
        }

        private static boolean isMIMETypesEquals(MIMEType type1, MIMEType type2) {
            if (!type1.equals(type2)) {
                return false;
            }
            if (!type1.getExtension().equals(type2.getExtension())) {
                return false;
            }
            if (!type1.getType().equals(type2.getType())) {
                return false;
            }

            return true;
        }

        static ImageDataDto buildImageDataDto() {
            return new ImageDataDto(
                    NAME,
                    BYTES,
                    MIME_TYPE.getType(),
                    SIZE
            );
        }

        static Image buildImageWithoutId() {
            return Image.builder()
                    .name(NAME)
                    .storageKey(KEY)
                    .mimeType(MIME_TYPE)
                    .size(SIZE)
                    .build();
        }

        static Image buildImage() {
            Image image = buildImageWithoutId();
            image.setId(ID);

            return image;
        }

        private static byte[] buildBytes() {
            // it is enough for test
            return KEY.getBytes();
        }

        private static MIMEType buildMIMEType() {
            return MIMEType.builder()
                    .id(1L)
                    .extension(".jpg")
                    .type("image/jpeg")
                    .build();
        }
    }

}