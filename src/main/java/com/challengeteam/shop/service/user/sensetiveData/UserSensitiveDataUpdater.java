package com.challengeteam.shop.service.user.sensetiveData;

import com.challengeteam.shop.dto.security.jwt.JwtResponseDto;
import com.challengeteam.shop.dto.user.request.sensetiveData.UpdateUserSensitiveDataDto;
import org.springframework.security.core.Authentication;

import java.util.Map;

public interface UserSensitiveDataUpdater {
    JwtResponseDto update(UpdateUserSensitiveDataDto updateUserSensitiveData,
                          Authentication authentication,
                          Map<String, String> tokens);
}