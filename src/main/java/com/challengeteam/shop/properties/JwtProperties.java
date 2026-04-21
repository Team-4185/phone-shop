package com.challengeteam.shop.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {
    private String privateKey;
    private String publicKey;
    private String resetSecret;
    private Duration accessTokenExpiration;
    private Duration refreshTokenExpiration;
    private Duration rememberMeRefreshTokenExpiration;
    private Duration resetTokenExpiration;

    public static JwtProperties copyOf(JwtProperties other) {
        var properties = new JwtProperties();
        properties.setPrivateKey(other.privateKey);
        properties.setPublicKey(other.publicKey);
        properties.setResetSecret(other.resetSecret);
        properties.setAccessTokenExpiration(other.accessTokenExpiration);
        properties.setRefreshTokenExpiration(other.refreshTokenExpiration);
        properties.setRememberMeRefreshTokenExpiration(other.rememberMeRefreshTokenExpiration);
        properties.setResetTokenExpiration(other.resetTokenExpiration);

        return properties;
    }

}
