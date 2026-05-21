package com.challengeteam.shop.service.security.auth.logout.blackListTokenCache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisTokenRevocationService implements TokenRevocationService {

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void revoke(String token, Duration ttl) {
        String key = "blacklist:" + DigestUtils.sha256Hex(token);
        log.debug("Revoking token, key: {}, ttl: {}", key, ttl);
        redisTemplate.opsForValue().set(key, "revoked", ttl);
    }

    @Override
    public boolean isRevoked(String token) {
        String key = "blacklist:" + DigestUtils.sha256Hex(token);
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}