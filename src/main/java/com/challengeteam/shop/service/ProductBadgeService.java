package com.challengeteam.shop.service;

import com.challengeteam.shop.dto.phone.response.PhoneResponseDto;
import com.challengeteam.shop.entity.phone.Phone;
import com.challengeteam.shop.service.model.ProductBadgeInfo;

import java.util.List;
import java.util.Map;

/**
 * Resolves catalog product badges that depend on current time and purchase history.
 */
public interface ProductBadgeService {
    Map<Long, ProductBadgeInfo> getBadges(List<Phone> phones);

    PhoneResponseDto applyBadges(PhoneResponseDto phoneResponseDto, ProductBadgeInfo productBadgeInfo);

    List<PhoneResponseDto> applyBadges(List<PhoneResponseDto> phoneResponseDtos, List<Phone> phones);
}
