package com.challengeteam.shop.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequestDto(
        @NotBlank(message = "Token must be present")
        String token,

        @NotBlank(message = "Password must be present")
        @Size(min = 8, max = 50, message = "Password length must be between {min} and {max}")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*])[a-zA-Z0-9!@#$%^&*]+$",
                message = "Password must contains: capital letter, small letter, number and special symbol (!@#$%^&*)"
        )
        String newPassword
) {
}
