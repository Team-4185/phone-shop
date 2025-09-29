package com.challengeteam.shop.service;

import com.challengeteam.shop.dto.jwt.JwtResponse;
import com.challengeteam.shop.entity.user.Role;

public interface JwtTokenService {

    String createAccessToken(Long userId, String username, Role role);

    String createRefreshToken(Long userId, String username, Role role);

    JwtResponse refreshTokens(String refreshToken);

    boolean isValid(String token);

    String getUsernameFromToken(String token);

}
