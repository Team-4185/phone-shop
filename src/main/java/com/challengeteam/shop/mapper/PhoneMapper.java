package com.challengeteam.shop.mapper;

import com.challengeteam.shop.dto.phone.PhoneResponseDto;
import com.challengeteam.shop.entity.phone.Phone;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PhoneMapper {

    PhoneResponseDto toResponse(Phone phone);

    List<PhoneResponseDto> toResponses(List<Phone> phones);

}
