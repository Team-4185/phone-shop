package com.challengeteam.shop.dto.email;

import jakarta.validation.constraints.NotBlank;

public record Notification(
        @NotBlank
        String to,
        String from,
        @NotBlank(message = "Subject is required")
        String subject,
        @NotBlank(message = "Context is required")
        String context
) {
}