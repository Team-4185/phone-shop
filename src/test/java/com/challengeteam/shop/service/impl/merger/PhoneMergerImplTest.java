package com.challengeteam.shop.service.impl.merger;

import com.challengeteam.shop.dto.phone.PhoneUpdateRequestDto;
import com.challengeteam.shop.entity.phone.Phone;
import com.challengeteam.shop.entity.phone.PhoneCharacteristics;
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
                    null,
                    null,
                    null,
                    null,
                    null,
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
            assertNotEquals(dto.cpu(), phone.getPhoneCharacteristics().getCpu());
            assertNotEquals(dto.coresNumber(), phone.getPhoneCharacteristics().getCoresNumber());
            assertNotEquals(dto.screenSize(), phone.getPhoneCharacteristics().getScreenSize());
            assertNotEquals(dto.frontCamera(), phone.getPhoneCharacteristics().getFrontCamera());
            assertNotEquals(dto.mainCamera(), phone.getPhoneCharacteristics().getMainCamera());
            assertNotEquals(dto.batteryCapacity(), phone.getPhoneCharacteristics().getBatteryCapacity());
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
                    SAMSUNG_RELEASE_YEAR,
                    "  " + SAMSUNG_CPU + "  ",
                    SAMSUNG_CORES_NUMBER,
                    "  " + SAMSUNG_SCREEN_SIZE + "  ",
                    "  " + SAMSUNG_FRONT_CAMERA + "  ",
                    "  " + SAMSUNG_MAIN_CAMERA + "  ",
                    "  " + SAMSUNG_BATTERY_CAPACITY + "  "
            );

            // when
            phoneMerger.mergePhone(phone, dto);

            // then
            assertEquals(SAMSUNG_NAME, phone.getName());
            assertEquals("  " + SAMSUNG_DESCRIPTION + "  ", phone.getDescription());
            assertEquals(SAMSUNG_PRICE, phone.getPrice());
            assertEquals(SAMSUNG_BRAND, phone.getBrand());
            assertEquals(SAMSUNG_RELEASE_YEAR, phone.getReleaseYear());
            assertEquals(SAMSUNG_CPU, phone.getPhoneCharacteristics().getCpu());
            assertEquals(SAMSUNG_CORES_NUMBER, phone.getPhoneCharacteristics().getCoresNumber());
            assertEquals(SAMSUNG_SCREEN_SIZE, phone.getPhoneCharacteristics().getScreenSize());
            assertEquals(SAMSUNG_FRONT_CAMERA, phone.getPhoneCharacteristics().getFrontCamera());
            assertEquals(SAMSUNG_MAIN_CAMERA, phone.getPhoneCharacteristics().getMainCamera());
            assertEquals(SAMSUNG_BATTERY_CAPACITY, phone.getPhoneCharacteristics().getBatteryCapacity());
        }

        @Test
        void whenParameterPhoneIsNull_thenThrowException() {
            // given
            var dto = new PhoneUpdateRequestDto(
                    "new iphone",
                    "new description",
                    BigDecimal.valueOf(1999.99),
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
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
        public static final String IPHONE_CPU = "Apple A16 Bionic";
        public static final Integer IPHONE_CORES_NUMBER = 6;
        public static final String IPHONE_SCREEN_SIZE = "6.1\"";
        public static final String IPHONE_FRONT_CAMERA = "12 MP";
        public static final String IPHONE_MAIN_CAMERA = "48 MP";
        public static final String IPHONE_BATTERY_CAPACITY = "3349 mAh";

        public static final Long SAMSUNG_ID = 2L;
        public static final String SAMSUNG_NAME = "Samsung Galaxy S24";
        public static final String SAMSUNG_DESCRIPTION = "Premium Android smartphone";
        public static final String SAMSUNG_BRAND = "Samsung";
        public static final BigDecimal SAMSUNG_PRICE = BigDecimal.valueOf(1399.00);
        public static final int SAMSUNG_RELEASE_YEAR = 2024;
        public static final String SAMSUNG_CPU = "Exynos 2400";
        public static final Integer SAMSUNG_CORES_NUMBER = 10;
        public static final String SAMSUNG_SCREEN_SIZE = "6.2\"";
        public static final String SAMSUNG_FRONT_CAMERA = "12 MP";
        public static final String SAMSUNG_MAIN_CAMERA = "50 MP";
        public static final String SAMSUNG_BATTERY_CAPACITY = "4000 mAh";

        public static final Long XIAOMI_ID = 3L;
        public static final String XIAOMI_NAME = "Xiaomi 14 Pro";
        public static final String XIAOMI_DESCRIPTION = "Affordable flagship smartphone";
        public static final String XIAOMI_BRAND = "Xiaomi";
        public static final BigDecimal XIAOMI_PRICE = BigDecimal.valueOf(899.50);
        public static final int XIAOMI_RELEASE_YEAR = 2023;
        public static final String XIAOMI_CPU = "Snapdragon 8 Gen 3";
        public static final Integer XIAOMI_CORES_NUMBER = 8;
        public static final String XIAOMI_SCREEN_SIZE = "6.73\"";
        public static final String XIAOMI_FRONT_CAMERA = "32 MP";
        public static final String XIAOMI_MAIN_CAMERA = "50 MP";
        public static final String XIAOMI_BATTERY_CAPACITY = "4880 mAh";

        public static final Long GOOGLE_ID = 4L;
        public static final String GOOGLE_NAME = "Google Pixel 9";
        public static final String GOOGLE_DESCRIPTION = "Pure Android experience";
        public static final String GOOGLE_BRAND = "Google";
        public static final BigDecimal GOOGLE_PRICE = BigDecimal.valueOf(1199.00);
        public static final int GOOGLE_RELEASE_YEAR = 2024;
        public static final String GOOGLE_CPU = "Google Tensor G4";
        public static final Integer GOOGLE_CORES_NUMBER = 8;
        public static final String GOOGLE_SCREEN_SIZE = "6.3\"";
        public static final String GOOGLE_FRONT_CAMERA = "11 MP";
        public static final String GOOGLE_MAIN_CAMERA = "50 MP";
        public static final String GOOGLE_BATTERY_CAPACITY = "4700 mAh";

        static Phone getIphone() {
            Phone phone = Phone.builder()
                    .name(IPHONE_NAME)
                    .description(IPHONE_DESCRIPTION)
                    .brand(IPHONE_BRAND)
                    .price(IPHONE_PRICE)
                    .releaseYear(IPHONE_RELEASE_YEAR)
                    .phoneCharacteristics(
                            PhoneCharacteristics.builder()
                                    .cpu(IPHONE_CPU)
                                    .coresNumber(IPHONE_CORES_NUMBER)
                                    .screenSize(IPHONE_SCREEN_SIZE)
                                    .frontCamera(IPHONE_FRONT_CAMERA)
                                    .mainCamera(IPHONE_MAIN_CAMERA)
                                    .batteryCapacity(IPHONE_BATTERY_CAPACITY)
                                    .build()
                    )
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
                    .phoneCharacteristics(
                            PhoneCharacteristics.builder()
                                    .cpu(SAMSUNG_CPU)
                                    .coresNumber(SAMSUNG_CORES_NUMBER)
                                    .screenSize(SAMSUNG_SCREEN_SIZE)
                                    .frontCamera(SAMSUNG_FRONT_CAMERA)
                                    .mainCamera(SAMSUNG_MAIN_CAMERA)
                                    .batteryCapacity(SAMSUNG_BATTERY_CAPACITY)
                                    .build()
                    )
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
                    .phoneCharacteristics(
                            PhoneCharacteristics.builder()
                                    .cpu(XIAOMI_CPU)
                                    .coresNumber(XIAOMI_CORES_NUMBER)
                                    .screenSize(XIAOMI_SCREEN_SIZE)
                                    .frontCamera(XIAOMI_FRONT_CAMERA)
                                    .mainCamera(XIAOMI_MAIN_CAMERA)
                                    .batteryCapacity(XIAOMI_BATTERY_CAPACITY)
                                    .build()
                    )
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
                    .phoneCharacteristics(
                            PhoneCharacteristics.builder()
                                    .cpu(GOOGLE_CPU)
                                    .coresNumber(GOOGLE_CORES_NUMBER)
                                    .screenSize(GOOGLE_SCREEN_SIZE)
                                    .frontCamera(GOOGLE_FRONT_CAMERA)
                                    .mainCamera(GOOGLE_MAIN_CAMERA)
                                    .batteryCapacity(GOOGLE_BATTERY_CAPACITY)
                                    .build()
                    )
                    .build();
            phone.setId(GOOGLE_ID);
            return phone;
        }
    }


}
