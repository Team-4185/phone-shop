package com.challengeteam.shop.mapper;

import com.challengeteam.shop.dto.user.UserRegisterRequestDto;
import com.challengeteam.shop.dto.user.UserResponseDto;
import com.challengeteam.shop.entity.user.User;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = {
        RoleMapper.class
})
public interface UserMapper {

    UserRegisterRequestDto toUserRegisterRequest(User user);

    User toUser(UserRegisterRequestDto userRegisterRequestDto);

    UserResponseDto toResponse(User user);

    List<UserResponseDto> toResponses(List<User> user);

}
