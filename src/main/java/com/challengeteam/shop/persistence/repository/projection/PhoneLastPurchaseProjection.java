package com.challengeteam.shop.persistence.repository.projection;

import java.time.Instant;

public interface PhoneLastPurchaseProjection {
    Long getPhoneId();

    Instant getLastPurchasedAt();
}
