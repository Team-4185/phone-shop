package com.challengeteam.shop.utility;

import lombok.experimental.UtilityClass;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Utility class for normalizing user input strings.
 * <p>
 * Provides methods to standardize text formatting for names, emails, and other user-provided data.
 * </p>
 */
@UtilityClass
public class InputNormalizer {
    /**
     * Converts a string to title case format.
     * <p>
     * Each word in the input string is capitalized, with the first letter in uppercase
     * and the remaining letters in lowercase. Multiple whitespace characters are normalized
     * to single spaces.
     * </p>
     * <p>
     * Examples:
     * <ul>
     *   <li>"john" → "John"</li>
     *   <li>"JOHN DOE" → "John Doe"</li>
     *   <li>"john doe" → "John Doe"</li>
     * </ul>
     * </p>
     *
     * @param input the input string to convert
     * @return the title-cased string, or the original input if null or blank
     */
    public static String toTitleCase(String input) {
        if (input == null || input.isBlank()) return input;
        return Arrays.stream(input.trim().split("\\s+"))
                .map(word -> word.isEmpty() ? word
                        : Character.toUpperCase(word.charAt(0))
                          + word.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
    }

    /**
     * Normalizes an email address to lowercase format.
     * <p>
     * Converts the entire email string to lowercase and removes leading/trailing whitespace.
     * This ensures consistent email formatting for storage and comparison.
     * </p>
     * <p>
     * Example: "Customer@Example.COM" → "customer@example.com"
     * </p>
     *
     * @param input the input email string to normalize
     * @return the normalized email in lowercase, or the original input if null or blank
     */
    public static String toEmail(String input) {
        if (input == null || input.isBlank()) return input;
        return input.trim().toLowerCase();
    }
}