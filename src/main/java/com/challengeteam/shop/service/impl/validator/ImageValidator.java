package com.challengeteam.shop.service.impl.validator;

import org.springframework.web.multipart.MultipartFile;

public interface ImageValidator {

    void validate(MultipartFile image);

}
