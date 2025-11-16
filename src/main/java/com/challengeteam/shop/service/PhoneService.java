package com.challengeteam.shop.service;

import com.challengeteam.shop.dto.phone.PhoneCreateRequestDto;
import com.challengeteam.shop.dto.phone.PhoneUpdateRequestDto;
import com.challengeteam.shop.entity.phone.Phone;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface PhoneService {

    Optional<Phone> getById(Long id);

    Page<Phone> getAllPhones(int page, int size);

    Long create(PhoneCreateRequestDto phoneCreateRequestDto);

    void update(Long id, PhoneUpdateRequestDto phoneUpdateRequestDto);

    void delete(Long id);

}
