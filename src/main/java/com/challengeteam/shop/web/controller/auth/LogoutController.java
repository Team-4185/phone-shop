package com.challengeteam.shop.web.controller.auth;

import com.challengeteam.shop.service.security.auth.logout.LogoutService;
import com.challengeteam.shop.utility.web.headers.AccessTokenHeaderExtractor;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/logout")
@RequiredArgsConstructor
public class LogoutController {
    private final LogoutService logoutService;

    @PostMapping()
    public ResponseEntity<Void> logout(HttpServletRequest request,
                                       @CookieValue("refreshToken") String refreshToken,
                                       HttpServletResponse response) {
        String accessToken = AccessTokenHeaderExtractor.extractAccessToken(request);
        if (accessToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        logoutService.logout(accessToken, refreshToken);
        clearRefreshTokenCookie(response);
        return ResponseEntity.noContent().build();
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("refreshToken", "");
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/api/auth");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}