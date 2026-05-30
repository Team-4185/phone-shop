package com.challengeteam.shop.mapper.order;

import com.challengeteam.shop.dto.order.response.user.UserResponseDto;
import com.challengeteam.shop.entity.user.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserOrderMapper {
    UserResponseDto toUserResponseDto(User user);
}