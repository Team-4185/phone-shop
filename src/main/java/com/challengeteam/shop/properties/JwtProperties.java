package com.challengeteam.shop.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

@Service
@ConfigurationProperties(prefix = "security.jwt")
@Data
public class JwtProperties {

    private String privateKey;

    private String publicKey;

    private long accessTokenExpiration;

    private long refreshTokenExpiration;

}
