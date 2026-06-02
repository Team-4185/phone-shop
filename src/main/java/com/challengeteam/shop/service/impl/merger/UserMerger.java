package com.challengeteam.shop.service.impl.merger;

import com.challengeteam.shop.dto.user.request.UpdateProfileDto;
import com.challengeteam.shop.entity.user.User;

public interface UserMerger {

    void mergeProfile(User user, UpdateProfileDto newProfile);

}
