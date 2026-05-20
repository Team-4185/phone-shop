package com.challengeteam.shop.constraints.order.orderInputData;

import lombok.experimental.UtilityClass;

/**
 * Utility class containing validation rules for shipping input data.
 * <p>
 * This class defines regular expression patterns used to validate shipping address fields
 * such as region, city, and country names.
 * </p>
 */
@UtilityClass
public class ShippingInputDataValidationRules {
    /**
     * Regular expression pattern for validating region, city, and country names.
     * <p>
     * The pattern allows:
     * <ul>
     *   <li>Latin letters (a-z, A-Z)</li>
     *   <li>Cyrillic letters (а-я, А-Я, ё, Ё)</li>
     *   <li>Spaces</li>
     *   <li>Hyphens (-)</li>
     *   <li>Apostrophes (')</li>
     * </ul>
     * The length must be between 2 and 50 characters.
     * </p>
     */
    public final String REGION_PATTER_CONSTRAINT = "^[a-zA-Zа-яА-ЯёЁ\\s\\-']{2,50}$";
}