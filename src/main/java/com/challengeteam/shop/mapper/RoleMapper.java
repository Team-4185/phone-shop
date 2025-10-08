package com.challengeteam.shop.mapper;

import com.challengeteam.shop.dto.role.RoleResponseDto;
import com.challengeteam.shop.entity.user.Role;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    RoleResponseDto toResponse(Role role);

}
