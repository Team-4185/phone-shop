package com.challengeteam.shop.service.impl;

import com.challengeteam.shop.dto.jwt.JwtResponseDto;
import com.challengeteam.shop.entity.user.Role;
import com.challengeteam.shop.entity.user.User;
import com.challengeteam.shop.exception.AccessDeniedException;
import com.challengeteam.shop.properties.JwtProperties;
import com.challengeteam.shop.service.JwtService;
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
public class JwtServiceImpl implements JwtService {

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
    public String createAccessToken(User user) {
        Claims claims = Jwts.claims()
                .subject(user.getEmail())
                .add("userId", user.getId())
                .add("role", user.getRole().getName())
                .build();

        Instant expiration = Instant.now().plus(jwtProperties.getAccessTokenExpiration(), ChronoUnit.MINUTES);
        return createToken(claims, expiration);
    }

    @Override
    public String createRefreshToken(User user) {
        Claims claims = Jwts.claims()
                .subject(user.getEmail())
                .add("userId", user.getId())
                .add("role", user.getRole().getName())
                .build();

        Instant expiration = Instant.now().plus(jwtProperties.getRefreshTokenExpiration(), ChronoUnit.HOURS);
        return createToken(claims, expiration);
    }

    @Override
    public JwtResponseDto refreshTokens(String refreshToken, User user) {
        String newAccessToken = createAccessToken(user);
        String newRefreshToken = createRefreshToken(user);

        return new JwtResponseDto(
                user.getId(),
                user.getEmail(),
                newRefreshToken,
                newAccessToken
        );
    }

    @Override
    public boolean isValid(String token) {
        return getClaims(token)
                .getExpiration()
                .after(new Date());
    }

    @Override
    public String getEmailFromToken(String token) {
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
