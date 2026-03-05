package com.challengeteam.shop.properties;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.PositiveValidatorForNumber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {
    private String privateKey;
    private String publicKey;
    private long accessTokenExpiration;
    private long refreshTokenExpiration;

    public static JwtProperties copyOf(JwtProperties other) {
        var properties = new JwtProperties();
        properties.setPrivateKey(other.privateKey);
        properties.setPublicKey(other.publicKey);
        properties.setAccessTokenExpiration(other.accessTokenExpiration);
        properties.setRefreshTokenExpiration(other.refreshTokenExpiration);

        return properties;
    }

}
