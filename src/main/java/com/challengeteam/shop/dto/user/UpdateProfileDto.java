package com.challengeteam.shop.dto.user;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateProfileDto(

        @Size(min = 3, max = 255, message = "First name must be between {min} and {max} characters")
        @Pattern(
                regexp = "^[A-Za-zА-Яа-яІіЇїЄєҐґ]+(?:[-' ]?[A-Za-zА-Яа-яІіЇїЄєҐґ]+)*$",
                message = "First name may contain one or more words with letters, spaces, hyphens, or apostrophes"
        )
        String firstName,

        @Size(min = 3, max = 255, message = "Last name must be between {min} and {max} characters")
        @Pattern(
                regexp = "^[A-Za-zА-Яа-яІіЇїЄєҐґ]+(?:[-' ]?[A-Za-zА-Яа-яІіЇїЄєҐґ]+)*$",
                message = "Last name may contain one or more words with letters, spaces, hyphens, or apostrophes"
        )
        String lastName,

        @Size(min = 3, max = 255, message = "City must be between {min} and {max} characters")
        @Pattern(
                regexp = "^[A-Za-zА-Яа-яІіЇїЄєҐґ]+(?:[-' ]?[A-Za-zА-Яа-яІіЇїЄєҐґ]+)*$",
                message = "City may contain one or more words with letters, spaces, hyphens, or apostrophes"
        )
        String city,

        @Pattern(
                regexp = "^(\\+?380)([0-9]{9})$",
                message = "Phone number must be in the format +380XXXXXXXXX"
        )
        String phoneNumber
) {}
