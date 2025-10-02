package com.challengeteam.shop.service;

import com.challengeteam.shop.entity.user.User;

public interface UserService {

    User getById(Long id);

    User email(String username);

    User create(User user);

    boolean existsByEmail(String username);

}
