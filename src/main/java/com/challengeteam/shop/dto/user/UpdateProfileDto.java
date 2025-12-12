package com.challengeteam.shop.dto.user;

<<<<<<< HEAD
=======
import jakarta.validation.constraints.NotBlank;
>>>>>>> 850e3095f24ef9a459006d4b5830467ce2cc75e3
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateProfileDto(

        @Size(min = 3, max = 255, message = "First name must be between {min} and {max} characters")
        @Pattern(
                regexp = "^[A-Za-zА-Яа-яІіЇїЄєҐґ]+(?:[-' ]?[A-Za-zА-Яа-яІіЇїЄєҐґ]+)*$",
                message = "First name can contain letters only"
        )
        String newFirstname,

        @Size(min = 3, max = 255, message = "Last name must be between {min} and {max} characters")
        @Pattern(
                regexp = "^[A-Za-zА-Яа-яІіЇїЄєҐґ]+(?:[-' ]?[A-Za-zА-Яа-яІіЇїЄєҐґ]+)*$",
                message = "Last name can contain letters only"
        )
        String newLastname,

        @Size(min = 3, max = 255, message = "City must be between {min} and {max} characters")
        @Pattern(
                regexp = "^[A-Za-zА-Яа-яІіЇїЄєҐґ]+(?:[-' ]?[A-Za-zА-Яа-яІіЇїЄєҐґ]+)*$",
                message = "City can contain letters only"
        )
        String newCity,

        @Pattern(
                regexp = "^(\\+?380)([0-9]{9})$",
                message = "Phone number must be in the format +380XXXXXXXXX"
        )
        String newPhoneNumber
) {}
