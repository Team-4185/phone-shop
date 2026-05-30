package com.challengeteam.shop.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ForgotPasswordRequestDto(
        @NotBlank(message = "Email must be present")
        @Size(min = 10, max = 100, message = "Email length must be between {min} and {max}")
        @Pattern(regexp = "^[a-zA-Z0-9._%+-]{3,}@[a-zA-Z0-9.-]{3,}\\.[a-zA-Z]{2,}$", message = "Email must match the pattern 'xxx@xxx.xx'")
        String email
) {
}
