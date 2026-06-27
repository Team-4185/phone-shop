package com.challengeteam.shop.service.security.auth.logout;

import com.challengeteam.shop.exceptionHandling.exception.security.InvalidTokenException;
import com.challengeteam.shop.utility.security.jwt.RevokeTokenHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
    private RevokeTokenHelper revokeTokenHelper;

    private String accessToken;
    private String refreshToken;

    @BeforeEach
    void setUp() {
        accessToken = UUID.randomUUID().toString();
        refreshToken = UUID.randomUUID().toString();
    }

    @Test
    void shouldThrowException_whenAccessTokenIsInvalid() {
        doThrow(InvalidTokenException.class).when(revokeTokenHelper).revokeToken(accessToken);

        assertThatThrownBy(() -> logoutService.logout(accessToken, refreshToken))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("Invalid token");
    }

    @Test
    void shouldThrowException_whenRefreshTokenIsInvalid() {
        doNothing().when(revokeTokenHelper).revokeToken(accessToken);
        doThrow(InvalidTokenException.class).when(revokeTokenHelper).revokeToken(refreshToken);

        assertThatThrownBy(() -> logoutService.logout(accessToken, refreshToken))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("Invalid token");

    }

    @Test
    void shouldRevokeAllTokens_whenBothTokensAreValid() {

        doNothing().when(revokeTokenHelper).revokeToken(accessToken);
        doNothing().when(revokeTokenHelper).revokeToken(refreshToken);

        logoutService.logout(accessToken, refreshToken);

        verify(revokeTokenHelper).revokeToken(eq(accessToken));
        verify(revokeTokenHelper).revokeToken(eq(refreshToken));
    }
}