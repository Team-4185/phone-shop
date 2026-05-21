package com.challengeteam.shop.service.security.auth.logout.blackListTokenCache;

import java.time.Duration;

public interface TokenRevocationService {
    void revoke(String token, Duration ttl);

    boolean isRevoked(String token);
}