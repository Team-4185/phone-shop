package com.challengeteam.shop.service.security.auth.jwt;

import com.challengeteam.shop.dto.security.jwt.JwtResponseDto;
import com.challengeteam.shop.entity.user.User;
import com.challengeteam.shop.exceptionHandling.exception.security.InvalidTokenException;
import com.challengeteam.shop.properties.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

    private static final String TOKEN_TYPE_CLAIM = "tokenType";
    private static final String TOKEN_VERSION_CLAIM = "tokenVersion";
    private static final String ACCESS_TOKEN_TYPE = "ACCESS";
    private static final String REFRESH_TOKEN_TYPE = "REFRESH";
    private static final String RESET_TOKEN_TYPE = "RESET";

    private final JwtProperties jwtProperties;

    private PrivateKey privateKey;

    private PublicKey publicKey;

    private SecretKey resetSecretKey;

    @PostConstruct
    public void init() {
        this.privateKey = loadPrivateKey(jwtProperties.getPrivateKey());
        this.publicKey = loadPublicKey(jwtProperties.getPublicKey());
        this.resetSecretKey = Keys.hmacShaKeyFor(jwtProperties.getResetSecret().getBytes());
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
                .add(TOKEN_VERSION_CLAIM, normalizeTokenVersion(user.getTokenVersion()))
                .add(TOKEN_TYPE_CLAIM, ACCESS_TOKEN_TYPE)
                .build();

        Instant expiration = Instant.now().plus(jwtProperties.getAccessTokenExpiration());
        return createToken(claims, expiration);
    }

    @Override
    public String createRefreshToken(User user, boolean rememberMe) {
        Claims claims = Jwts.claims()
                .subject(user.getEmail())
                .add("userId", user.getId())
                .add("role", user.getRole().getName())
                .add("rememberMe", rememberMe)
                .add(TOKEN_VERSION_CLAIM, normalizeTokenVersion(user.getTokenVersion()))
                .add(TOKEN_TYPE_CLAIM, REFRESH_TOKEN_TYPE)
                .build();

        Duration expiration = rememberMe
                ? jwtProperties.getRememberMeRefreshTokenExpiration()
                : jwtProperties.getRefreshTokenExpiration();

        Instant expirationInstant = Instant.now().plus(expiration);
        return createToken(claims, expirationInstant);
    }

    @Override
    public JwtResponseDto refreshTokens(String refreshToken, User user) {
        boolean rememberMe = getClaims(refreshToken).get("rememberMe", Boolean.class);

        String newAccessToken = createAccessToken(user);
        String newRefreshToken = createRefreshToken(user, rememberMe);

        return new JwtResponseDto(
                user.getId(),
                user.getEmail(),
                newAccessToken,
                newRefreshToken,
                rememberMe
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

    @Override
    public Long getTokenVersion(String token) {
        Long tokenVersion = getClaims(token).get(TOKEN_VERSION_CLAIM, Long.class);
        return normalizeTokenVersion(tokenVersion);
    }

    @Override
    public String createResetToken(User user) {
        Claims claims = Jwts.claims()
                .subject(user.getEmail())
                .add(TOKEN_TYPE_CLAIM, RESET_TOKEN_TYPE)
                .build();
        Instant expiration = Instant.now().plus(jwtProperties.getResetTokenExpiration());
        return Jwts.builder()
                .claims(claims)
                .expiration(Date.from(expiration))
                .signWith(resetSecretKey)
                .compact();
    }

    @Override
    public String getEmailFromResetToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(resetSecretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.getSubject();
        } catch (ExpiredJwtException e) {
            throw new InvalidTokenException("Token is expired");
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidTokenException("Token is invalid");
        }
    }

    @Override
    public Instant getExpiration(String token) {
        return getClaims(token).getExpiration().toInstant();
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

    private String createToken(Claims claims, Instant expirationInstant) {
        return Jwts.builder()
                .claims(claims)
                .expiration(Date.from(expirationInstant))
                .signWith(privateKey)
                .compact();
    }

    private Long normalizeTokenVersion(Long tokenVersion) {
        return tokenVersion == null ? 0L : tokenVersion;
    }

}
