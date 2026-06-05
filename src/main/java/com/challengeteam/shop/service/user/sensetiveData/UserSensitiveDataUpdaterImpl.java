package com.challengeteam.shop.service.user.sensetiveData;

import com.challengeteam.shop.constants.security.jwt.JwtTokenNameConstants;
import com.challengeteam.shop.dto.security.jwt.JwtResponseDto;
import com.challengeteam.shop.dto.user.request.sensetiveData.UpdateUserSensitiveDataDto;
import com.challengeteam.shop.entity.user.User;
import com.challengeteam.shop.exceptionHandling.exception.EmailAlreadyExistsException;
import com.challengeteam.shop.exceptionHandling.exception.user.InvalidUserCredentialsException;
import com.challengeteam.shop.exceptionHandling.exception.user.inputData.InvalidInputUserDataException;
import com.challengeteam.shop.persistence.repository.UserRepository;
import com.challengeteam.shop.service.security.auth.jwt.JwtService;
import com.challengeteam.shop.utility.AuthenticationUserExtractorHelper;
import com.challengeteam.shop.utility.InputNormalizer;
import com.challengeteam.shop.utility.security.jwt.RevokeTokenHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserSensitiveDataUpdaterImpl implements UserSensitiveDataUpdater {

    private final UserRepository userRepository;
    private final AuthenticationUserExtractorHelper authenticationUserExtractorHelper;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final RevokeTokenHelper revokeTokenHelper;

    @Transactional
    @Override
    public JwtResponseDto update(UpdateUserSensitiveDataDto inputDataDto,
                                 Authentication authentication,
                                 Map<String, String> tokens) {
        Optional<User> optionalUser = authenticationUserExtractorHelper.extractUserFromSecurityContextHolder(authentication);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (inputDataDto.newPassword() == null && inputDataDto.newEmail() == null) {
                log.warn("User: {} did not provide any new data", user.getEmail());
                throw new InvalidInputUserDataException("User %s did not provide any new data"
                        .formatted(user.getEmail()));
            }
            if (inputDataDto.newPassword() != null) {
                checkPasswordChangeValidity(inputDataDto, user);
                user.setPassword(passwordEncoder.encode(inputDataDto.newPassword()));
            }
            if (inputDataDto.newEmail() != null) {
                String newEmail = InputNormalizer.toEmail(inputDataDto.newEmail());
                checkEmailChangeValidity(user, newEmail);
                user.setEmail(newEmail);
            }
            User saved = userRepository.save(user);
            return updateTokensAndRevokeOld(tokens, saved);
        } else {
            log.error("User was nit authenticated: {}", authentication.getName());
            throw new InvalidUserCredentialsException("User %s was not found".formatted(authentication.getName()));
        }
    }

    private JwtResponseDto updateTokensAndRevokeOld(Map<String, String> tokens, User saved) {
        String accessToken = jwtService.createAccessToken(saved);
        String refreshToken = jwtService.createRefreshToken(saved, false);
        revokeTokenHelper.revokeToken(tokens.get(JwtTokenNameConstants.ACCESS_TOKEN_TYPE));
        revokeTokenHelper.revokeToken(tokens.get(JwtTokenNameConstants.REFRESH_TOKEN_TYPE));
        return new JwtResponseDto(saved.getId(), saved.getEmail(), accessToken, refreshToken, false);
    }

    private void checkEmailChangeValidity(User user, String newEmail) {
        if (user.getEmail().equals(newEmail)) {
            log.warn("User: {} provided same email", user.getEmail());
            throw new InvalidInputUserDataException("New email cannot be the same as the old one");
        } else if (userRepository.existsByEmail(newEmail)) {
            log.warn("User: {} provided already used email", user.getEmail());
            throw new EmailAlreadyExistsException("Email already in use");
        }
    }

    private void checkPasswordChangeValidity(UpdateUserSensitiveDataDto inputDataDto, User user) {
        if (!inputDataDto.newPassword().equals(inputDataDto.confirmPassword())) {
            log.warn("User: {} provided different passwords", user.getEmail());
            throw new InvalidInputUserDataException("Passwords do not match for user");
        } else if (!passwordEncoder.matches(inputDataDto.oldPassword(), user.getPassword())) {
            log.warn("User: {} provided wrong password", user.getEmail());
            throw new InvalidInputUserDataException("Wrong password");
        } else if (passwordEncoder.matches(inputDataDto.newPassword(), user.getPassword())) {
            log.warn("User: {} provided same password", user.getEmail());
            throw new InvalidInputUserDataException("New password cannot be the same as the old one");
        }
    }
}