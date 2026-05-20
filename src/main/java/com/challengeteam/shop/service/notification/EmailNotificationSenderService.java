package com.challengeteam.shop.service.notification;

import com.challengeteam.shop.constants.Notification_type;
import com.challengeteam.shop.constraints.userData.InputUserValidationRules;
import com.challengeteam.shop.dto.email.Notification;
import com.challengeteam.shop.exceptionHandling.exception.NotificationSendingException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.regex.Pattern;

/**
 * Service implementation for sending email notifications.
 * <p>
 * This service sends notifications via email using JavaMailSender. It validates that the notification
 * type is EMAIL and that the recipient email address follows the correct format before sending.
 * The sending operation is performed asynchronously to avoid blocking the calling thread.
 * </p>
 *
 * @see NotificationSenderService
 * @see JavaMailSender
 */
@Service
@RequiredArgsConstructor
@Validated
@Slf4j
public class EmailNotificationSenderService implements NotificationSenderService {

    /**
     * JavaMailSender used to create and send email messages.
     */
    private final JavaMailSender mailSender;

    /**
     * Sends an email notification asynchronously to the recipient specified in the notification object.
     * <p>
     * This method validates that the notification type is EMAIL and that the recipient email address
     * matches the expected email pattern. It constructs a MIME message with UTF-8 encoding and supports
     * HTML content in the message body.
     * </p>
     *
     * @param notification the notification details including recipient, sender, subject, and content.
     *                     Must not be null and must be valid according to validation constraints.
     * @param type         the type of notification to send. Must be {@link Notification_type#EMAIL}.
     * @throws NotificationSendingException if the notification type is not EMAIL or if the email address is invalid
     * @throws RuntimeException             if a {@link MessagingException} occurs during email sending
     */
    @Async
    @Override
    public void sendNotification(@NotNull @Valid Notification notification, Notification_type type) {
        if (type != Notification_type.EMAIL) {
            log.error("Notification type is not email: {}", type);
            throw new NotificationSendingException("Notification type is not email: %s".formatted(type));
        }
        String email = notification.to().toLowerCase();
        if (!Pattern.matches(InputUserValidationRules.EMAIL_PATTERN_CONSTRAINT, email)) {
            log.error("Email is not valid: {}", notification.to());
            throw new NotificationSendingException("Email is not valid: %s".formatted(email));
        }
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setTo(email);
            helper.setSubject(notification.subject());
            helper.setText(notification.context(), true);
            if (notification.from() == null) {
                helper.setFrom("Your GadgetRoom");
            } else {
                helper.setFrom(notification.from());
            }
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            log.error("Failed to send email to: {}", email, e);
            throw new RuntimeException(e);
        }
    }
}