package com.challengeteam.shop.service.mock;

import com.challengeteam.shop.dto.order.request.payment.PaymentDetailsRequestDto;
import com.challengeteam.shop.dto.payment.TransactionResult;

import java.math.BigDecimal;

/**
 * Mock service interface for simulating payment processing operations.
 * <p>
 * This service provides a mock implementation for testing payment flows without
 * connecting to real payment gateways. It simulates various payment scenarios
 * including successful payments, blocked cards, and insufficient funds.
 * </p>
 */
public interface PaymentMockService {
    /**
     * Processes a payment transaction with the provided payment details and amount.
     * <p>
     * This method simulates payment processing by introducing a random delay (4-8 seconds)
     * and evaluating the card number to determine the transaction outcome:
     * </p>
     * <ul>
     *   <li>Cards starting with "0000" will result in a failed transaction (card blocked)</li>
     *   <li>Cards starting with "9999" will result in a failed transaction (insufficient funds)</li>
     *   <li>All other card numbers will result in a successful payment with a generated transaction ID</li>
     * </ul>
     *
     * @param paymentDetailsRequestDto the payment details including card information,
     *                                 cardholder name, expiration date, and CVV
     * @param amount                   the payment amount to be processed
     * @return a {@link TransactionResult} containing the payment status, transaction ID
     * (if successful), and error message (if failed)
     */
    TransactionResult pay(PaymentDetailsRequestDto paymentDetailsRequestDto, BigDecimal amount);
}