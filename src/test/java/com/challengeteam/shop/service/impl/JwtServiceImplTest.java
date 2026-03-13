package com.challengeteam.shop.service.impl;

import com.challengeteam.shop.dto.jwt.JwtResponseDto;
import com.challengeteam.shop.entity.user.User;
import com.challengeteam.shop.exceptionHandling.exception.InvalidTokenException;
import com.challengeteam.shop.properties.JwtProperties;
import com.challengeteam.shop.testData.user.UserTestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.time.Duration;
import java.util.Base64;

import static com.challengeteam.shop.service.impl.JwtServiceImplTest.TestResources.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtServiceImplTest {
    private JwtProperties jwtProperties;
    private JwtServiceImpl jwtService;

    @BeforeEach
    void setUp() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        String privateKeyBase64 = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
        String publicKeyBase64 = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());

        jwtProperties = mock(JwtProperties.class);
        when(jwtProperties.getPrivateKey()).thenReturn(privateKeyBase64);
        when(jwtProperties.getPublicKey()).thenReturn(publicKeyBase64);
        when(jwtProperties.getAccessTokenExpiration()).thenReturn(Duration.ofMinutes(60));
        when(jwtProperties.getRefreshTokenExpiration()).thenReturn(Duration.ofDays(10));
        when(jwtProperties.getRememberMeRefreshTokenExpiration()).thenReturn(Duration.ofDays(30));

        jwtService = new JwtServiceImpl(jwtProperties);
        jwtService.init();
    }

    @Nested
    class CreateAccessTokenTest {

        @Test
        void whenUserIsValid_thenReturnAccessToken() {
            // when
            String token = jwtService.createAccessToken(buildUser());

            // then
            assertThat(token).isNotNull();
            assertThat(jwtService.isValid(token)).isTrue();
            assertThat(jwtService.getEmailFromToken(token)).isEqualTo(USER_EMAIL);
            assertThat(jwtService.isAccessToken(token)).isTrue();
            verify(jwtProperties).getAccessTokenExpiration();
        }

        @Test
        void whenUserIsNull_thenThrowException() {
            // when + then
            assertThatThrownBy(() -> jwtService.createAccessToken(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    class CreateRefreshTokenTest {

        @Test
        void whenRememberMeIsFalse_thenReturnRefreshTokenWithShorterMaxAge() {
            // when
            String token = jwtService.createRefreshToken(buildUser(), false);

            // then
            assertThat(token).isNotNull();
            assertThat(jwtService.isValid(token)).isTrue();
            assertThat(jwtService.getEmailFromToken(token)).isEqualTo(USER_EMAIL);
            assertThat(jwtService.isRefreshToken(token)).isTrue();
            verify(jwtProperties).getRefreshTokenExpiration();
        }

        @Test
        void whenRememberMeIsTrue_thenReturnRefreshTokenWithLongerMaxAge() {
            // when
            String token = jwtService.createRefreshToken(buildUser(), true);

            // then
            assertThat(token).isNotNull();
            assertThat(jwtService.isValid(token)).isTrue();
            assertThat(jwtService.getEmailFromToken(token)).isEqualTo(USER_EMAIL);
            assertThat(jwtService.isRefreshToken(token)).isTrue();
            verify(jwtProperties).getRememberMeRefreshTokenExpiration();
        }

        @Test
        void whenUserIsNull_thenThrowException() {
            // when + then
            assertThatThrownBy(() -> jwtService.createRefreshToken(null, false))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    class RefreshTokensTest {

        @Test
        void whenRefreshTokenWithRememberMeIsFalse_thenReturnValidResponse() {
            // given
            String refreshToken = jwtService.createRefreshToken(buildUser(), false);

            // when
            JwtResponseDto response = jwtService.refreshTokens(refreshToken, buildUser());

            // then
            assertThat(response).isNotNull();
            assertThat(response.userId()).isEqualTo(USER_ID);
            assertThat(response.email()).isEqualTo(USER_EMAIL);
            assertThat(response.accessToken()).isNotNull();
            assertThat(jwtService.isValid(response.accessToken())).isTrue();
            assertThat(response.refreshToken()).isNotNull();
            assertThat(jwtService.isValid(response.refreshToken())).isTrue();
            assertThat(response.rememberMe()).isFalse();
        }

        @Test
        void whenRefreshTokenWithRememberMeIsTrue_thenReturnValidResponse() {
            // given
            String refreshToken = jwtService.createRefreshToken(buildUser(), false);

            // when
            JwtResponseDto response = jwtService.refreshTokens(refreshToken, buildUser());

            // then
            assertThat(response).isNotNull();
            assertThat(response.userId()).isEqualTo(USER_ID);
            assertThat(response.email()).isEqualTo(USER_EMAIL);
            assertThat(response.accessToken()).isNotNull();
            assertThat(jwtService.isValid(response.accessToken())).isTrue();
            assertThat(response.refreshToken()).isNotNull();
            assertThat(jwtService.isValid(response.refreshToken())).isTrue();
            assertThat(response.rememberMe()).isFalse();
        }


        @Test
        void whenTokenIsInvalid_thenThrowException() {
            // when + then
            assertThatThrownBy(() -> jwtService.refreshTokens(INVALID_TOKEN, buildUser()))
                    .isInstanceOf(InvalidTokenException.class);
        }
    }

    @Nested
    class IsValidTest {

        @Test
        void whenTokenIsValid_thenReturnTrue() {
            // given
            String accessToken = jwtService.createAccessToken(buildUser());

            // then
            assertThat(jwtService.isValid(accessToken)).isTrue();
        }

        @Test
        void whenTokenIsGarbage_thenReturnFalse() {
            // when + then
            assertThat(jwtService.isValid(INVALID_TOKEN)).isFalse();
        }

        @Test
        void whenTokenIsEmpty_thenReturnFalse() {
            // when + then
            assertThat(jwtService.isValid("")).isFalse();
        }
    }

    @Nested
    class GetEmailFromTokenTest {

        @Test
        void whenAccessToken_thenReturnCorrectEmail() {
            // given
            String token = jwtService.createAccessToken(buildUser());

            // when
            String email = jwtService.getEmailFromToken(token);

            // then
            assertThat(email).isEqualTo(USER_EMAIL);
        }

        @Test
        void whenRefreshToken_thenReturnCorrectEmail() {
            // given
            String token = jwtService.createRefreshToken(buildUser(), false);

            // when
            String email = jwtService.getEmailFromToken(token);

            // then
            assertThat(email).isEqualTo(USER_EMAIL);
        }

        @Test
        void whenTokenIsInvalid_thenThrowException() {
            // when + then
            assertThatThrownBy(() -> jwtService.getEmailFromToken(INVALID_TOKEN))
                    .isInstanceOf(InvalidTokenException.class);
        }
    }


    static class TestResources {

        static final Long USER_ID = UserTestData.getJeremy().getId();
        static final String USER_EMAIL = UserTestData.getJeremy().getEmail();
        static final String INVALID_TOKEN = "this.is.invalid";

        static User buildUser() {
            return UserTestData.getJeremy();
        }
    }
}