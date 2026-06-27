package com.challengeteam.shop.web;

import com.challengeteam.shop.dto.auth.UserLoginRequestDto;
import com.challengeteam.shop.dto.auth.UserRegisterRequestDto;
import com.challengeteam.shop.dto.security.jwt.JwtResponseDto;
import com.challengeteam.shop.entity.user.Role;
import com.challengeteam.shop.entity.user.User;
import com.challengeteam.shop.exceptionHandling.exception.EmailAlreadyExistsException;
import com.challengeteam.shop.persistence.repository.RoleRepository;
import com.challengeteam.shop.persistence.repository.UserRepository;
import com.challengeteam.shop.service.security.auth.authorization.JwtAuthorizationService;
import com.challengeteam.shop.service.security.auth.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class TestAuthHelper {
    public static String TEST_COMPONENT_EMAIL = "test@gmail.com";
    public static String TEST_COMPONENT_PASSWORD = "password";
    public static boolean TEST_COMPONENT_REMEMBER_ME_FLAG = false;

    private final JwtAuthorizationService authService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRegisterRequestDto registerRequest = new UserRegisterRequestDto(
            TEST_COMPONENT_EMAIL,
            TEST_COMPONENT_PASSWORD,
            TEST_COMPONENT_PASSWORD
    );
    private final UserLoginRequestDto loginRequest = new UserLoginRequestDto(
            TEST_COMPONENT_EMAIL,
            TEST_COMPONENT_PASSWORD,
            TEST_COMPONENT_REMEMBER_ME_FLAG
    );


    public String authorizeLikeTestUser() {
        return authorizeAndReturnTokens().accessToken();
    }

    public JwtResponseDto authorizeAndReturnTokens() {
        try {
            // register
            return authService.register(registerRequest);
        } catch (EmailAlreadyExistsException e) {
            // if already registered,
            // then try to log in
            return authService.login(loginRequest);
        }
    }

    public String authorizeAsNewUser(String email, String password) {
        UserRegisterRequestDto register = new UserRegisterRequestDto(email, password, password);
        UserLoginRequestDto login = new UserLoginRequestDto(email, password, false);
        try {
            return authService.register(register).accessToken();
        } catch (EmailAlreadyExistsException e) {
            return authService.login(login).accessToken();
        }
    }

    public String authorizeAsAdmin(String email) {
        return userRepository.findByEmail(email)
                .map(jwtService::createAccessToken)
                .orElseGet(() -> jwtService.createAccessToken(userRepository.save(adminUser(email))));
    }

    private User adminUser(String email) {
        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseThrow(() -> new IllegalStateException("Admin role is missing in test database"));
        return User.builder()
                .email(email)
                .password(passwordEncoder.encode("AdminPassword123!"))
                .role(adminRole)
                .build();
    }
}
