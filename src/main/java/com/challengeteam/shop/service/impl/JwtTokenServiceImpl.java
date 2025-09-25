package com.challengeteam.shop.service.impl;

import com.challengeteam.shop.dto.jwt.JwtResponse;
import com.challengeteam.shop.entity.user.Role;
import com.challengeteam.shop.entity.user.User;
import com.challengeteam.shop.exception.AccessDeniedException;
import com.challengeteam.shop.properties.JwtProperties;
import com.challengeteam.shop.service.JwtTokenService;
import com.challengeteam.shop.service.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtTokenServiceImpl implements JwtTokenService {

    private final JwtProperties jwtProperties;

    private final UserDetailsService userDetailsService;

    private final UserService userService;

    private SecretKey key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
    }

    public String createAccessToken(Long userId, String username, Role role) {
        Claims claims = Jwts.claims()
                .subject(username)
                .add("userId", userId)
                .add("role", role.name())
                .build();
        Instant expiration = Instant.now().plus(jwtProperties.getAccessTokenExpiration(), ChronoUnit.MINUTES);
        return createToken(claims, expiration);
    }

    public String createRefreshToken(Long userId, String username) {
        Claims claims = Jwts.claims()
                .subject(username)
                .add("userId", userId)
                .build();
        Instant expiration = Instant.now().plus(jwtProperties.getRefreshTokenExpiration(), ChronoUnit.HOURS);
        return createToken(claims, expiration);
    }

    public JwtResponse refreshTokens(String refreshToken) {
        if (!isValid(refreshToken)) {
            throw new AccessDeniedException("The refresh token is invalid");
        }
        Long userId = getClaims(refreshToken).get("userId", Long.class);
        User user = userService.getById(userId);
        JwtResponse jwtResponse = new JwtResponse();
        jwtResponse.setUserId(userId);
        jwtResponse.setUsername(user.getUsername());
        jwtResponse.setAccessToken(createAccessToken(userId, user.getUsername(), user.getRole()));
        jwtResponse.setRefreshToken(createRefreshToken(userId, user.getUsername()));
        return jwtResponse;
    }

    public Authentication getAuthentication(String token) {
        String username = getClaims(token).getSubject();
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    public boolean isValid(String token) {

        return getClaims(token).getExpiration().after(new Date());
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
