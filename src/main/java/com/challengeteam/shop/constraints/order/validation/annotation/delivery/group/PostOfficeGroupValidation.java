package com.challengeteam.shop.constraints.order.validation.annotation.delivery.group;

/**
 * Validation group for post office delivery type.
 * <p>
 * This validation group is used to apply specific constraints when the delivery method
 * is post office delivery. Fields validated under this group include:
 * <ul>
 *     <li>Logistics company (required)</li>
 *     <li>Logistic post office (required)</li>
 *     <li>City (required)</li>
 *     <li>Region (required)</li>
 *     <li>Country (required)</li>
 *     <li>Zip code (required)</li>
 * </ul>
 *
 * @see com.challengeteam.shop.dto.order.request.shippingAddress.ShippingAddressRequestDto
 * @see com.challengeteam.shop.constraints.order.validation.annotation.delivery.group.CourierGroupValidation
 */
public interface PostOfficeGroupValidation {
}