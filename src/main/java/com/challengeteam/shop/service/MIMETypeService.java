package com.challengeteam.shop.service;

import com.challengeteam.shop.entity.image.MIMEType;
import org.springframework.web.multipart.MultipartFile;

public interface MIMETypeService {

    MIMEType createIfDoesntExist(MultipartFile file);

}
