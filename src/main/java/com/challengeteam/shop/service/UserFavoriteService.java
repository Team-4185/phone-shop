package com.challengeteam.shop.service;

import com.challengeteam.shop.dto.phone.PhoneResponseDto;

import java.util.List;

public interface UserFavoriteService {

    List<PhoneResponseDto> getUserFavorites(Long userId);

    List<PhoneResponseDto> addProductToFavorites(Long userId, Long phoneId);

    List<PhoneResponseDto> removeProductFromFavorites(Long userId, Long phoneId);
}
