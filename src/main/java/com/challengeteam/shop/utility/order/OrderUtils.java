package com.challengeteam.shop.utility.order;

import com.challengeteam.shop.dto.order.request.item.OrderItemRequestDto;
import com.challengeteam.shop.entity.phone.Phone;
import com.challengeteam.shop.entity.phone.ProductStatus;
import com.challengeteam.shop.exceptionHandling.exception.order.OrderCreationException;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Utility class providing helper methods for order processing operations.
 * <p>
 * This class contains static methods for calculating order totals, validating stock availability,
 * and managing phone inventory during order processing.
 * </p>
 */
@UtilityClass
@Slf4j
public class OrderUtils {
    /**
     * Calculates the total price of an order based on phone prices and quantities.
     * <p>
     * Iterates through all order items, multiplies each phone's price by its quantity,
     * and sums the results to produce the total order price.
     * </p>
     *
     * @param phoneMap a map of phone IDs to {@link Phone} entities
     * @param items    a list of {@link OrderItemRequestDto} containing phone IDs and quantities
     * @return the total price of the order as a {@link BigDecimal}
     */
    public static BigDecimal calculateTotalOrderPrice(Map<Long, Phone> phoneMap, List<OrderItemRequestDto> items) {
        return items.stream()
                .map(item -> {
                    Phone phone = phoneMap.get(item.phoneId());
                    return phone.getPrice().multiply(BigDecimal.valueOf(item.quantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Validates that sufficient stock is available for all phones in the order.
     * <p>
     * Checks each order item against the current stock levels. If any phone has insufficient
     * stock to fulfill the order, an {@link OrderCreationException} is thrown with details
     * of the unavailable phones.
     * </p>
     *
     * @param phoneMap a map of phone IDs to {@link Phone} entities
     * @param items    a list of {@link OrderItemRequestDto} containing phone IDs and quantities
     * @throws OrderCreationException if one or more phones have insufficient stock
     */
    public static void checkIfStockAvailable(Map<Long, Phone> phoneMap, List<OrderItemRequestDto> items) {
        List<String> unavailablePhones = items.stream()
                .filter(item -> phoneMap.get(item.phoneId()).getStock() < item.quantity())
                .map(item -> phoneMap.get(item.phoneId()).getName())
                .toList();

        if (!unavailablePhones.isEmpty()) {
            log.error("Not enough stock for phones: {}", unavailablePhones);
            throw new OrderCreationException("Not enough stock for phones: " + unavailablePhones);
        }
    }

    /**
     * Updates the stock level of a phone after an order is placed.
     * <p>
     * Decreases the phone's stock by the ordered quantity and updates the product status
     * based on the remaining stock:
     * <ul>
     *   <li>If remaining stock is 0, sets status to {@link ProductStatus#OUT_OF_STOCK}</li>
     *   <li>If remaining stock is 5 or less, sets status to {@link ProductStatus#LOW_STOCK}</li>
     * </ul>
     * </p>
     *
     * @param phone           the {@link Phone} entity whose stock needs to be updated
     * @param orderedQuantity the quantity of phones ordered
     */
    public static void updatePhoneStock(Phone phone, int orderedQuantity) {
        int remainingStock = phone.getStock() - orderedQuantity;
        phone.setStock(remainingStock);
        log.info("Updated phone stock for phone: {} with remaining stock: {}", phone.getId(), remainingStock);
        if (remainingStock == 0) {
            log.info("Phone {} is out of stock", phone.getId());
            phone.setStatus(ProductStatus.OUT_OF_STOCK);
        } else if (remainingStock <= 5) {
            log.info("Phone {} is low on stock", phone.getId());
            phone.setStatus(ProductStatus.LOW_STOCK);
        }
    }
}