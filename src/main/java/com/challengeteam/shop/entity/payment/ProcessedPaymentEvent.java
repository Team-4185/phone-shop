package com.challengeteam.shop.entity.payment;

import com.challengeteam.shop.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "processed_payment_events")
public class ProcessedPaymentEvent extends BaseEntity {

    @Column(nullable = false, length = 40)
    private String provider;

    @Column(nullable = false, unique = true, length = 120)
    private String externalEventId;

    @Column(nullable = false, length = 120)
    private String externalTransactionId;
}
