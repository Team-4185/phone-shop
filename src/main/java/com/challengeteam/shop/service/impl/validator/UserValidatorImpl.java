package com.challengeteam.shop.service.impl.validator;

import com.challengeteam.shop.exception.EmailAlreadyExistsException;
import com.challengeteam.shop.repository.UserRepository;
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
            throw new EmailAlreadyExistsException("Email already in use");
        }
    }

}
