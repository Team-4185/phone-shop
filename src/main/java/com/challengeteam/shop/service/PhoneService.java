package com.challengeteam.shop.service;

import com.challengeteam.shop.dto.phone.PhoneCreateRequestDto;
import com.challengeteam.shop.dto.phone.PhoneUpdateRequestDto;
import com.challengeteam.shop.entity.image.Image;
import com.challengeteam.shop.entity.phone.Phone;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface PhoneService {

    Optional<Phone> getById(Long id);
    Page<Phone> getPhones(int page, int size);
    Long create(PhoneCreateRequestDto phoneCreateRequestDto, List<MultipartFile> images);
    void update(Long id, PhoneUpdateRequestDto phoneUpdateRequestDto);
    void delete(Long id);
    void addImageToPhone(Long phoneId, MultipartFile newImage);
    List<Image> getPhoneImages(Long phoneId);
    void deletePhonesImageById(Long phoneId, Long imageId);

}
