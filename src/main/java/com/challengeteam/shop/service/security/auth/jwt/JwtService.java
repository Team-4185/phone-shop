package com.challengeteam.shop.service.security.auth.jwt;

import com.challengeteam.shop.dto.security.jwt.JwtResponseDto;
import com.challengeteam.shop.entity.user.User;

import java.time.Instant;

public interface JwtService {

    String createAccessToken(User user);

    String createRefreshToken(User user, boolean rememberMe);

    JwtResponseDto refreshTokens(String refreshToken, User user);

    boolean isValid(String token);

    boolean isAccessToken(String token);

    boolean isRefreshToken(String token);

    String getEmailFromToken(String token);

    String createResetToken(User user);

    String getEmailFromResetToken(String token);

    Instant getExpiration(String token);
}