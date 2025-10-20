package com.challengeteam.shop.testData.phone;

import com.challengeteam.shop.entity.phone.Phone;

import java.math.BigDecimal;

public interface PhoneTestData {
    Long IPHONE_ID = 1L;
    String IPHONE_NAME = "iPhone 15";
    String IPHONE_DESCRIPTION = "Flagship Apple smartphone";
    String IPHONE_BRAND = "Apple";
    BigDecimal IPHONE_PRICE = BigDecimal.valueOf(1499.99);
    int IPHONE_RELEASE_YEAR = 2023;

    Long SAMSUNG_ID = 2L;
    String SAMSUNG_NAME = "Samsung Galaxy S24";
    String SAMSUNG_DESCRIPTION = "Premium Android smartphone";
    String SAMSUNG_BRAND = "Samsung";
    BigDecimal SAMSUNG_PRICE = BigDecimal.valueOf(1399.00);
    int SAMSUNG_RELEASE_YEAR = 2024;

    Long XIAOMI_ID = 3L;
    String XIAOMI_NAME = "Xiaomi 14 Pro";
    String XIAOMI_DESCRIPTION = "Affordable flagship smartphone";
    String XIAOMI_BRAND = "Xiaomi";
    BigDecimal XIAOMI_PRICE = BigDecimal.valueOf(899.50);
    int XIAOMI_RELEASE_YEAR = 2023;

    Long GOOGLE_ID = 4L;
    String GOOGLE_NAME = "Google Pixel 9";
    String GOOGLE_DESCRIPTION = "Pure Android experience";
    String GOOGLE_BRAND = "Google";
    BigDecimal GOOGLE_PRICE = BigDecimal.valueOf(1199.00);
    int GOOGLE_RELEASE_YEAR = 2024;

    static Phone getIphone() {
        Phone phone = Phone.builder()
                .name(IPHONE_NAME)
                .description(IPHONE_DESCRIPTION)
                .brand(IPHONE_BRAND)
                .price(IPHONE_PRICE)
                .releaseYear(IPHONE_RELEASE_YEAR)
                .build();
        phone.setId(IPHONE_ID);
        return phone;
    }

    static Phone getSamsung() {
        Phone phone = Phone.builder()
                .name(SAMSUNG_NAME)
                .description(SAMSUNG_DESCRIPTION)
                .brand(SAMSUNG_BRAND)
                .price(SAMSUNG_PRICE)
                .releaseYear(SAMSUNG_RELEASE_YEAR)
                .build();
        phone.setId(SAMSUNG_ID);
        return phone;
    }

    static Phone getXiaomi() {
        Phone phone = Phone.builder()
                .name(XIAOMI_NAME)
                .description(XIAOMI_DESCRIPTION)
                .brand(XIAOMI_BRAND)
                .price(XIAOMI_PRICE)
                .releaseYear(XIAOMI_RELEASE_YEAR)
                .build();
        phone.setId(XIAOMI_ID);
        return phone;
    }

    static Phone getGooglePixel() {
        Phone phone = Phone.builder()
                .name(GOOGLE_NAME)
                .description(GOOGLE_DESCRIPTION)
                .brand(GOOGLE_BRAND)
                .price(GOOGLE_PRICE)
                .releaseYear(GOOGLE_RELEASE_YEAR)
                .build();
        phone.setId(GOOGLE_ID);
        return phone;
    }
}
