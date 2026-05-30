package com.challengeteam.shop.service.security.auth.logout.blackListTokenCache;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisTokenRevocationServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private RedisTokenRevocationService service;


    /**
     * cases:
     * if a token is present in cache -> return true
     * if the token is not present in cache -> return false
     */
    @Nested
    class IsRevoked {

        @Test
        void shouldReturnFalse_whenTokenIsNotPresentInCache() {
            String token = "test.jwt.token";
            String expectedKey = "blacklist:" + DigestUtils.sha256Hex(token);

            when(redisTemplate.hasKey(expectedKey)).thenReturn(false);

            assertThat(service.isRevoked(token)).isFalse();
        }

        @Test
        void shouldReturnTrue_whenTokenIsPresentInCache() {
            String token = "test.jwt.token";
            String expectedKey = "blacklist:" + DigestUtils.sha256Hex(token);

            when(redisTemplate.hasKey(expectedKey)).thenReturn(true);

            assertThat(service.isRevoked(token)).isTrue();
        }
    }

    /**
     * cases:
     * store hashed token with correct ttl
     * store distinct keys for different tokens
     */
    @Nested
    class Revoke {

        @BeforeEach
        void setUp() {
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        }

        @Test
        void shouldStoreHashedTokenWithCorrectTtl() {
            String token = "test.jwt.token";
            Duration ttl = Duration.ofMinutes(15);
            String expectedKey = "blacklist:" + DigestUtils.sha256Hex(token);

            service.revoke(token, ttl);

            verify(valueOperations).set(expectedKey, "revoked", ttl);
        }

        @Test
        void shouldStoreDistinctKeysForDifferentTokens() {
            String tokenA = "token.a";
            String tokenB = "token.b";
            Duration ttl = Duration.ofMinutes(15);

            service.revoke(tokenA, ttl);
            service.revoke(tokenB, ttl);

            verify(valueOperations).set(
                    "blacklist:" + DigestUtils.sha256Hex(tokenA), "revoked", ttl);
            verify(valueOperations).set(
                    "blacklist:" + DigestUtils.sha256Hex(tokenB), "revoked", ttl);
        }
    }
}