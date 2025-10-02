package com.challengeteam.shop.service.impl;

import com.challengeteam.shop.dto.jwt.JwtResponse;
import com.challengeteam.shop.entity.user.Role;
import com.challengeteam.shop.exception.AccessDeniedException;
import com.challengeteam.shop.properties.JwtProperties;
import com.challengeteam.shop.service.JwtTokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtTokenServiceImpl implements JwtTokenService {

    private final JwtProperties jwtProperties;

    private PrivateKey privateKey;

    private PublicKey publicKey;

    @PostConstruct
    public void init() {
        this.privateKey = loadPrivateKey(jwtProperties.getPrivateKey());
        this.publicKey = loadPublicKey(jwtProperties.getPublicKey());
    }

    @SneakyThrows
    private PrivateKey loadPrivateKey(String privateKey) {
        byte[] keyBytes = Base64.getDecoder().decode(privateKey);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(spec);
    }

    @SneakyThrows
    private PublicKey loadPublicKey(String publicKey) {
        byte[] keyBytes = Base64.getDecoder().decode(publicKey);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(spec);
    }

    @Override
    public String createAccessToken(Long userId, String username, Role role) {
        Claims claims = Jwts.claims()
                .subject(username)
                .add("userId", userId)
                .add("role", role.getName())
                .build();
        Instant expiration = Instant.now().plus(jwtProperties.getAccessTokenExpiration(), ChronoUnit.MINUTES);
        return createToken(claims, expiration);
    }

    @Override
    public String createRefreshToken(Long userId, String username, Role role) {
        Claims claims = Jwts.claims()
                .subject(username)
                .add("userId", userId)
                .add("role", role.getName())
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
        String username = getClaims(refreshToken).getSubject();
        String roleName = getClaims(refreshToken).get("role", String.class);

        Role role = new Role();
        role.setName(roleName);
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
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private String createToken(Claims claims, Instant expiration) {
        return Jwts.builder()
                .claims(claims)
                .expiration(Date.from(expiration))
                .signWith(privateKey)
                .compact();
    }

}
