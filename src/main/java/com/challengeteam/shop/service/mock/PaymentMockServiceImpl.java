package com.challengeteam.shop.service.mock;

import com.challengeteam.shop.dto.order.request.payment.PaymentDetailsRequestDto;
import com.challengeteam.shop.dto.payment.TransactionResult;
import com.challengeteam.shop.entity.order.payment.PaymentStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * Mock implementation of {@link PaymentMockService} for simulating payment processing.
 * <p>
 * This service simulates payment processing behavior for testing purposes without
 * connecting to real payment gateways. It introduces artificial delays and provides
 * deterministic outcomes based on card number patterns.
 * </p>
 */
@Service
@Slf4j
public class PaymentMockServiceImpl implements PaymentMockService {

    /**
     * Processes a payment transaction with the provided payment details and amount.
     * <p>
     * This method simulates payment processing by:
     * </p>
     * <ol>
     *   <li>Introducing a random delay (4-8 seconds) to simulate network latency</li>
     *   <li>Evaluating the card number to determine the transaction outcome:
     *     <ul>
     *       <li>Cards starting with "0000" result in a failed transaction (card blocked)</li>
     *       <li>Cards starting with "9999" result in a failed transaction (insufficient funds)</li>
     *       <li>All other card numbers result in a successful payment with a generated transaction ID</li>
     *     </ul>
     *   </li>
     * </ol>
     *
     * @param paymentDetailsRequestDto the payment details including card information,
     *                                 cardholder name, expiration date, and CVV
     * @param amount                   the payment amount to be processed
     * @return a {@link TransactionResult} containing the payment status, transaction ID
     * (if successful), and error message (if failed)
     */
    @Override
    public TransactionResult pay(PaymentDetailsRequestDto paymentDetailsRequestDto, BigDecimal amount) {
        simulatePaymentDelay();
        String cardNumber = paymentDetailsRequestDto.cardNumber();
        if (cardNumber.startsWith("0000")) {
            log.error("Card is blocked: {}", cardNumber);
            return new TransactionResult(PaymentStatus.FAILED, null, "Card is blocked");
        } else if (cardNumber.startsWith("9999")) {
            log.error("Insufficient funds: {}", cardNumber);
            return new TransactionResult(PaymentStatus.FAILED, null, "Insufficient funds");
        } else {
            return new TransactionResult(PaymentStatus.PAID, UUID.randomUUID().toString(), null);
        }
    }

    /**
     * Simulates a payment processing delay by sleeping for a random duration.
     * <p>
     * The delay is randomly selected between 4 and 8 seconds to mimic real-world
     * network latency and payment gateway processing time. If the thread is interrupted
     * during the delay, the interruption status is restored.
     * </p>
     */
    private void simulatePaymentDelay() {
        int sleepTime = ThreadLocalRandom.current().nextInt(4, 8);
        log.info("Simulating payment delay for {} seconds", sleepTime);
        try {
            TimeUnit.SECONDS.sleep(sleepTime);
        } catch (InterruptedException e) {
            log.error("Payment delay interrupted", e);
            Thread.currentThread().interrupt();
        }
    }
}