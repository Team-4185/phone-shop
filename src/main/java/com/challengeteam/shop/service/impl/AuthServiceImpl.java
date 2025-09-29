package com.challengeteam.shop.service.impl;

import com.challengeteam.shop.dto.jwt.JwtLoginRequest;
import com.challengeteam.shop.dto.jwt.JwtResponse;
import com.challengeteam.shop.dto.user.UserRegisterRequest;
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
    public JwtResponse register(UserRegisterRequest userRegisterRequest) {
        if(userService.existsByUsername(userRegisterRequest.getUsername())) {
            throw new IllegalStateException("Username already exists");
        }

        if(!userRegisterRequest.getPassword().equals(userRegisterRequest.getPasswordConfirmation())) {
            throw new IllegalStateException("Password and confirmation do not match");
        }
        User user = userService.create(userMapper.toUser(userRegisterRequest));
        return createJwtResponse(user);
    }

    @Override
    public JwtResponse login(JwtLoginRequest jwtLoginRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(jwtLoginRequest.getUsername(), jwtLoginRequest.getPassword())
        );
        User user = userService.getByUsername(jwtLoginRequest.getUsername());
        return createJwtResponse(user);
    }

    @Override
    public JwtResponse refresh(String refreshToken) {
        return jwtTokenService.refreshTokens(refreshToken);
    }

    private JwtResponse createJwtResponse(User user) {
        JwtResponse jwtResponse = new JwtResponse();
        jwtResponse.setUserId(user.getId());
        jwtResponse.setUsername(user.getUsername());
        jwtResponse.setAccessToken(
                jwtTokenService.createAccessToken(user.getId(), user.getUsername(), user.getRole())
        );
        jwtResponse.setRefreshToken(
                jwtTokenService.createRefreshToken(user.getId(), user.getUsername(), user.getRole())
        );
        return jwtResponse;
    }

}
