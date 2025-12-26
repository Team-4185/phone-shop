package com.challengeteam.shop.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "cart")
public class UserCartProperties {

    private Integer minUpdate;

    private Integer maxUpdate;

    private Integer totalAmount;

}
