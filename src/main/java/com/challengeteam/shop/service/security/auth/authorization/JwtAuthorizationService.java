package com.challengeteam.shop.service.security.auth.authorization;

import com.challengeteam.shop.dto.auth.UserLoginRequestDto;
import com.challengeteam.shop.dto.auth.UserRegisterRequestDto;
import com.challengeteam.shop.dto.security.jwt.JwtResponseDto;

public interface JwtAuthorizationService {

    JwtResponseDto register(UserRegisterRequestDto userRegisterRequestDto);

    JwtResponseDto login(UserLoginRequestDto userLoginRequestDto);

    JwtResponseDto refresh(String refreshToken);

}
