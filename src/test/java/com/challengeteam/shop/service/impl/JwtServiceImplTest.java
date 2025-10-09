package com.challengeteam.shop.service.impl;

import com.challengeteam.shop.dto.jwt.JwtResponseDto;
import com.challengeteam.shop.entity.user.User;
import com.challengeteam.shop.properties.JwtProperties;
import com.challengeteam.shop.testData.UserTestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtServiceImplTest {

    private JwtServiceImpl jwtTokenService;

    private final User jeremy = UserTestData.getJeremy();


    @BeforeEach
    void setUp() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        String privateKeyBase64 = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
        String publicKeyBase64 = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());

        JwtProperties jwtProperties = mock(JwtProperties.class);
        when(jwtProperties.getPrivateKey()).thenReturn(privateKeyBase64);
        when(jwtProperties.getPublicKey()).thenReturn(publicKeyBase64);
        when(jwtProperties.getAccessTokenExpiration()).thenReturn(1L);
        when(jwtProperties.getRefreshTokenExpiration()).thenReturn(24L);

        jwtTokenService = new JwtServiceImpl(jwtProperties);
        jwtTokenService.init();
    }

    @Test
    void testCreateAccessToken() {
        String token = jwtTokenService.createAccessToken(jeremy);

        assertNotNull(token);
        assertTrue(jwtTokenService.isValid(token));
        assertEquals(jeremy.getEmail(), jwtTokenService.getEmailFromToken(token));
    }

    @Test
    void testCreateRefreshToken() {
        String token = jwtTokenService.createRefreshToken(jeremy);

        assertNotNull(token);
        assertTrue(jwtTokenService.isValid(token));
        assertEquals(jeremy.getEmail(), jwtTokenService.getEmailFromToken(token));
    }

    @Test
    void testRefreshTokens() {
        String refreshToken = jwtTokenService.createRefreshToken(jeremy);

        JwtResponseDto response = jwtTokenService.refreshTokens(refreshToken, jeremy);

        assertNotNull(response);
        assertEquals(jeremy.getId(), response.userId());
        assertEquals(jeremy.getEmail(), response.email());
        assertNotNull(response.accessToken());
        assertNotNull(response.refreshToken());
    }

    @Test
    void testGetEmailFromToken() {
        String token = jwtTokenService.createAccessToken(jeremy);

        String extracted = jwtTokenService.getEmailFromToken(token);
        assertEquals(jeremy.getEmail(), extracted);
    }
}
