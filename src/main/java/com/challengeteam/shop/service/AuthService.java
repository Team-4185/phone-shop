package com.challengeteam.shop.service;

import com.challengeteam.shop.dto.jwt.JwtLoginRequest;
import com.challengeteam.shop.dto.jwt.JwtResponse;
import com.challengeteam.shop.dto.user.UserRegisterRequestDto;

public interface AuthService {

    JwtResponse register(UserRegisterRequestDto userRegisterRequestDto);

    JwtResponse login(JwtLoginRequest jwtLoginRequest);

    JwtResponse refresh(String refreshToken);

}
