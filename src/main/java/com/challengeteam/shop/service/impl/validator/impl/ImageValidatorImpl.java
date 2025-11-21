package com.challengeteam.shop.service.impl.validator.impl;

import com.challengeteam.shop.exceptionHandling.exception.UnsupportedImageContentTypeException;
import com.challengeteam.shop.service.impl.validator.ImageValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;
import java.util.Set;

@Slf4j
@Component
public class ImageValidatorImpl implements ImageValidator {
    private final Set<String> allowedContentTypes;

    public ImageValidatorImpl() {
        allowedContentTypes = Set.of(
                "image/jpeg",
                "image/jpg",
                "image/png"
        );
    }

    @Override
    public void validate(MultipartFile image) {
        Objects.requireNonNull(image, "image");

        String contentType = image.getContentType();
        log.debug("Start to validate image content type: {} ", contentType);
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
