package com.challengeteam.shop.web.resolver.headerResolver.imageHeaderResolver;

import com.challengeteam.shop.dto.image.ImageDataDto;
import com.challengeteam.shop.exceptionHandling.exception.UnsupportedImageContentTypeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Set;

@Slf4j
@Component
public class ImageHeadersResolverImpl implements ImageHeadersResolver {
    private final Set<ContentTypeResolveStrategy> contentTypeResolveStrategies;

    public ImageHeadersResolverImpl() {
        // fill strategies manually
        contentTypeResolveStrategies = Set.of(
                new JpegContentTypeResolverStrategy(),
                new PngContentTypeResolverStrategy()
        );
    }

    @Override
    public HttpHeaders resolveHeaders(ImageDataDto responseBody) {
        HttpHeaders headers = new HttpHeaders();

        resolveContentType(responseBody, headers);
        resolveContentLength(responseBody, headers);
        resolveContentDisposition(responseBody, headers);

        return headers;
    }

    private void resolveContentType(ImageDataDto dto, HttpHeaders headers) {
        for (ContentTypeResolveStrategy strategy : contentTypeResolveStrategies) {
            if (strategy.supportImageMIMEType(dto)) {
                strategy.resolveImageContentType(headers);

                log.debug("Successfully resolved content-type: {} for image: {}", strategy.getSupportedMimeType(), dto.filename());
                return;
            }
        }

        log.warn("Failed to resolve Content-Type: {}, because of unsupported", dto.mimeType());
        throw new UnsupportedImageContentTypeException(dto.mimeType());
    }

    private void resolveContentLength(ImageDataDto dto, HttpHeaders headers) {
        headers.setContentLength(dto.size());
    }

    private void resolveContentDisposition(ImageDataDto dto, HttpHeaders headers) {
        var contentDisposition = ContentDisposition
                .inline()
                .filename(dto.filename())
                .build();

        headers.setContentDisposition(contentDisposition);
    }

    private abstract static class ContentTypeResolveStrategy {

        abstract void resolveImageContentType(HttpHeaders headers);

        abstract boolean supportImageMIMEType(ImageDataDto dto);

        abstract String getSupportedMimeType();

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != getClass()) return false;
            var other = (ContentTypeResolveStrategy) obj;

            return Objects.equals(getSupportedMimeType(), other.getSupportedMimeType());
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(getSupportedMimeType());
        }

    }

    private static class JpegContentTypeResolverStrategy extends ContentTypeResolveStrategy {
        private static final String MIME_TYPE = "image/jpeg";
        private static final String ALTERNATIVE_MIME_TYPE = "image/jpg";

        @Override
        public void resolveImageContentType(HttpHeaders headers) {
            headers.setContentType(MediaType.IMAGE_JPEG);
        }

        @Override
        public boolean supportImageMIMEType(ImageDataDto dto) {
            return Objects.equals(dto.mimeType(), MIME_TYPE)
                   || Objects.equals(dto.mimeType(), ALTERNATIVE_MIME_TYPE);
        }

        @Override
        public String getSupportedMimeType() {
            return MIME_TYPE;
        }

    }

    private static class PngContentTypeResolverStrategy extends ContentTypeResolveStrategy {
        private static final String MIME_TYPE = "image/png";

        @Override
        public void resolveImageContentType(HttpHeaders headers) {
            headers.setContentType(MediaType.IMAGE_PNG);
        }

        @Override
        public boolean supportImageMIMEType(ImageDataDto dto) {
            return Objects.equals(dto.mimeType(), MIME_TYPE);
        }

        @Override
        public String getSupportedMimeType() {
            return MIME_TYPE;
        }

    }

}
