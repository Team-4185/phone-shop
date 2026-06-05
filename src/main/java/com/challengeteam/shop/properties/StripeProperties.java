package com.challengeteam.shop.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "payment.stripe")
public class StripeProperties {
    private String secretKey;
    private String webhookSecret;
    private String currency = "usd";
}
