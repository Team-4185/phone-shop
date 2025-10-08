package com.challengeteam.shop.service.impl;

import com.challengeteam.shop.dto.jwt.JwtLoginRequest;
import com.challengeteam.shop.dto.jwt.JwtResponse;
import com.challengeteam.shop.dto.user.UserRegisterRequestDto;
import com.challengeteam.shop.entity.user.User;
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
    public JwtResponse register(UserRegisterRequestDto userRegisterRequestDto) {
        if(userService.existsByEmail(userRegisterRequestDto.email())) {
            throw new IllegalStateException("Username already exists");
        }
        if(!userRegisterRequestDto.password().equals(userRegisterRequestDto.passwordConfirmation())) {
            throw new IllegalStateException("Password and confirmation do not match");
        }

        User user = userService.createDefaultUser(userMapper.toUser(userRegisterRequestDto));
        return createJwtResponse(user);
    }

    @Override
    public JwtResponse login(JwtLoginRequest jwtLoginRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(jwtLoginRequest.getUsername(), jwtLoginRequest.getPassword())
        );
        User user = userService.getByEmail(jwtLoginRequest.getUsername());
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
