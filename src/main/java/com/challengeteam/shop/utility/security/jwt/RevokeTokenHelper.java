package com.challengeteam.shop.utility.security.jwt;

import com.challengeteam.shop.exceptionHandling.exception.security.InvalidTokenException;
import com.challengeteam.shop.service.security.auth.jwt.JwtService;
import com.challengeteam.shop.service.security.auth.logout.blackListTokenCache.TokenRevocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class RevokeTokenHelper {
    private final JwtService jwtService;
    private final TokenRevocationService tokenRevocationService;

    public void revokeToken(String token) {
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