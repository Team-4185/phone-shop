package com.challengeteam.shop.service;

import com.challengeteam.shop.dto.jwt.JwtResponseDto;
import com.challengeteam.shop.entity.user.User;

public interface JwtService {

    String createAccessToken(User user);

    String createRefreshToken(User user);

    JwtResponseDto refreshTokens(String refreshToken, User user);

    boolean isValid(String token);

    boolean isAccessToken(String token);

    boolean isRefreshToken(String token);

    String getEmailFromToken(String token);

}
