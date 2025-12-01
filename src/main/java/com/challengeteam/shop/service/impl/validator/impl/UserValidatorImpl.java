package com.challengeteam.shop.service.impl.validator.impl;

import com.challengeteam.shop.exceptionHandling.exception.EmailAlreadyExistsException;
import com.challengeteam.shop.persistence.repository.UserRepository;
import com.challengeteam.shop.service.impl.validator.UserValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class UserValidatorImpl implements UserValidator {
    private final UserRepository userRepository;

    @Override
    public void validateEmailIsUnique(String email) {
        boolean existsByEmail = userRepository.existsByEmail(email);
        if (existsByEmail) {
            throw new EmailAlreadyExistsException("Email already in use: " + email);
        }
    }

}
