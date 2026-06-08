package com.challengeteam.shop.dto.user.request;

import com.challengeteam.shop.constraints.userData.InputUserValidationRules;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ChangePasswordRequestDto(
        @NotBlank(message = "Current password must be present")
        String currentPassword,

        @NotBlank(message = "New password must be present")
        @Pattern(regexp = InputUserValidationRules.PASSWORD_PATTERN_CONSTRAINT,
                message = InputUserValidationRules.PASSWORD_PATTERN_MESSAGE)
        String newPassword,

        @NotBlank(message = "New password confirmation must be present")
        String confirmNewPassword
) {
}
