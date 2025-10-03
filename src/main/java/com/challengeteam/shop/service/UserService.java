package com.challengeteam.shop.service;

import com.challengeteam.shop.entity.user.User;

public interface UserService {

    User getById(Long id);

    User getByEmail(String email);

    User create(User user);

    boolean existsByEmail(String email);

}
