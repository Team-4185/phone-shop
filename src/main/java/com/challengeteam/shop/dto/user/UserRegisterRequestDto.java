package com.challengeteam.shop.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserRegisterRequestDto(
        @NotBlank(message = "Email must be present")
        @Size(min = 10, max = 100, message = "Email length must be between {min} and {max}")
        @Pattern(regexp = "^[a-zA-Z0-9._%+-]{3,}@[a-zA-Z0-9.-]{3,}\\.[a-zA-Z]{2,}$", message = "Email must match the pattern 'xxx@xxx.xx'")
        String email,
        @NotBlank(message = "Password must be present")
        @Size(min = 8, max = 100, message = "Password length must be between {min} and {max}")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*])[a-zA-Z0-9!@#$%^&*]+$",
                message = "Password must contains: capital letter, small letter, number and special symbol (!@#$%^&*)"
        )
        String password,
        @NotBlank(message = "Password confirmation must be present")
        @Size(min = 8, max = 100, message = "Password confirmation length must be between {min} and {max}")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*])[a-zA-Z0-9!@#$%^&*]+$",
                message = "Password confirmation must contains: capital letter, small letter, number and special symbol (!@#$%^&*)"
        )
        String passwordConfirmation
) {
}
