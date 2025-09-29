package com.challengeteam.shop.service.impl;

import com.challengeteam.shop.dto.jwt.JwtResponse;
import com.challengeteam.shop.entity.user.Role;
import com.challengeteam.shop.exception.AccessDeniedException;
import com.challengeteam.shop.properties.JwtProperties;
import com.challengeteam.shop.service.JwtTokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtTokenServiceImpl implements JwtTokenService {

    private final JwtProperties jwtProperties;

    private SecretKey key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
    }

    @Override
    public String createAccessToken(Long userId, String username, Role role) {
        Claims claims = Jwts.claims()
                .subject(username)
                .add("userId", userId)
                .add("role", role.name())
                .build();
        Instant expiration = Instant.now().plus(jwtProperties.getAccessTokenExpiration(), ChronoUnit.MINUTES);
        return createToken(claims, expiration);
    }

    @Override
    public String createRefreshToken(Long userId, String username, Role role) {
        Claims claims = Jwts.claims()
                .subject(username)
                .add("userId", userId)
                .add("role", role.name())
                .build();
        Instant expiration = Instant.now().plus(jwtProperties.getRefreshTokenExpiration(), ChronoUnit.HOURS);
        return createToken(claims, expiration);
    }

    @Override
    public JwtResponse refreshTokens(String refreshToken) {
        if (!isValid(refreshToken)) {
            throw new AccessDeniedException("The refresh token is invalid");
        }
        Long userId = getClaims(refreshToken).get("userId", Long.class);
        String username = getClaims(refreshToken).get("username", String.class);
        Role role = getClaims(refreshToken).get("role", Role.class);
        JwtResponse jwtResponse = new JwtResponse();
        jwtResponse.setUserId(userId);
        jwtResponse.setUsername(username);
        jwtResponse.setAccessToken(createAccessToken(userId, username, role));
        jwtResponse.setRefreshToken(createRefreshToken(userId, username, role));
        return jwtResponse;
    }

    @Override
    public boolean isValid(String token) {

        return getClaims(token).getExpiration().after(new Date());
    }

    @Override
    public String getUsernameFromToken(String token) {
        return getClaims(token).getSubject();
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private String createToken(Claims claims, Instant expiration) {
        return Jwts.builder()
                .claims(claims)
                .expiration(Date.from(expiration))
                .signWith(key)
                .compact();
    }

}
