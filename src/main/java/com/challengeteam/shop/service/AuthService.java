package com.challengeteam.shop.service;

import com.challengeteam.shop.dto.jwt.JwtRequest;
import com.challengeteam.shop.dto.jwt.JwtResponse;
import com.challengeteam.shop.dto.user.UserRegisterRequest;

public interface AuthService {

    JwtResponse register(UserRegisterRequest userRegisterRequest);

    JwtResponse login(JwtRequest jwtRequest);

    JwtResponse refresh(String refreshToken);

}
