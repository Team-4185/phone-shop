package com.challengeteam.shop.service.security.auth.logout;

import com.challengeteam.shop.exceptionHandling.exception.security.InvalidTokenException;
import com.challengeteam.shop.service.security.auth.jwt.JwtServiceImpl;
import com.challengeteam.shop.service.security.auth.logout.blackListTokenCache.RedisTokenRevocationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * cases:
 * if the token is invalid -> throw exception
 * if each token is valid -> save
 */
@ExtendWith(MockitoExtension.class)
class DefaultLogoutServiceTest {
    @InjectMocks
    private DefaultLogoutService logoutService;
    @Mock
    private JwtServiceImpl jwtService;
    @Mock
    private RedisTokenRevocationService redisTokenRevocationService;

    private String accessToken;
    private String refreshToken;

    @BeforeEach
    void setUp() {
        accessToken = UUID.randomUUID().toString();
        refreshToken = UUID.randomUUID().toString();
    }

    @Test
    void shouldThrowException_whenAccessTokenIsInvalid() {
        when(jwtService.isValid(accessToken)).thenReturn(false);

        assertThatThrownBy(() -> logoutService.logout(accessToken, refreshToken))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("Invalid token");

        verifyNoInteractions(redisTokenRevocationService);
    }

    @Test
    void shouldThrowException_whenRefreshTokenIsInvalid() {
        when(jwtService.isValid(accessToken)).thenReturn(true);
        when(jwtService.isValid(refreshToken)).thenReturn(false);
        when(jwtService.getExpiration(accessToken))
                .thenReturn(Instant.now().plus(Duration.ofMinutes(3)));

        assertThatThrownBy(() -> logoutService.logout(accessToken, refreshToken))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("Invalid token");

        verify(redisTokenRevocationService).revoke(eq(accessToken), any(Duration.class));
        verifyNoMoreInteractions(redisTokenRevocationService);
    }

    @Test
    void shouldRevokeAllTokens_whenBothTokensAreValid() {
        Instant expiration = Instant.now().plus(Duration.ofMinutes(3));

        when(jwtService.isValid(accessToken)).thenReturn(true);
        when(jwtService.isValid(refreshToken)).thenReturn(true);
        when(jwtService.getExpiration(any())).thenReturn(expiration);

        logoutService.logout(accessToken, refreshToken);

        verify(redisTokenRevocationService).revoke(eq(accessToken), any(Duration.class));
        verify(redisTokenRevocationService).revoke(eq(refreshToken), any(Duration.class));
    }
}