package com.challengeteam.shop.service.security.auth.logout;

import com.challengeteam.shop.exceptionHandling.exception.security.InvalidTokenException;
import com.challengeteam.shop.utility.security.jwt.RevokeTokenHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultLogoutService implements LogoutService {

    //    private final TokenRevocationService tokenRevocationService;
//    private final JwtService jwtService;
    private final RevokeTokenHelper revokeTokenHelper;

    @Override
    public void logout(String accessToken, String refreshToken) {
        try {
            revokeTokenHelper.revokeToken(accessToken);
            revokeTokenHelper.revokeToken(refreshToken);
        } catch (InvalidTokenException e) {
            if (e.getMessage() == null || e.getMessage().isBlank()) {
                throw new InvalidTokenException("Invalid token", e);
            }
            throw e;
        }
    }

//    private void revokeTokenHelper(String token) {
//        if (!jwtService.isValid(token)) {
//            log.warn("Attempted to revoke an invalid token");
//            throw new InvalidTokenException("Invalid token");
//        }
//        Instant expiration = jwtService.getExpiration(token);
//        Duration ttl = Duration.between(Instant.now(), expiration);
//        tokenRevocationService.revoke(token, ttl);
//        log.debug("Token revoked, expires in: {}", ttl);
//    }
}
