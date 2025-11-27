package com.challengeteam.shop.service.impl;

import com.challengeteam.shop.dto.jwt.JwtResponseDto;
import com.challengeteam.shop.entity.user.User;
import com.challengeteam.shop.exceptionHandling.exception.InvalidTokenException;
import com.challengeteam.shop.properties.JwtProperties;
import com.challengeteam.shop.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

    private static final String TOKEN_TYPE_CLAIM = "tokenType";
    private static final String ACCESS_TOKEN_TYPE = "ACCESS";
    private static final String REFRESH_TOKEN_TYPE = "REFRESH";

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
                .add(TOKEN_TYPE_CLAIM, ACCESS_TOKEN_TYPE)
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
                .add(TOKEN_TYPE_CLAIM, REFRESH_TOKEN_TYPE)
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
        try {
            return getClaims(token)
                    .getExpiration()
                    .after(new Date());
        } catch (InvalidTokenException e) {
            return false;
        }
    }

    @Override
    public boolean isAccessToken(String token) {
        Claims claims = getClaims(token);
        String tokenType = claims.get(TOKEN_TYPE_CLAIM, String.class);
        return ACCESS_TOKEN_TYPE.equals(tokenType);
    }

    @Override
    public boolean isRefreshToken(String token) {
        Claims claims = getClaims(token);
        String tokenType = claims.get(TOKEN_TYPE_CLAIM, String.class);
        return REFRESH_TOKEN_TYPE.equals(tokenType);
    }

    @Override
    public String getEmailFromToken(String token) {
        return getClaims(token).getSubject();
    }

    private Claims getClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new InvalidTokenException("Token is expired");
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidTokenException("Token is invalid");
        }
    }

    private String createToken(Claims claims, Instant expiration) {
        return Jwts.builder()
                .claims(claims)
                .expiration(Date.from(expiration))
                .signWith(privateKey)
                .compact();
    }

}
