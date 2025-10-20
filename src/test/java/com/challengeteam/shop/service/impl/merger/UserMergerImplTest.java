package com.challengeteam.shop.service.impl.merger;

import com.challengeteam.shop.dto.user.UpdateProfileDto;
import com.challengeteam.shop.entity.user.User;
import com.challengeteam.shop.testData.user.UserTestData;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserMergerImplTest {
    private final UserMergerImpl userMerger = new UserMergerImpl();


    @Nested
    class MergeProfileTest {

        @Test
        void whenCalled_thenUpdateOnlyPresentFields() throws Exception {
            // given
            User user = UserTestData.getJeremy();
            User expected = UserTestData.getJeremy();
            user.setPassword(null);

            var dto = new UpdateProfileDto(
                    null,
                    "terminator 2",
                    "Atlantic",
                    null
            );

            // mockito
            // ..

            // when
            userMerger.mergeProfile(user, dto);

            // then
            assertNull(user.getPassword());
            assertEquals(expected.getId(), user.getId());
            assertEquals(expected.getEmail(), user.getEmail());
            assertEquals(dto.newLastname(), user.getLastName());
            assertEquals(dto.newCity(), user.getCity());
            assertNotEquals(dto.newFirstname(), user.getFirstName());
            assertNotEquals(dto.newPhoneNumber(), user.getPhoneNumber());
        }

        @Test
        void whenParameterUserIsNull_thenThrowException() throws Exception {
            // given
            User user = null;
            var dto = new UpdateProfileDto(
                    null,
                    "terminator 2",
                    "Atlantic",
                    null
            );

            // mockito
            // ..

            // when + then
            assertThrows(NullPointerException.class, () -> userMerger.mergeProfile(user, dto));
        }

        @Test
        void whenParameterUpdateProfileDtoIsNull_thenThrowException() throws Exception {
            // given
            User user = UserTestData.getJeremy();
            UpdateProfileDto dto = null;

            // mockito
            // ..

            // when + then
            assertThrows(NullPointerException.class, () -> userMerger.mergeProfile(user, dto));
        }

    }

}