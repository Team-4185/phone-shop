package com.challengeteam.shop.service.impl;

import com.challengeteam.shop.dto.jwt.JwtLoginRequest;
import com.challengeteam.shop.dto.jwt.JwtResponse;
import com.challengeteam.shop.dto.user.CreateUserDto;
import com.challengeteam.shop.dto.user.UserRegisterRequestDto;
import com.challengeteam.shop.entity.user.User;
import com.challengeteam.shop.exception.ResourceNotFoundException;
import com.challengeteam.shop.mapper.UserMapper;
import com.challengeteam.shop.service.AuthService;
import com.challengeteam.shop.service.JwtTokenService;
import com.challengeteam.shop.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;

    private final UserMapper userMapper;

    private final JwtTokenService jwtTokenService;

    private final AuthenticationManager authenticationManager;

    @Override
    public JwtResponse register(UserRegisterRequestDto registerRequest) {
        if(userService.existsByEmail(registerRequest.email())) {
            throw new IllegalStateException("Username already exists");
        }

        if(!registerRequest.password().equals(registerRequest.passwordConfirmation())) {
            throw new IllegalStateException("Password and confirmation do not match");
        }

        CreateUserDto createUserDto = new CreateUserDto(registerRequest.email(), registerRequest.password());
        Long id = userService.createDefaultUser(createUserDto);
        User user = userService
                .getById(id)
                .orElseThrow(() -> new RuntimeException("Failed to get new user"));
        return createJwtResponse(user);
    }

    @Override
    public JwtResponse login(JwtLoginRequest loginRequest) {
        String username = loginRequest.getUsername();

        var authenticationToken = new UsernamePasswordAuthenticationToken(username, loginRequest.getPassword());
        authenticationManager.authenticate(authenticationToken);

        User user = userService
                .getByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("Not found user with username: " + username));
        return createJwtResponse(user);
    }

    @Override
    public JwtResponse refresh(String refreshToken) {
        return jwtTokenService.refreshTokens(refreshToken);
    }

    private JwtResponse createJwtResponse(User user) {
        String accessToken = jwtTokenService.createAccessToken(user.getId(), user.getEmail(), user.getRole());
        String refreshToken = jwtTokenService.createRefreshToken(user.getId(), user.getEmail(), user.getRole());

        return new JwtResponse(
                user.getId(),
                user.getEmail(),
                accessToken,
                refreshToken
        );
    }

}
