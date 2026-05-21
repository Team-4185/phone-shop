package com.challengeteam.shop.service.security.auth.logout;

public interface LogoutService {
    void logout(String accessToken, String refreshToken);
}