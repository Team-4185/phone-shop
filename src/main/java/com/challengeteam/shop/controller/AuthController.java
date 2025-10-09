package com.challengeteam.shop.controller;

import com.challengeteam.shop.dto.jwt.JwtRefreshRequest;
import com.challengeteam.shop.dto.user.UserLoginRequestDto;
import com.challengeteam.shop.dto.jwt.JwtResponseDto;
import com.challengeteam.shop.dto.user.UserRegisterRequestDto;
import com.challengeteam.shop.service.JwtAuthorizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtAuthorizationService jwtAuthorizationService;

    @PostMapping("/register")
    public ResponseEntity<JwtResponseDto> register(@RequestBody UserRegisterRequestDto userRegisterRequestDto) {
        JwtResponseDto jwtResponseDto = jwtAuthorizationService.register(userRegisterRequestDto);
        return ResponseEntity.ok(jwtResponseDto);
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponseDto> login(@RequestBody UserLoginRequestDto userLoginRequestDto) {
        JwtResponseDto jwtResponseDto = jwtAuthorizationService.login(userLoginRequestDto);
        return ResponseEntity.ok(jwtResponseDto);
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtResponseDto> refresh(@RequestBody JwtRefreshRequest jwtRefreshRequest) {
        JwtResponseDto jwtResponseDto = jwtAuthorizationService.refresh(jwtRefreshRequest.getRefreshToken());
        return ResponseEntity.ok(jwtResponseDto);
    }

}
