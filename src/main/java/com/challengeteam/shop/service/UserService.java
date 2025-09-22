package com.challengeteam.shop.service;

import com.challengeteam.shop.dto.user.UserRegisterRequest;
import com.challengeteam.shop.entity.user.User;

public interface UserService {

    User getById(Long id);

    User getByUsername(String username);

    User create(UserRegisterRequest userRegisterRequest);

    boolean existsByUsername(String username);

}
