package com.challengeteam.shop.constraints.filter.validation.validator;

import com.challengeteam.shop.constraints.filter.validation.annotation.MinPriceNotExceedMaxPrice;
import com.challengeteam.shop.dto.pagination.paginationRequest.PhoneMultipleFilterRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Custom constraint validator that ensures the minimum price does not exceed the maximum price.
 * <p>
 * This validator is used in conjunction with the {@link MinPriceNotExceedMaxPrice} annotation
 * to validate {@link PhoneMultipleFilterRequest} objects at the type level.
 * </p>
 * <p>
 * The validation logic performs the following checks:
 * <ul>
 *   <li>If either {@code maxPrice} or {@code minPrice} is {@code null}, validation passes (returns {@code true})</li>
 *   <li>If both prices are non-null, validates that {@code minPrice <= maxPrice}</li>
 * </ul>
 * </p>
 *
 * <p><strong>Validation Rules:</strong></p>
 * <ul>
 *   <li>Valid: minPrice = null, maxPrice = 100</li>
 *   <li>Valid: minPrice = 50, maxPrice = null</li>
 *   <li>Valid: minPrice = 50, maxPrice = 100</li>
 *   <li>Valid: minPrice = 50, maxPrice = 50</li>
 *   <li>Invalid: minPrice = 100, maxPrice = 50</li>
 * </ul>
 *
 * @see MinPriceNotExceedMaxPrice
 * @see PhoneMultipleFilterRequest
 */
public class MinPriceNotExceedMaxPriceValidator
        implements ConstraintValidator<MinPriceNotExceedMaxPrice, PhoneMultipleFilterRequest> {
    /**
     * Validates that the minimum price does not exceed the maximum price in the given request.
     * <p>
     * This method is called automatically by the Jakarta Bean Validation framework when
     * validating objects annotated with {@link MinPriceNotExceedMaxPrice}.
     * </p>
     *
     * @param request the {@link PhoneMultipleFilterRequest} object to validate; must not be {@code null}
     * @param context the validation context provided by the framework
     * @return {@code true} if either price is {@code null} or if {@code minPrice <= maxPrice};
     * {@code false} otherwise
     */
    @Override
    public boolean isValid(PhoneMultipleFilterRequest request, ConstraintValidatorContext context) {
        if (request.maxPrice() == null || request.minPrice() == null) {
            return true;
        }
        return request.minPrice().compareTo(request.maxPrice()) <= 0;
    }
}