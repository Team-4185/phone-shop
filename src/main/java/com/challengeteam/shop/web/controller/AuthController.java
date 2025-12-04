package com.challengeteam.shop.web.controller;

import com.challengeteam.shop.dto.jwt.JwtRefreshRequestDto;
import com.challengeteam.shop.dto.jwt.JwtResponseDto;
import com.challengeteam.shop.dto.user.UserLoginRequestDto;
import com.challengeteam.shop.dto.user.UserRegisterRequestDto;
import com.challengeteam.shop.service.JwtAuthorizationService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
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

    @Operation(
            summary = "Endpoint for user sign up",
            description = "Gets user's credentials and registers user with unique email. Returns JWT pair."
    )
    @PostMapping("/register")
    public ResponseEntity<JwtResponseDto> register(@RequestBody @Valid UserRegisterRequestDto userRegisterRequestDto) {
        JwtResponseDto jwtResponseDto = jwtAuthorizationService.register(userRegisterRequestDto);

        return ResponseEntity.ok(jwtResponseDto);
    }

    @Operation(
            summary = "Endpoint for user sign in",
            description = "If the user registered in the system, gets user credentials for login. Returns JWT pair."
    )
    @PostMapping("/login")
    public ResponseEntity<JwtResponseDto> login(@RequestBody @Valid UserLoginRequestDto userLoginRequestDto) {
        JwtResponseDto jwtResponseDto = jwtAuthorizationService.login(userLoginRequestDto);

        return ResponseEntity.ok(jwtResponseDto);
    }

    @Operation(
            summary = "Endpoint for token refresh",
            description = "Takes 'refreshToken' and if token valid, creates a new token pair. Returns JWT pair."
    )
    @PostMapping("/refresh-token")
    public ResponseEntity<JwtResponseDto> refreshToken(@RequestBody @Valid JwtRefreshRequestDto jwtRefreshRequestDto) {
        String refreshToken = jwtRefreshRequestDto.refreshToken();
        JwtResponseDto jwtResponseDto = jwtAuthorizationService.refresh(refreshToken);

        return ResponseEntity.ok(jwtResponseDto);
    }

}
