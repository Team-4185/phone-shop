package com.challengeteam.shop.web.controller.auth;

import com.challengeteam.shop.dto.auth.ForgotPasswordRequestDto;
import com.challengeteam.shop.dto.auth.ResetPasswordRequestDto;
import com.challengeteam.shop.dto.auth.UserLoginRequestDto;
import com.challengeteam.shop.dto.auth.UserRegisterRequestDto;
import com.challengeteam.shop.dto.security.jwt.JwtPublicResponseDto;
import com.challengeteam.shop.dto.security.jwt.JwtResponseDto;
import com.challengeteam.shop.properties.JwtProperties;
import com.challengeteam.shop.service.PasswordResetService;
import com.challengeteam.shop.service.security.auth.authorization.JwtAuthorizationService;
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

    private final PasswordResetService passwordResetService;

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

    @Operation(
            summary = "Endpoint for password reset request",
            description = "Sends a password reset link to the email. Always returns 204 regardless of whether the email exists in the system."
    )
    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDto forgotPasswordRequestDto) {
        passwordResetService.sendResetLink(forgotPasswordRequestDto.email());

        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Endpoint for password reset",
            description = "Takes a reset token and a new password. If the token is valid and not expired, updates the user's password."
    )
    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequestDto resetPasswordRequestDto) {
        passwordResetService.resetPassword(resetPasswordRequestDto.token(), resetPasswordRequestDto.newPassword());

        return ResponseEntity.noContent().build();
    }

    private void addRefreshTokenCookie(HttpServletResponse httpServletResponse, String refreshToken, boolean rememberMe) {
        int maxAge = rememberMe
                ? (int) jwtProperties.getRememberMeRefreshTokenExpiration().toSeconds()
                : (int) jwtProperties.getRefreshTokenExpiration().toSeconds();

        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/api");
        cookie.setMaxAge(maxAge);
        httpServletResponse.addCookie(cookie);
    }

}