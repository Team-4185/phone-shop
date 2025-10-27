package com.challengeteam.shop.web;

import com.challengeteam.shop.dto.user.UserLoginRequestDto;
import com.challengeteam.shop.dto.user.UserRegisterRequestDto;
import com.challengeteam.shop.exceptionHandling.exception.EmailAlreadyExistsException;
import com.challengeteam.shop.service.JwtAuthorizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class TestAuthHelper {
    public static String TEST_COMPONENT_EMAIL = "test@gmail.com";
    public static String TEST_COMPONENT_PASSWORD = "password";

    private final JwtAuthorizationService authService;
    private final UserRegisterRequestDto registerRequest = new UserRegisterRequestDto(
            TEST_COMPONENT_EMAIL,
            TEST_COMPONENT_PASSWORD,
            TEST_COMPONENT_PASSWORD
    );
    private final UserLoginRequestDto loginRequest = new UserLoginRequestDto(
            TEST_COMPONENT_EMAIL,
            TEST_COMPONENT_PASSWORD
    );


    public String authorizeLikeTestUser() {
        try {
            // register
            return authService
                    .register(registerRequest)
                    .accessToken();
        } catch (EmailAlreadyExistsException e) {
            // if already registered,
            // then try to log in
            return authService
                    .login(loginRequest)
                    .accessToken();
        }
    }

}
