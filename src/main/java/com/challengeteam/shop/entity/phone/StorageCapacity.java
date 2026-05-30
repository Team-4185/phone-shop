package com.challengeteam.shop.entity.phone;

import lombok.Getter;

@Getter
public enum StorageCapacity {
    CAPACITY_64GB(64, "GB"),
    CAPACITY_128GB(128, "GB"),
    CAPACITY_256GB(256, "GB"),
    CAPACITY_512GB(512, "GB"),
    CAPACITY_1TB(1, "TB"),
    CAPACITY_2TB(2, "TB");

    private final int value;
    private final String unit;

    StorageCapacity(int value, String unit) {
        this.value = value;
        this.unit = unit;
    }

    @Override
    public String toString() {
        return value + unit;
    }
}