package com.challengeteam.shop.service;

import com.challengeteam.shop.dto.jwt.JwtResponse;
import com.challengeteam.shop.entity.user.Role;
import org.springframework.security.core.Authentication;

public interface JwtTokenService {

    String createAccessToken(Long userId, String username, Role role);

    String createRefreshToken(Long userId, String username);

    JwtResponse refreshTokens(String refreshToken);

    Authentication getAuthentication(String token);

    boolean isValid(String token);

}
