package com.challengeteam.shop.service.user.personalInfo;

import com.challengeteam.shop.dto.user.response.UserPersonalInfoResponseDto;

public interface UserPersonalInfoService {
    UserPersonalInfoResponseDto getUserPersonalInfo(String username);
}