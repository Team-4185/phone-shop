package com.challengeteam.shop.service.impl.merger;

import com.challengeteam.shop.dto.phone.PhoneUpdateRequestDto;
import com.challengeteam.shop.entity.phone.Phone;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static com.challengeteam.shop.service.impl.merger.PhoneMergerImplTest.TestResources.*;
import static org.junit.jupiter.api.Assertions.*;

class PhoneMergerImplTest {
    private final PhoneMergerImpl phoneMerger = new PhoneMergerImpl();

    @Nested
    class MergePhoneTest {

        @Test
        void whenCalled_thenUpdateOnlyPresentFields() {
            // given
            Phone phone = getIphone();
            Phone expected = getIphone();

            var dto = new PhoneUpdateRequestDto(
                    "new iphone",
                    "new description",
                    BigDecimal.valueOf(1999.99),
                    null,
                    null
            );

            // mockito
            // ..

            // when
            phoneMerger.mergePhone(phone, dto);

            // then
            assertEquals(expected.getId(), phone.getId());
            assertEquals(dto.name(), phone.getName());
            assertEquals(dto.description(), phone.getDescription());
            assertEquals(dto.price(), phone.getPrice());
            assertNotEquals(dto.brand(), phone.getBrand());
            assertNotEquals(dto.releaseYear(), phone.getReleaseYear());
        }

        @Test
        void whenFieldsHasWhitespaces_thenTrimAndMerge() {
            // given
            Phone phone = getIphone();
            var dto = new PhoneUpdateRequestDto(
                    "  " + SAMSUNG_NAME + "  ",
                    "  " + SAMSUNG_DESCRIPTION + "  ",
                    SAMSUNG_PRICE,
                    "  " + SAMSUNG_BRAND + "  ",
                    SAMSUNG_RELEASE_YEAR
            );

            // when
            phoneMerger.mergePhone(phone, dto);

            // then
            assertEquals(SAMSUNG_NAME, phone.getName());
            assertEquals("  " + SAMSUNG_DESCRIPTION + "  ", phone.getDescription());
            assertEquals(SAMSUNG_PRICE, phone.getPrice());
            assertEquals(SAMSUNG_BRAND, phone.getBrand());
            assertEquals(SAMSUNG_RELEASE_YEAR, phone.getReleaseYear());
        }

        @Test
        void whenParameterPhoneIsNull_thenThrowException() {
            // given
            var dto = new PhoneUpdateRequestDto(
                    "new iphone",
                    "new description",
                    BigDecimal.valueOf(1999.99),
                    null,
                    null
            );

            // mockito
            // ..

            // when + then
            assertThrows(NullPointerException.class, () -> phoneMerger.mergePhone(null, dto));
        }

        @Test
        void whenParameterPhoneUpdateRequestDtoIsNull_thenThrowException() {
            // given
            Phone phone = getIphone();
            PhoneUpdateRequestDto dto = null;

            // mockito
            // ..

            // when + then
            assertThrows(NullPointerException.class, () -> phoneMerger.mergePhone(phone, dto));
        }
    }

    static class TestResources {
        public static final Long IPHONE_ID = 1L;
        public static final String IPHONE_NAME = "iPhone 15";
        public static final String IPHONE_DESCRIPTION = "Flagship Apple smartphone";
        public static final String IPHONE_BRAND = "Apple";
        public static final BigDecimal IPHONE_PRICE = BigDecimal.valueOf(1499.99);
        public static final int IPHONE_RELEASE_YEAR = 2023;

        public static final Long SAMSUNG_ID = 2L;
        public static final String SAMSUNG_NAME = "Samsung Galaxy S24";
        public static final String SAMSUNG_DESCRIPTION = "Premium Android smartphone";
        public static final String SAMSUNG_BRAND = "Samsung";
        public static final BigDecimal SAMSUNG_PRICE = BigDecimal.valueOf(1399.00);
        public static final int SAMSUNG_RELEASE_YEAR = 2024;

        public static final Long XIAOMI_ID = 3L;
        public static final String XIAOMI_NAME = "Xiaomi 14 Pro";
        public static final String XIAOMI_DESCRIPTION = "Affordable flagship smartphone";
        public static final String XIAOMI_BRAND = "Xiaomi";
        public static final BigDecimal XIAOMI_PRICE = BigDecimal.valueOf(899.50);
        public static final int XIAOMI_RELEASE_YEAR = 2023;

        public static final Long GOOGLE_ID = 4L;
        public static final String GOOGLE_NAME = "Google Pixel 9";
        public static final String GOOGLE_DESCRIPTION = "Pure Android experience";
        public static final String GOOGLE_BRAND = "Google";
        public static final BigDecimal GOOGLE_PRICE = BigDecimal.valueOf(1199.00);
        public static final int GOOGLE_RELEASE_YEAR = 2024;

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

}
