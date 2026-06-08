package com.challengeteam.shop.service;

import com.challengeteam.shop.dto.phone.response.PhoneResponseDto;
import com.challengeteam.shop.entity.phone.Phone;

import java.util.List;

public interface CatalogProductResponseAssembler {

    PhoneResponseDto toResponse(Phone phone);

    List<PhoneResponseDto> toResponses(List<Phone> phones);
}
