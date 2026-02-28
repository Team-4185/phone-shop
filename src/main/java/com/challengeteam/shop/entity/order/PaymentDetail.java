package com.challengeteam.shop.entity.order;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Embeddable
public class PaymentDetail {
    @Column(name = "payment_paid", nullable = false)
    private boolean paid;

    @Column(name = "payment_method", nullable = false, length = 100)
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Column(name = "payment_intent_id", nullable = true, length = 255)
    private String intentId;

    @Column(name = "payment_checkout_url", nullable = true, length = 1000)
    private String checkoutUrl;

    public PaymentDetail(boolean paid, PaymentMethod paymentMethod) {
        this.paid = paid;
        this.paymentMethod = paymentMethod;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PaymentDetail that = (PaymentDetail) o;

        return paid == that.paid
               && paymentMethod == that.paymentMethod
               && Objects.equals(intentId, that.intentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(paid, paymentMethod, intentId);
    }

    @Override
    public String toString() {
        return "PaymentDetail{" +
               "isPaid=" + paid +
               ", paymentMethod=" + paymentMethod +
               ", transactionId='" + intentId + '\'' +
               '}';
    }
}
