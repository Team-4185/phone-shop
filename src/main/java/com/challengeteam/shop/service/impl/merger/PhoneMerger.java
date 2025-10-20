package com.challengeteam.shop.service.impl.merger;

import com.challengeteam.shop.dto.phone.PhoneUpdateRequestDto;
import com.challengeteam.shop.entity.phone.Phone;

public interface PhoneMerger {

    void mergePhone(Phone phone, PhoneUpdateRequestDto newPhone);

}
