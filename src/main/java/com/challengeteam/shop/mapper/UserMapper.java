package com.challengeteam.shop.mapper;

import com.challengeteam.shop.dto.user.UserRegisterRequest;
import com.challengeteam.shop.entity.user.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserRegisterRequest toUserRegisterRequest(User user);

    User toUser(UserRegisterRequest userRegisterRequest);

}
