package com.challengeteam.shop.dto.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record OrderRecipient(
        @NotBlank(message = "Firstname is required")
        @Size(min = 3,max = 255, message = "Firstname length must be between {min} and {max} symbols")
        String firstname,

        @NotBlank(message = "Lastname is required")
        @Size(min = 3,max = 255, message = "Lastname length must be between {min} and {max} symbols")
        String lastname,

        @NotBlank(message = "Email is required")
        @Size(min = 10, max = 100, message = "Email length must be between {min} and {max} symbols")
        @Pattern(regexp = "^[a-zA-Z0-9._%+-]{3,}@[a-zA-Z0-9.-]{3,}\\.[a-zA-Z]{2,}$", message = "Email must match the pattern 'xxx@xxx.xx'")
        String email,

        @NotBlank(message = "Phone is required")
        @Pattern(regexp = "^(\\+380)([0-9]{9})$", message = "Phone number must be in the format +380XXXXXXXXX")
        @Size(min = 13, max = 13, message = "Phone number length must be 13 symbols")
        String phone
) {
}
