package com.challengeteam.shop.service;

import com.challengeteam.shop.dto.user.CreateUserDto;
import com.challengeteam.shop.dto.user.UpdateProfileDto;
import com.challengeteam.shop.entity.user.User;

import java.util.List;
import java.util.Optional;

public interface UserService {

    List<User> getAll();

    Optional<User> getById(Long id);

    Optional<User> getByEmail(String email);

    Long createDefaultUser(CreateUserDto createUserDto);

    void updateProfile(Long id, UpdateProfileDto updateProfileDto);

    void delete(Long id);

    boolean existsByEmail(String email);

}
