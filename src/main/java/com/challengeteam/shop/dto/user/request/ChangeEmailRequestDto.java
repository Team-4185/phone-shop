package com.challengeteam.shop.dto.user.request;

import com.challengeteam.shop.constraints.userData.InputUserValidationRules;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ChangeEmailRequestDto(
        @NotBlank(message = "New email must be present")
        @Pattern(regexp = InputUserValidationRules.EMAIL_PATTERN_CONSTRAINT,
                message = "Email format is invalid")
        String newEmail,

        @NotBlank(message = "Current password must be present")
        String currentPassword
) {
}
