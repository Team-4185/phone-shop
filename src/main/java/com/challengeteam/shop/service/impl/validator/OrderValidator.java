package com.challengeteam.shop.service.impl.validator;

import com.challengeteam.shop.dto.order.OrderDestination;
import com.challengeteam.shop.dto.order.OrderPaymentDetail;
import com.challengeteam.shop.dto.order.OrderRecipient;
import com.challengeteam.shop.entity.phone.Phone;
import com.challengeteam.shop.entity.user.User;

import java.util.Map;

public interface OrderValidator {

    void validateProducts(Map<Phone, Integer> products);
    void validateUser(User user);
    void validateDestination(OrderDestination destination);
    void validateRecipient(OrderRecipient recipient);
    void validatePaymentDetail(OrderPaymentDetail paymentDetail);

}
