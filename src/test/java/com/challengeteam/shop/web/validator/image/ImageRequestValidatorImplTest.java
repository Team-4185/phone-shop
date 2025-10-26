package com.challengeteam.shop.web.validator.image;

import com.challengeteam.shop.exceptionHandling.exception.UnsupportedImageContentTypeException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import static com.challengeteam.shop.web.validator.image.ImageRequestValidatorImplTest.TestResources.buildInvalidMultipartFile;
import static com.challengeteam.shop.web.validator.image.ImageRequestValidatorImplTest.TestResources.buildMultipartFile;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class ImageRequestValidatorImplTest {
    private final ImageRequestValidatorImpl imageRequestValidator = new ImageRequestValidatorImpl();

    @Nested
    class ValidateClass {

        @Test
        void whenGivenValidMultipartFile_thenDoNothing() {
            // when + then
            imageRequestValidator.validate(buildMultipartFile());
        }

        @Test
        void whenGivenInvalidMultipartFile_thenThrowException() {
            // when + then
            assertThatThrownBy(() -> imageRequestValidator.validate(buildInvalidMultipartFile()))
                    .isInstanceOf(UnsupportedImageContentTypeException.class);
        }

        @Test
        void whenParameterImageIsNull_thenThrowException() {
            // when + then
            assertThatThrownBy(() -> imageRequestValidator.validate(null))
                    .isInstanceOf(NullPointerException.class);
        }

    }

    static class TestResources {
        static final String FILENAME = "file";
        static final String ORIGINAL_FILENAME = "file.jpeg";
        static final String CONTENT_TYPE = "image/jpeg";
        static final byte[] CONTENT = ORIGINAL_FILENAME.getBytes();

        static final String UNSUPPORTED_CONTENT_TYPE = "image/gif";

        static MultipartFile buildMultipartFile() {
            return new MockMultipartFile(
                    FILENAME,
                    ORIGINAL_FILENAME,
                    CONTENT_TYPE,
                    CONTENT
            );
        }

        static MultipartFile buildInvalidMultipartFile() {
            return new MockMultipartFile(
                    FILENAME,
                    ORIGINAL_FILENAME,
                    UNSUPPORTED_CONTENT_TYPE,
                    CONTENT
            );
        }

    }

}