package com.challengeteam.shop.service.impl;

import com.challengeteam.shop.dto.jwt.JwtRequest;
import com.challengeteam.shop.dto.jwt.JwtResponse;
import com.challengeteam.shop.dto.user.UserRegisterRequest;
import com.challengeteam.shop.entity.user.User;
import com.challengeteam.shop.security.JwtTokenProvider;
import com.challengeteam.shop.service.AuthService;
import com.challengeteam.shop.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;

    private final JwtTokenProvider jwtTokenProvider;

    private final AuthenticationManager authenticationManager;

    @Override
    public JwtResponse register(UserRegisterRequest userRegisterRequest) {
        if(userService.existsByUsername(userRegisterRequest.getUsername())) {
            throw new IllegalStateException("Username already exists");
        }

        if(!userRegisterRequest.getPassword().equals(userRegisterRequest.getPasswordConfirmation())) {
            throw new IllegalStateException("Password and confirmation do not match");
        }
        User user = userService.create(userRegisterRequest);
        return createJwtResponse(user);
    }

    @Override
    public JwtResponse login(JwtRequest jwtRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(jwtRequest.getUsername(), jwtRequest.getPassword())
        );
        User user = userService.getByUsername(jwtRequest.getUsername());
        return createJwtResponse(user);
    }

    @Override
    public JwtResponse refresh(String refreshToken) {
        return jwtTokenProvider.refreshTokens(refreshToken);
    }

    private JwtResponse createJwtResponse(User user) {
        JwtResponse jwtResponse = new JwtResponse();
        jwtResponse.setUserId(user.getId());
        jwtResponse.setUsername(user.getUsername());
        jwtResponse.setAccessToken(
                jwtTokenProvider.createAccessToken(user.getId(), user.getUsername(), user.getRole())
        );
        jwtResponse.setRefreshToken(
                jwtTokenProvider.createRefreshToken(user.getId(), user.getUsername())
        );
        return jwtResponse;
    }

}
