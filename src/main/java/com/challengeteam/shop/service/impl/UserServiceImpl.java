package com.challengeteam.shop.service.impl;

import com.challengeteam.shop.entity.user.Role;
import com.challengeteam.shop.entity.user.User;
import com.challengeteam.shop.exception.ResourceNotFoundException;
import com.challengeteam.shop.repository.RoleRepository;
import com.challengeteam.shop.repository.UserRepository;
import com.challengeteam.shop.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private static final String DEFAULT_ROLE_NAME_FOR_CREATED_USER = "USER";

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;


    @Transactional(readOnly = true)
    @Override
    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found"));
    }

    @Transactional(readOnly = true)
    @Override
    public User email(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User with email " + email + " not found"));
    }

    @Transactional
    @Override
    public User create(User user) {
        Role defaultRole = roleRepository
                .findByName(DEFAULT_ROLE_NAME_FOR_CREATED_USER)
                .orElseThrow(() -> new ResourceNotFoundException("Not found default role for new user " + user.getEmail()));

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(defaultRole);
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

}
