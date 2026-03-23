package com.challengeteam.shop.service.impl;

import com.challengeteam.shop.entity.token.PasswordResetToken;
import com.challengeteam.shop.entity.user.User;
import com.challengeteam.shop.exceptionHandling.exception.InvalidTokenException;
import com.challengeteam.shop.exceptionHandling.exception.ResourceNotFoundException;
import com.challengeteam.shop.persistence.repository.PasswordResetTokenRepository;
import com.challengeteam.shop.persistence.repository.UserRepository;
import com.challengeteam.shop.service.EmailService;
import com.challengeteam.shop.service.JwtService;
import com.challengeteam.shop.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetService {

    private final UserRepository userRepository;

    private final PasswordResetTokenRepository tokenRepository;

    private final JwtService jwtService;

    private final EmailService emailService;

    private final PasswordEncoder passwordEncoder;

    @Value("${security.frontend-url}")
    private String frontendUrl;

    @Override
    public void sendResetLink(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            String token = jwtService.createResetToken(user);
            String link = frontendUrl + "/reset-password?token=" + token;
            emailService.sendResetLink(user.getEmail(), link);
        });
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Token is invalid"));

        if (!resetToken.isActive()) {
            throw new InvalidTokenException("Token already used or expired");
        }

        String email = jwtService.getEmailFromResetToken(token);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Not found user with email: " + email));

        List<PasswordResetToken> activeTokens = tokenRepository.findAllByUserId(user.getId());
        activeTokens.stream()
                .filter(PasswordResetToken::isActive)
                .forEach(PasswordResetToken::markUsed);

        tokenRepository.saveAll(activeTokens);

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

}
