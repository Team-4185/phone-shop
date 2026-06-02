package com.challengeteam.shop.mapper.user;

import com.challengeteam.shop.dto.user.response.UserPersonalInfoResponseDto;
import com.challengeteam.shop.entity.favorite.Favorite;
import com.challengeteam.shop.entity.order.Order;
import com.challengeteam.shop.entity.user.User;
import com.challengeteam.shop.mapper.cart.CartMapper;
import com.challengeteam.shop.mapper.order.OrderMapper;
import com.challengeteam.shop.mapper.phone.PhoneMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {RoleMapper.class, CartMapper.class,
                OrderMapper.class, PhoneMapper.class})
public interface UserPersonalInfoMapper {

    @Mapping(source = "orderList", target = "orders")
    @Mapping(source = "favoriteList", target = "favorites")
    UserPersonalInfoResponseDto toDto(User user, List<Favorite> favoriteList, List<Order> orderList);
}