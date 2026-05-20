package com.challengeteam.shop.mapper.order;

import com.challengeteam.shop.dto.order.response.paymentDetails.PaymentDetailsResponseDto;
import com.challengeteam.shop.entity.order.payment.PaymentDetails;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderPaymentDetailsMapper {
    PaymentDetailsResponseDto toPaymentDetailsResponseDto(PaymentDetails paymentDetails);
}
