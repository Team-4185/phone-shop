package com.challengeteam.shop.web.validator.image;

import com.challengeteam.shop.exceptionHandling.exception.UnsupportedImageContentTypeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Slf4j
@Component
public class ImageRequestValidatorImpl implements ImageRequestValidator {
    private final Set<String> allowedContentTypes;

    public ImageRequestValidatorImpl() {
        allowedContentTypes = Set.of(
                "image/jpeg",
                "image/jpg",
                "image/png"
        );
    }

    @Override
    public void validate(MultipartFile image) {
        String contentType = image.getContentType();

        log.debug("Start to validate request image content type: {} ", contentType);
        for (String allowed : allowedContentTypes) {
            if (allowed.equals(contentType)) {
                log.debug("Validation success: image content type supported");
                return;
            }
        }

        log.debug("Validation fail: image content type not supported");
        throw new UnsupportedImageContentTypeException(contentType);
    }

}
