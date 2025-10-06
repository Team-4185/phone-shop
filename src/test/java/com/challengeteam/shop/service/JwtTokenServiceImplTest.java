package com.challengeteam.shop.service;

import com.challengeteam.shop.dto.jwt.JwtResponse;
import com.challengeteam.shop.entity.user.Role;
import com.challengeteam.shop.properties.JwtProperties;
import com.challengeteam.shop.service.impl.JwtTokenServiceImpl;
import io.jsonwebtoken.MalformedJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtTokenServiceImplTest {

    private JwtTokenServiceImpl jwtTokenService;

    private final Long userId = 1L;
    private final String username = "username";

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

        jwtTokenService = new JwtTokenServiceImpl(jwtProperties);
        jwtTokenService.init();
    }

    @Test
    void testCreateAccessToken() {
        Role role = new Role();
        role.setName("USER");
        String token = jwtTokenService.createAccessToken(userId, username, role);
        assertNotNull(token);
        assertTrue(jwtTokenService.isValid(token));
        assertEquals(username, jwtTokenService.getUsernameFromToken(token));
    }

    @Test
    void testCreateRefreshToken() {
        Role role = new Role();
        role.setName("USER");
        String token = jwtTokenService.createRefreshToken(userId, username, role);
        assertNotNull(token);
        assertTrue(jwtTokenService.isValid(token));
        assertEquals(username, jwtTokenService.getUsernameFromToken(token));
    }

    @Test
    void testRefreshTokens() {
        Role role = new Role();
        role.setName("USER");
        String refreshToken = jwtTokenService.createRefreshToken(userId, username, role);

        JwtResponse response = jwtTokenService.refreshTokens(refreshToken);

        assertNotNull(response);
        assertEquals(userId, response.userId());
        assertEquals(username, response.email());
        assertNotNull(response.accessToken());
        assertNotNull(response.refreshToken());
    }

    @Test
    void testRefreshTokens_InvalidToken() {
        String refreshToken = "invalid.token.value";

        assertThrows(MalformedJwtException.class, () -> jwtTokenService.refreshTokens(refreshToken));
    }

    @Test
    void testGetUsernameFromToken() {
        Role role = new Role();
        role.setName("USER");
        String token = jwtTokenService.createAccessToken(userId, username, role);
        String extracted = jwtTokenService.getUsernameFromToken(token);
        assertEquals(username, extracted);
    }
}
