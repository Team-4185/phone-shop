package com.challengeteam.shop.dto.user.request.sensetiveData;

import com.challengeteam.shop.constraints.userData.InputUserValidationRules;
import jakarta.validation.constraints.Pattern;

public record UpdateUserSensitiveDataDto(
        @Pattern(regexp = InputUserValidationRules.PASSWORD_PATTERN_CONSTRAINT,
                message = InputUserValidationRules.PASSWORD_PATTERN_MESSAGE)
        String newPassword,
        String confirmPassword,
        String oldPassword,
        @Pattern(regexp = InputUserValidationRules.EMAIL_PATTERN_CONSTRAINT,
                message = "Email format is invalid")
        String newEmail
) {
}