package com.challengeteam.shop.service.impl;

import com.challengeteam.shop.entity.user.Role;
import com.challengeteam.shop.entity.user.User;
import com.challengeteam.shop.exception.ResourceNotFoundException;
import com.challengeteam.shop.repository.UserRepository;
import com.challengeteam.shop.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    @Override
    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found"));
    }

    @Override
    public User getByUsername(String username) {
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User with username " + username + " not found"));
    }

    @Override
    public User create(User user) {
        var role = new Role();
        role.setId(1L);
        role.setName("USER");

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(role);
        return userRepository.save(user);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByEmail(username);
    }

}
