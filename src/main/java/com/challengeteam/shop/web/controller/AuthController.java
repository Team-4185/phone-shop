package com.challengeteam.shop.web.controller;

import com.challengeteam.shop.dto.jwt.JwtPublicResponseDto;
import com.challengeteam.shop.dto.jwt.JwtResponseDto;
import com.challengeteam.shop.dto.user.UserLoginRequestDto;
import com.challengeteam.shop.dto.user.UserRegisterRequestDto;
import com.challengeteam.shop.properties.JwtProperties;
import com.challengeteam.shop.service.JwtAuthorizationService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtAuthorizationService jwtAuthorizationService;

    private final JwtProperties jwtProperties;

    @Operation(
            summary = "Endpoint for user sign up",
            description = "Gets user's credentials and registers user with unique email. Returns JWT pair."
    )
    @PostMapping("/register")
    public ResponseEntity<JwtPublicResponseDto> register(@RequestBody @Valid UserRegisterRequestDto userRegisterRequestDto,
                                                         HttpServletResponse httpServletResponse) {
        JwtResponseDto jwtResponseDto = jwtAuthorizationService.register(userRegisterRequestDto);

        addRefreshTokenCookie(httpServletResponse, jwtResponseDto.refreshToken(), jwtResponseDto.rememberMe());

        return ResponseEntity.ok(
                new JwtPublicResponseDto(
                        jwtResponseDto.userId(),
                        jwtResponseDto.email(),
                        jwtResponseDto.accessToken()
                )
        );
    }

    @Operation(
            summary = "Endpoint for user sign in",
            description = "If the user registered in the system, gets user credentials for login. Returns JWT pair."
    )
    @PostMapping("/login")
    public ResponseEntity<JwtPublicResponseDto> login(@RequestBody @Valid UserLoginRequestDto userLoginRequestDto,
                                                      HttpServletResponse httpServletResponse) {
        JwtResponseDto jwtResponseDto = jwtAuthorizationService.login(userLoginRequestDto);

        addRefreshTokenCookie(httpServletResponse, jwtResponseDto.refreshToken(), jwtResponseDto.rememberMe());

        return ResponseEntity.ok(
                new JwtPublicResponseDto(
                        jwtResponseDto.userId(),
                        jwtResponseDto.email(),
                        jwtResponseDto.accessToken()
                )
        );
    }

    @Operation(
            summary = "Endpoint for token refresh",
            description = "Takes 'refreshToken' and if token valid, creates a new token pair. Returns JWT pair."
    )
    @PostMapping("/refresh-token")
    public ResponseEntity<JwtPublicResponseDto> refreshToken(@CookieValue("refreshToken") String refreshToken,
                                                             HttpServletResponse httpServletResponse) {
        JwtResponseDto jwtResponseDto = jwtAuthorizationService.refresh(refreshToken);

        addRefreshTokenCookie(httpServletResponse, jwtResponseDto.refreshToken(), jwtResponseDto.rememberMe());

        return ResponseEntity.ok(
                new JwtPublicResponseDto(
                        jwtResponseDto.userId(),
                        jwtResponseDto.email(),
                        jwtResponseDto.accessToken()
                )
        );
    }

    private void addRefreshTokenCookie(HttpServletResponse httpServletResponse, String refreshToken, boolean rememberMe) {
        int maxAge = rememberMe
                ? (int) jwtProperties.getRememberMeRefreshTokenExpiration().toSeconds()
                : (int) jwtProperties.getRefreshTokenExpiration().toSeconds();

        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/api/auth");
        cookie.setMaxAge(maxAge);
        httpServletResponse.addCookie(cookie);
    }

}
