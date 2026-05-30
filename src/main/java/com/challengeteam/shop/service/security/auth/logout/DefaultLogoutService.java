package com.challengeteam.shop.service.security.auth.logout;

import com.challengeteam.shop.exceptionHandling.exception.security.InvalidTokenException;
import com.challengeteam.shop.service.security.auth.jwt.JwtService;
import com.challengeteam.shop.service.security.auth.logout.blackListTokenCache.TokenRevocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultLogoutService implements LogoutService {

    private final TokenRevocationService tokenRevocationService;
    private final JwtService jwtService;

    @Override
    public void logout(String accessToken, String refreshToken) {
        revokeTokenHelper(accessToken);
        revokeTokenHelper(refreshToken);
    }

    private void revokeTokenHelper(String token) {
        if (!jwtService.isValid(token)) {
            log.warn("Attempted to revoke an invalid token");
            throw new InvalidTokenException("Invalid token");
        }
        Instant expiration = jwtService.getExpiration(token);
        Duration ttl = Duration.between(Instant.now(), expiration);
        tokenRevocationService.revoke(token, ttl);
        log.debug("Token revoked, expires in: {}", ttl);
    }
}