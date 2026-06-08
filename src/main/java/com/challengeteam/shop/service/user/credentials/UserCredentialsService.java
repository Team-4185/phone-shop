package com.challengeteam.shop.service.user.credentials;

import com.challengeteam.shop.dto.user.request.ChangeEmailRequestDto;
import com.challengeteam.shop.dto.user.request.ChangePasswordRequestDto;
import org.springframework.security.core.Authentication;

public interface UserCredentialsService {

    void changePassword(ChangePasswordRequestDto request, Authentication authentication, String accessToken);

    void changeEmail(ChangeEmailRequestDto request, Authentication authentication, String accessToken);
}
