package com.challengeteam.shop.service.impl;

import com.challengeteam.shop.dto.jwt.JwtResponseDto;
import com.challengeteam.shop.dto.user.CreateUserDto;
import com.challengeteam.shop.dto.user.UserLoginRequestDto;
import com.challengeteam.shop.dto.user.UserRegisterRequestDto;
import com.challengeteam.shop.entity.user.User;
import com.challengeteam.shop.exception.InvalidTokenException;
import com.challengeteam.shop.exception.ResourceNotFoundException;
import com.challengeteam.shop.service.JwtAuthorizationService;
import com.challengeteam.shop.service.JwtService;
import com.challengeteam.shop.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtAuthorizationServiceImpl implements JwtAuthorizationService {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtService jwtService;


    @Override
    public JwtResponseDto register(UserRegisterRequestDto registerRequest) {
        Objects.requireNonNull(registerRequest, "registerRequest");

        if(!registerRequest.password().equals(registerRequest.passwordConfirmation())) {
            throw new IllegalStateException("Password and confirmation do not match");
        }

        var createUserDto = new CreateUserDto(
                registerRequest.email(),
                registerRequest.password()
        );
        Long id = userService.createDefaultUser(createUserDto);
        User user = userService
                .getById(id)
                .orElseThrow(() -> new RuntimeException("Failed to get new user"));
        log.debug("Created user with email: {}", registerRequest.email());
        return createJwtResponse(user);
    }

    @Override
    public JwtResponseDto login(UserLoginRequestDto loginRequest) {
        Objects.requireNonNull(loginRequest, "loginRequest");

        String email = loginRequest.email();
        var authenticationToken = new UsernamePasswordAuthenticationToken(email, loginRequest.password());
        authenticationManager.authenticate(authenticationToken);

        User user = userService
                .getByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Not found user with email: " + email));
        log.debug("Login user with email {}", email);
        return createJwtResponse(user);
    }

    @Override
    public JwtResponseDto refresh(String refreshToken) {
        Objects.requireNonNull(refreshToken, "refreshToken");

        if (jwtService.isValid(refreshToken)) {
            String email = jwtService.getEmailFromToken(refreshToken);
            User user = userService
                    .getByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("Not found user with email: "));

            log.debug("Called refreshing token for user with id: {}", user.getId());
            return jwtService.refreshTokens(refreshToken, user);
        } else {
            throw new InvalidTokenException("Refresh token is invalid");
        }
    }

    private JwtResponseDto createJwtResponse(User user) {
        String accessToken = jwtService.createAccessToken(user);
        String refreshToken = jwtService.createRefreshToken(user);

        return new JwtResponseDto(
                user.getId(),
                user.getEmail(),
                accessToken,
                refreshToken
        );
    }

}
