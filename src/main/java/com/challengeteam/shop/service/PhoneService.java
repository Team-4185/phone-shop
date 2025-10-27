package com.challengeteam.shop.service;

import com.challengeteam.shop.dto.phone.PhoneCreateRequestDto;
import com.challengeteam.shop.dto.phone.PhoneUpdateRequestDto;
import com.challengeteam.shop.entity.phone.Phone;

import java.util.List;
import java.util.Optional;

public interface PhoneService {

    Optional<Phone> getById(Long id);

    List<Phone> getAll();

    Long create(PhoneCreateRequestDto phoneCreateRequestDto);

    void update(Long id, PhoneUpdateRequestDto phoneUpdateRequestDto);

    void delete(Long id);

}
