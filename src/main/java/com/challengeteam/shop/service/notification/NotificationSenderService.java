package com.challengeteam.shop.service.notification;

import com.challengeteam.shop.constants.Notification_type;
import com.challengeteam.shop.dto.email.Notification;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * A functional interface for sending notifications to users.
 * <p>
 * This service provides a standardized way to send notifications through different channels
 * (e.g., email, SMS) based on the specified notification type.
 * </p>
 *
 * @see Notification
 * @see Notification_type
 */
@FunctionalInterface
public interface NotificationSenderService {
    /**
     * Sends a notification to the recipient specified in the notification object.
     *
     * @param notification the notification details including recipient, sender, subject, and content.
     *                     Must not be null and must be valid according to validation constraints.
     * @param type         the type of notification to send (e.g., EMAIL, SMS).
     *                     The implementation should validate that it supports the specified type.
     * @throws com.challengeteam.shop.exceptionHandling.exception.NotificationSendingException if the notification type is not supported or if the notification fails to send
     * @throws jakarta.validation.ConstraintViolationException                                 if the notification object fails validation
     */
    void sendNotification(@NotNull @Valid Notification notification, Notification_type type);
}