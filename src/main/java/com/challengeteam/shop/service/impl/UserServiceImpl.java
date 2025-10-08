package com.challengeteam.shop.service.impl;

import com.challengeteam.shop.dto.user.CreateUserDto;
import com.challengeteam.shop.dto.user.UpdateProfileDto;
import com.challengeteam.shop.entity.user.Role;
import com.challengeteam.shop.entity.user.User;
import com.challengeteam.shop.exception.ResourceNotFoundException;
import com.challengeteam.shop.repository.RoleRepository;
import com.challengeteam.shop.repository.UserRepository;
import com.challengeteam.shop.service.UserService;
import com.challengeteam.shop.service.impl.merger.UserMerger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private static final String DEFAULT_ROLE_NAME_FOR_CREATED_USER = "USER";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMerger userMerger;


    @Transactional(readOnly = true)
    @Override
    public List<User> getAll() {
        log.debug("Get all users");
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<User> getById(Long id) {
        Objects.requireNonNull(id, "id");

        log.debug("Get user by id: {}", id);
        return userRepository.findById(id);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<User> getByEmail(String email) {
        Objects.requireNonNull(email, "email");

        log.debug("Get user by email: {}", email);
        return userRepository.findByEmail(email);
    }

    @Transactional
    @Override
    public Long createDefaultUser(CreateUserDto createUserDto) {
        Objects.requireNonNull(createUserDto, "createUserDto");

        String encoded = passwordEncoder.encode(createUserDto.password());
        Role defaultRole = roleRepository
                .findByName(DEFAULT_ROLE_NAME_FOR_CREATED_USER)
                .orElseThrow(() -> new ResourceNotFoundException("Not found default role for new user " + createUserDto.email()));

        var user = User.builder()
                .email(createUserDto.email())
                .password(encoded)
                .role(defaultRole)
                .build();
        user = userRepository.save(user);
        log.debug("Created new user: {}", user);
        return user.getId();
    }

    @Transactional
    @Override
    public void updateProfile(Long id, UpdateProfileDto updateProfileDto) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(updateProfileDto, "updateProfileDto");

        User user = userRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not found user with id: " + id));

        userMerger.mergeProfile(user, updateProfileDto);
        userRepository.save(user);
        log.debug("Updated user profile: {}", user);
    }

    @Transactional
    @Override
    public void delete(Long id) {
        Objects.requireNonNull(id, "id");

        userRepository.deleteById(id);
        log.debug("Deleted user with id: {}", id);
    }

    @Transactional(readOnly = true)
    @Override
    public boolean existsByEmail(String email) {
        log.debug("Called method 'existsByEmail' with email: {}", email);
        return userRepository.existsByEmail(email);
    }

}
