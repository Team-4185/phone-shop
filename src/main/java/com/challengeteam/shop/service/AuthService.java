package com.challengeteam.shop.service;

import com.challengeteam.shop.dto.jwt.JwtLoginRequest;
import com.challengeteam.shop.dto.jwt.JwtResponse;
import com.challengeteam.shop.dto.user.UserRegisterRequest;

public interface AuthService {

    JwtResponse register(UserRegisterRequest userRegisterRequest);

    JwtResponse login(JwtLoginRequest jwtLoginRequest);

    JwtResponse refresh(String refreshToken);

}
