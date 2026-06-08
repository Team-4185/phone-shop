package com.challengeteam.shop.service.user.credentials;

import com.challengeteam.shop.dto.user.request.ChangeEmailRequestDto;
import com.challengeteam.shop.dto.user.request.ChangePasswordRequestDto;
import com.challengeteam.shop.entity.user.User;
import com.challengeteam.shop.exceptionHandling.exception.EmailAlreadyExistsException;
import com.challengeteam.shop.exceptionHandling.exception.user.InvalidUserCredentialsException;
import com.challengeteam.shop.exceptionHandling.exception.user.inputData.InvalidInputUserDataException;
import com.challengeteam.shop.persistence.repository.UserRepository;
import com.challengeteam.shop.utility.AuthenticationUserExtractorHelper;
import com.challengeteam.shop.utility.InputNormalizer;
import com.challengeteam.shop.utility.security.jwt.RevokeTokenHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserCredentialsServiceImpl implements UserCredentialsService {

    private final UserRepository userRepository;
    private final AuthenticationUserExtractorHelper authenticationUserExtractorHelper;
    private final PasswordEncoder passwordEncoder;
    private final RevokeTokenHelper revokeTokenHelper;

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequestDto request, Authentication authentication, String accessToken) {
        User user = getAuthenticatedUser(authentication);

        validateCurrentPassword(request.currentPassword(), user, "changing password");
        validateNewPassword(request, user);
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        rotateCredentialsVersion(user, accessToken);
        log.info("Password changed for user id={}", user.getId());
    }

    @Override
    @Transactional
    public void changeEmail(ChangeEmailRequestDto request, Authentication authentication, String accessToken) {
        User user = getAuthenticatedUser(authentication);
        String newEmail = InputNormalizer.toEmail(request.newEmail());

        validateCurrentPassword(request.currentPassword(), user, "changing email");
        validateNewEmail(user, newEmail);
        user.setEmail(newEmail);
        rotateCredentialsVersion(user, accessToken);
        log.info("Email changed for user id={}", user.getId());
    }

    private User getAuthenticatedUser(Authentication authentication) {
        return authenticationUserExtractorHelper.extractUserFromSecurityContextHolder(authentication)
                .orElseThrow(() -> new InvalidUserCredentialsException("Authenticated user was not found"));
    }

    private void validateCurrentPassword(String currentPassword, User user, String action) {
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            log.warn("User {} provided wrong current password while {}", user.getEmail(), action);
            throw new InvalidInputUserDataException("Current password is incorrect");
        }
    }

    private void validateNewPassword(ChangePasswordRequestDto request, User user) {
        if (!request.newPassword().equals(request.confirmNewPassword())) {
            log.warn("User {} provided non-matching new password confirmation", user.getEmail());
            throw new InvalidInputUserDataException("New password and confirmation do not match");
        }
        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            log.warn("User {} tried to reuse current password", user.getEmail());
            throw new InvalidInputUserDataException("New password must differ from current password");
        }
    }

    private void validateNewEmail(User user, String newEmail) {
        if (user.getEmail().equals(newEmail)) {
            log.warn("User {} tried to reuse current email", user.getEmail());
            throw new InvalidInputUserDataException("New email must differ from current email");
        }
        if (userRepository.existsByEmail(newEmail)) {
            log.warn("User {} tried to change email to already used email {}", user.getEmail(), newEmail);
            throw new EmailAlreadyExistsException("Email is already in use");
        }
    }

    private void rotateCredentialsVersion(User user, String accessToken) {
        user.incrementTokenVersion();
        userRepository.save(user);
        revokeTokenHelper.revokeToken(accessToken);
    }
}
