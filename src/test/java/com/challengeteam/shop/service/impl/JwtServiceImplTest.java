package com.challengeteam.shop.service.impl;

import com.challengeteam.shop.dto.security.jwt.JwtResponseDto;
import com.challengeteam.shop.entity.user.User;
import com.challengeteam.shop.exceptionHandling.exception.security.InvalidTokenException;
import com.challengeteam.shop.properties.JwtProperties;
import com.challengeteam.shop.service.security.auth.jwt.JwtServiceImpl;
import com.challengeteam.shop.testData.user.UserTestData;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

import static com.challengeteam.shop.service.impl.JwtServiceImplTest.TestResources.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

        SecretKey resetSecretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        String resetSecret = Base64.getEncoder().encodeToString(resetSecretKey.getEncoded());

        jwtProperties = mock(JwtProperties.class);
        when(jwtProperties.getPrivateKey()).thenReturn(privateKeyBase64);
        when(jwtProperties.getPublicKey()).thenReturn(publicKeyBase64);
        when(jwtProperties.getResetSecret()).thenReturn(resetSecret);
        when(jwtProperties.getAccessTokenExpiration()).thenReturn(Duration.ofMinutes(60));
        when(jwtProperties.getRefreshTokenExpiration()).thenReturn(Duration.ofDays(10));
        when(jwtProperties.getRememberMeRefreshTokenExpiration()).thenReturn(Duration.ofDays(30));
        when(jwtProperties.getResetTokenExpiration()).thenReturn(Duration.ofMinutes(15));

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
            assertThat(jwtService.getTokenVersion(token)).isZero();
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
            assertThat(jwtService.getTokenVersion(token)).isZero();
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

    @Nested
    class CreateResetTokenTest {

        @Test
        void whenUserIsValid_thenReturnResetToken() {
            // when
            String token = jwtService.createResetToken(buildUser());

            // then
            assertThat(token).isNotNull();
            assertThat(jwtService.getEmailFromResetToken(token)).isEqualTo(USER_EMAIL);
            verify(jwtProperties).getResetTokenExpiration();
        }

        @Test
        void whenUserIsNull_thenThrowException() {
            // when + then
            assertThatThrownBy(() -> jwtService.createResetToken(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    class GetEmailFromResetTokenTest {

        @Test
        void whenResetToken_thenReturnCorrectEmail() {
            // given
            String token = jwtService.createResetToken(buildUser());

            // when
            String email = jwtService.getEmailFromResetToken(token);

            // then
            assertThat(email).isEqualTo(USER_EMAIL);
        }

        @Test
        void whenTokenIsInvalid_thenThrowException() {
            // when + then
            assertThatThrownBy(() -> jwtService.getEmailFromResetToken(INVALID_TOKEN))
                    .isInstanceOf(InvalidTokenException.class);
        }

        @Test
        void whenAuthTokenPassedInsteadOfResetToken_thenThrowException() {
            // given - auth токен підписаний RSA, reset токен очікує HMAC
            String authToken = jwtService.createAccessToken(buildUser());

            // when + then
            assertThatThrownBy(() -> jwtService.getEmailFromResetToken(authToken))
                    .isInstanceOf(InvalidTokenException.class);
        }
    }

    @Nested
    class CheckCorrectExpirationTest {
        @Test
        void getExpiration_shouldReturnCorrectInstant_whenAccessTokenProvided() {
            Duration expectedTtl = Duration.ofMinutes(3);
            when(jwtProperties.getAccessTokenExpiration()).thenReturn(expectedTtl);

            Instant before = Instant.now().truncatedTo(ChronoUnit.SECONDS);
            String token = jwtService.createAccessToken(buildUser());
            Instant after = Instant.now().truncatedTo(ChronoUnit.SECONDS);

            Instant expiration = jwtService.getExpiration(token);

            assertThat(expiration).isAfterOrEqualTo(before.plus(expectedTtl));
            assertThat(expiration).isBeforeOrEqualTo(after.plus(expectedTtl).plusSeconds(1));
        }

        @Test
        void getExpiration_shouldThrowInvalidTokenException_whenTokenIsInvalid() {
            assertThatThrownBy(() -> jwtService.getExpiration("not.a.token"))
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
