package com.challengeteam.shop.service;

import com.challengeteam.shop.dto.auth.UserLoginRequestDto;
import com.challengeteam.shop.dto.jwt.JwtResponseDto;
import com.challengeteam.shop.dto.auth.UserRegisterRequestDto;

public interface JwtAuthorizationService {

    JwtResponseDto register(UserRegisterRequestDto userRegisterRequestDto);

    JwtResponseDto login(UserLoginRequestDto userLoginRequestDto);

    JwtResponseDto refresh(String refreshToken);

}
