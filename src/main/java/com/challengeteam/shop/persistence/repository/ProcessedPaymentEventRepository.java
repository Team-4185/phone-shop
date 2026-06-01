package com.challengeteam.shop.persistence.repository;

import com.challengeteam.shop.entity.payment.ProcessedPaymentEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcessedPaymentEventRepository extends JpaRepository<ProcessedPaymentEvent, Long> {
    boolean existsByExternalEventId(String externalEventId);
}
