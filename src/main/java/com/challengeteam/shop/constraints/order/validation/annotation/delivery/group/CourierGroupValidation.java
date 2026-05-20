package com.challengeteam.shop.constraints.order.validation.annotation.delivery.group;

/**
 * Validation group for courier delivery type.
 * <p>
 * This validation group is used to apply specific constraints when the delivery method
 * is courier delivery. Fields validated under this group include:
 * <ul>
 *     <li>Apartment number (required)</li>
 *     <li>House number (required)</li>
 *     <li>Street (required)</li>
 *     <li>City (required)</li>
 *     <li>Region (required)</li>
 *     <li>Country (required)</li>
 *     <li>Zip code (required)</li>
 *     <li>Logistics company (required)</li>
 * </ul>
 *
 * @see com.challengeteam.shop.dto.order.request.shippingAddress.ShippingAddressRequestDto
 * @see com.challengeteam.shop.constraints.order.validation.annotation.delivery.group.PostOfficeGroupValidation
 */
public interface CourierGroupValidation {
}