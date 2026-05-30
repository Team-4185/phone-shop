package com.challengeteam.shop.service.impl.merger;

import com.challengeteam.shop.dto.phone.request.PhoneUpdateRequestDto;
import com.challengeteam.shop.entity.phone.Phone;
import com.challengeteam.shop.entity.phone.PhoneColor;
import com.challengeteam.shop.entity.phone.StorageCapacity;
import com.challengeteam.shop.utility.ProductStatusResolver;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Set;

@Component
public class PhoneMergerImpl implements PhoneMerger {

    @Override
    public void mergePhone(Phone phone, PhoneUpdateRequestDto newPhone) {
        Objects.requireNonNull(phone, "phone");
        Objects.requireNonNull(newPhone, "newPhone");

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

        String newSku = newPhone.sku();
        if (newSku != null) {
            phone.setSku(newSku.trim().toUpperCase());
        }

        Integer newStock = newPhone.stock();
        if (newStock != null) {
            phone.setStock(newStock);
            phone.setStatus(ProductStatusResolver.resolve(newStock));
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

        Set<PhoneColor> newColors = newPhone.colors();
        if (newColors != null && !newColors.isEmpty()) {
            if (!phone.getPhoneCharacteristics().getPhoneColors().equals(newColors)) {
                phone.getPhoneCharacteristics().getPhoneColors().clear();
                phone.getPhoneCharacteristics().getPhoneColors().addAll(newColors);
            }
        }

        Set<StorageCapacity> newStorageCapacities = newPhone.storageCapacities();
        if (newStorageCapacities != null && !newStorageCapacities.isEmpty()) {
            if (!phone.getPhoneCharacteristics().getStorageCapacities().equals(newStorageCapacities)) {
                phone.getPhoneCharacteristics().getStorageCapacities().clear(); // ← исправлено
                phone.getPhoneCharacteristics().getStorageCapacities().addAll(newStorageCapacities);
            }
        }
    }
}