package com.challengeteam.shop.mapper;

import com.challengeteam.shop.dto.order.OrderResponseDto;
import com.challengeteam.shop.entity.order.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {OrderItemMapper.class})
public interface OrderMapper {

    @Mapping(source = "paymentDetail.paid", target = "paymentPaid")
    @Mapping(source = "paymentDetail.paymentMethod", target = "paymentMethod")
    @Mapping(source = "paymentDetail.checkoutUrl", target = "paymentCheckoutUrl")
    @Mapping(source = "recipient.email", target = "recipientEmail")
    @Mapping(source = "recipient.phone", target = "recipientPhone")
    @Mapping(source = "recipient.firstname", target = "recipientFirstname")
    @Mapping(source = "recipient.lastname", target = "recipientLastname")
    @Mapping(source = "orderItems", target = "orderItems")
    OrderResponseDto toResponse(Order order);

    List<OrderResponseDto> toResponse(List<Order> orders);

}
