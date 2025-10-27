package com.challengeteam.shop.service.impl.merger;

import com.challengeteam.shop.dto.phone.PhoneUpdateRequestDto;
import com.challengeteam.shop.entity.phone.Phone;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Objects;

@Component
public class PhoneMergerImpl implements PhoneMerger {

    @Override
    public void mergePhone(Phone phone, PhoneUpdateRequestDto newPhone) {
        Objects.requireNonNull(phone,"phone");
        Objects.requireNonNull(newPhone,"newPhone");

        String newName = newPhone.newName();
        if (newName != null) {
            phone.setName(newName);
        }

        String newDescription = newPhone.newDescription();
        if (newDescription != null) {
            phone.setDescription(newDescription);
        }

        BigDecimal newPrice = newPhone.newPrice();
        if (newPrice != null) {
            phone.setPrice(newPrice);
        }

        String newBrand = newPhone.newBrand();
        if (newBrand != null) {
            phone.setBrand(newBrand);
        }

        Integer newReleaseYear = newPhone.newReleaseYear();
        if (newReleaseYear != null) {
            phone.setReleaseYear(newReleaseYear);
        }
    }

}
