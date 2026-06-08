package com.challengeteam.shop.persistence.repository.projection;

public interface PhoneReviewSummaryProjection {
    Long getPhoneId();

    Double getAverageRating();

    Long getReviewsCount();
}
