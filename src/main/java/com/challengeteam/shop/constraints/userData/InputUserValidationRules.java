package com.challengeteam.shop.constraints.userData;

import lombok.experimental.UtilityClass;

/**
 * Utility class containing validation pattern constants for user input data.
 * This class provides regular expression patterns used to validate various user-related fields
 * such as email addresses, names, and phone numbers.
 *
 * <p>This is a utility class and cannot be instantiated due to the {@code @UtilityClass} annotation.</p>
 */
@UtilityClass
public class InputUserValidationRules {
    /**
     * Regular expression pattern for validating email addresses.
     *
     * <p>The pattern validates emails in the format: localpart@domain.tld</p>
     * <ul>
     *   <li>Local part: alphanumeric characters and special characters (._%+-)</li>
     *   <li>Domain: alphanumeric characters, dots, and hyphens</li>
     *   <li>Top-level domain: at least 2 alphabetic characters</li>
     * </ul>
     *
     * <p>Example valid emails: user@example.com, test.user+tag@sub-domain.co.uk</p>
     */
    public final String EMAIL_PATTERN_CONSTRAINT = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

    /**
     * Regular expression pattern for validating person names.
     *
     * <p>The pattern accepts names containing:</p>
     * <ul>
     *   <li>Latin letters (a-z, A-Z)</li>
     *   <li>Cyrillic letters, including Ukrainian-specific letters (і, ї, є, ґ)</li>
     *   <li>Special characters: apostrophe ('), comma (,), period (.), space, and hyphen (-)</li>
     * </ul>
     *
     * <p>Example valid names: John Smith, Mary-Jane O'Connor, Иван Петров</p>
     */
    public final String NAME_PATTERN_CONSTRAINT =
            "^[a-zA-Zа-яА-ЯёЁіІїЇєЄґҐ]+(([',. -][a-zA-Zа-яА-ЯёЁіІїЇєЄґҐ ])?[a-zA-Zа-яА-ЯёЁіІїЇєЄґҐ]*)*$";

    /**
     * Regular expression pattern for validating international phone numbers in E.164 format.
     *
     * <p>The pattern validates phone numbers with:</p>
     * <ul>
     *   <li>Leading plus sign (+)</li>
     *   <li>Country code starting with 1-9</li>
     *   <li>Total of 1 to 14 digits after the country code</li>
     * </ul>
     *
     * <p>Example valid phone numbers: +1234567890, +442071234567, +79161234567</p>
     */
    public final String PHONE_NUMBER_PATTERN_CONSTRAINT = "^\\+[1-9]\\d{1,14}$";
}
