package com.challengeteam.shop.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateProfileDto(

        @NotBlank(message = "First name must not be empty")
        @Size(min = 3, max = 255, message = "First name must be between {min} and {max} characters")
        @Pattern(
                regexp = "^[A-Za-zА-Яа-яІіЇїЄєҐґ]+$",
                message = "First name can contain letters only"
        )
        String newFirstname,

        @NotBlank(message = "Last name must not be empty")
        @Size(min = 3, max = 255, message = "Last name must be between {min} and {max} characters")
        @Pattern(
                regexp = "^[A-Za-zА-Яа-яІіЇїЄєҐґ]+$",
                message = "Last name can contain letters only"
        )
        String newLastname,

        @NotBlank(message = "City must not be empty")
        @Size(min = 3, max = 255, message = "City must be between {min} and {max} characters")
        @Pattern(
                regexp = "^[A-Za-zА-Яа-яІіЇїЄєҐґ]+$",
                message = "City can contain letters only"
        )
        String newCity,

        @NotBlank(message = "Phone number must not be empty")
        @Pattern(
                regexp = "^(\\+?380)([0-9]{9})$",
                message = "Phone number must be in the format +380XXXXXXXXX"
        )
        String newPhoneNumber
) {}
