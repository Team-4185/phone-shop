package com.challengeteam.shop.service.impl.merger;

import com.challengeteam.shop.dto.phone.PhoneUpdateRequestDto;
import com.challengeteam.shop.entity.phone.Phone;
import com.challengeteam.shop.testData.phone.PhoneTestData;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PhoneMergerImplTest {
    private final PhoneMergerImpl phoneMerger = new PhoneMergerImpl();

    @Nested
    class MergePhoneTest {

        @Test
        void whenCalled_thenUpdateOnlyPresentFields() {
            // given
            Phone phone = PhoneTestData.getIphone();
            Phone expected = PhoneTestData.getIphone();

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
            assertEquals(dto.newName(), phone.getName());
            assertEquals(dto.newDescription(), phone.getDescription());
            assertEquals(dto.newPrice(), phone.getPrice());
            assertNotEquals(dto.newBrand(), phone.getBrand());
            assertNotEquals(dto.newReleaseYear(), phone.getReleaseYear());
        }

        @Test
        void whenParameterPhoneIsNull_thenThrowException() {
            // given
            Phone phone = null;
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
            assertThrows(NullPointerException.class, () -> phoneMerger.mergePhone(phone, dto));
        }

        @Test
        void whenParameterPhoneUpdateRequestDtoIsNull_thenThrowException() {
            // given
            Phone phone = PhoneTestData.getIphone();
            PhoneUpdateRequestDto dto = null;

            // mockito
            // ..

            // when + then
            assertThrows(NullPointerException.class, () -> phoneMerger.mergePhone(phone, dto));
        }
    }
}
