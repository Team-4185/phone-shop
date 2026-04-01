package com.challengeteam.shop.mapper;

import com.challengeteam.shop.dto.phone.PhoneResponseDto;
import com.challengeteam.shop.entity.phone.Phone;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {ImageMapper.class})
public interface PhoneMapper {

    @Mapping(source = "phoneCharacteristics.cpu", target = "cpu")
    @Mapping(source = "phoneCharacteristics.coresNumber", target = "coresNumber")
    @Mapping(source = "phoneCharacteristics.screenSize", target = "screenSize")
    @Mapping(source = "phoneCharacteristics.frontCamera", target = "frontCamera")
    @Mapping(source = "phoneCharacteristics.mainCamera", target = "mainCamera")
    @Mapping(source = "phoneCharacteristics.batteryCapacity", target = "batteryCapacity")
    @Mapping(source = "images", target = "images")
    PhoneResponseDto toResponse(Phone phone);

}
