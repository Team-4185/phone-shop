package com.challengeteam.shop.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties("payment.stripe")
public class StripeProperties {
    private String publicKey;
    private String privateKey;
    private String paymentSuccessUrl;
    private String webhookSecret;
    private Integer checkoutExpirationTime;
}
