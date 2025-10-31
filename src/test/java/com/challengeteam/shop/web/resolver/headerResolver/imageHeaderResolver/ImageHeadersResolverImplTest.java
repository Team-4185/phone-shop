package com.challengeteam.shop.web.resolver.headerResolver.imageHeaderResolver;

import com.challengeteam.shop.dto.image.ImageDataDto;
import com.challengeteam.shop.exceptionHandling.exception.UnsupportedImageContentTypeException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static com.challengeteam.shop.web.resolver.headerResolver.imageHeaderResolver.ImageHeadersResolverImplTest.TestResources.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ImageHeadersResolverImplTest {
    private final ImageHeadersResolverImpl imageHeadersResolver = new ImageHeadersResolverImpl();

    @Nested
    class ResolveHeadersTest {

        @Test
        void whenGivenSupportedImageType_thenReturnHeader() {
            // when
            HttpHeaders result = imageHeadersResolver.resolveHeaders(buildImageDataDto());

            // then
            assertThat(result).isEqualTo(buildValidHeaders());
        }

        @Test
        void whenGivenUnsupportedImageType_thenThrowException() {
            // when
            assertThatThrownBy(() -> imageHeadersResolver.resolveHeaders(buildInvalidImageDataDto()))
                    .isInstanceOf(UnsupportedImageContentTypeException.class);
        }

        @Test
        void whenParameterResponseBodyDataIsNull_thenThrowException() {
            // when
            assertThatThrownBy(() -> imageHeadersResolver.resolveHeaders(null))
                    .isInstanceOf(NullPointerException.class);
        }

    }

    static class TestResources {
        static final String FILENAME = "my-wonderful-file.jpeg";
        static final byte[] IMAGE_BYTES = FILENAME.getBytes();
        static final String MIME_TYPE = "image/jpeg";
        static final long SIZE = 2_500_000;

        static final String INVALID_MIME_TYPE = "image/gif";

        static final ContentDisposition CONTENT_DISPOSITION = buildContentDisposition();


        private static ContentDisposition buildContentDisposition() {
            return ContentDisposition
                    .inline()
                    .filename(FILENAME)
                    .build();
        }

        static ImageDataDto buildImageDataDto() {
            return new ImageDataDto(
                    FILENAME,
                    IMAGE_BYTES,
                    MIME_TYPE,
                    SIZE
            );
        }

        static HttpHeaders buildValidHeaders() {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG);
            headers.setContentLength(SIZE);
            headers.setContentDisposition(CONTENT_DISPOSITION);

            return headers;
        }

        static ImageDataDto buildInvalidImageDataDto() {
            return new ImageDataDto(
                    FILENAME,
                    IMAGE_BYTES,
                    INVALID_MIME_TYPE,
                    SIZE
            );
        }

    }

}