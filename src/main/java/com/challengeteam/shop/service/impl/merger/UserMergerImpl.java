package com.challengeteam.shop.service.impl.merger;

import com.challengeteam.shop.dto.user.UpdateProfileDto;
import com.challengeteam.shop.entity.user.User;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class UserMergerImpl implements UserMerger {

    @Override
    public void mergeProfile(User user, UpdateProfileDto newProfile) {
        Objects.requireNonNull(user,"user");
        Objects.requireNonNull(newProfile,"newProfile");

        String firstname = newProfile.firstName();
        if (firstname != null) {
            user.setFirstName(firstname);
        }

        String lastname = newProfile.lastName();
        if (lastname != null) {
            user.setLastName(lastname);
        }

        String city = newProfile.city();
        if (city != null) {
            user.setCity(city);
        }

        String phoneNumber = newProfile.phoneNumber();
        if (phoneNumber != null) {
            user.setPhoneNumber(phoneNumber);
        }
    }

}
