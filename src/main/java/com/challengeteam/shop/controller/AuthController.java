package com.challengeteam.shop.controller;

import com.challengeteam.shop.dto.jwt.JwtRefreshRequest;
import com.challengeteam.shop.dto.jwt.JwtRequest;
import com.challengeteam.shop.dto.jwt.JwtResponse;
import com.challengeteam.shop.dto.user.UserRegisterRequest;
import com.challengeteam.shop.service.AuthService;
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

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<JwtResponse> register(@RequestBody UserRegisterRequest userRegisterRequest) {
        JwtResponse jwtResponse = authService.register(userRegisterRequest);
        return ResponseEntity.ok(jwtResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody JwtRequest jwtRequest) {
        JwtResponse jwtResponse = authService.login(jwtRequest);
        return ResponseEntity.ok(jwtResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtResponse> refresh(@RequestBody JwtRefreshRequest jwtRefreshRequest) {
        JwtResponse jwtResponse = authService.refresh(jwtRefreshRequest.getRefreshToken());
        return ResponseEntity.ok(jwtResponse);
    }

}
