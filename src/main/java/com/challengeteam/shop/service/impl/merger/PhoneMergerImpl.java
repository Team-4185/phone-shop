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

        String newName = newPhone.name();
        if (newName != null) {
            phone.setName(newName.trim());
        }

        String newDescription = newPhone.description();
        if (newDescription != null) {
            phone.setDescription(newDescription);
        }

        BigDecimal newPrice = newPhone.price();
        if (newPrice != null) {
            phone.setPrice(newPrice);
        }

        String newBrand = newPhone.brand();
        if (newBrand != null) {
            phone.setBrand(newBrand.trim());
        }

        Integer newReleaseYear = newPhone.releaseYear();
        if (newReleaseYear != null) {
            phone.setReleaseYear(newReleaseYear);
        }

        String cpu = newPhone.cpu();
        if (cpu != null) {
            phone.getPhoneCharacteristics().setCpu(cpu.trim());
        }

        Integer coresNumber = newPhone.coresNumber();
        if (coresNumber != null) {
            phone.getPhoneCharacteristics().setCoresNumber(coresNumber);
        }

        String screenSize = newPhone.screenSize();
        if (screenSize != null) {
            phone.getPhoneCharacteristics().setScreenSize(screenSize.trim());
        }

        String frontCamera = newPhone.frontCamera();
        if (frontCamera != null) {
            phone.getPhoneCharacteristics().setFrontCamera(frontCamera.trim());
        }

        String mainCamera = newPhone.mainCamera();
        if (mainCamera != null) {
            phone.getPhoneCharacteristics().setMainCamera(mainCamera.trim());
        }

        String batteryCapacity = newPhone.batteryCapacity();
        if (batteryCapacity != null) {
            phone.getPhoneCharacteristics().setBatteryCapacity(batteryCapacity.trim());
        }
    }

}
