package com.challengeteam.shop.service.impl;

import com.challengeteam.shop.entity.user.User;
import com.challengeteam.shop.exceptionHandling.exception.InvalidTokenException;
import com.challengeteam.shop.exceptionHandling.exception.ResourceNotFoundException;
import com.challengeteam.shop.persistence.repository.UserRepository;
import com.challengeteam.shop.service.EmailService;
import com.challengeteam.shop.service.JwtService;
import com.challengeteam.shop.testData.user.UserTestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static com.challengeteam.shop.service.impl.PasswordResetServiceImplTest.TestResources.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PasswordResetServiceImpl passwordResetService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(passwordResetService, "frontendUrl", "https://test.com");
    }

    @Nested
    class SendResetLinkTest {

        @Test
        void whenUserExists_thenSendResetLink() {
            // given
            User user = buildUser();
            when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
            when(jwtService.createResetToken(user)).thenReturn(RESET_TOKEN);

            // when
            passwordResetService.sendResetLink(USER_EMAIL);

            // then
            verify(emailService).sendResetLink(USER_EMAIL, "https://test.com/reset-password?token=" + RESET_TOKEN);
        }

        @Test
        void whenUserNotExists_thenDoNothing() {
            // given
            when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.empty());

            // when
            passwordResetService.sendResetLink(USER_EMAIL);

            // then
            verifyNoInteractions(jwtService, emailService);
        }
    }

    @Nested
    class ResetPasswordTest {

        @Test
        void whenTokenAndUserAreValid_thenUpdatePassword() {
            // given
            User user = buildUser();
            when(jwtService.getEmailFromResetToken(RESET_TOKEN)).thenReturn(USER_EMAIL);
            when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
            when(passwordEncoder.encode(NEW_PASSWORD)).thenReturn(ENCODED_PASSWORD);

            // when
            passwordResetService.resetPassword(RESET_TOKEN, NEW_PASSWORD);

            // then
            assertThat(user.getPassword()).isEqualTo(ENCODED_PASSWORD);
            verify(userRepository).save(user);
        }

        @Test
        void whenTokenIsInvalid_thenThrowException() {
            // given
            when(jwtService.getEmailFromResetToken(INVALID_TOKEN))
                    .thenThrow(new InvalidTokenException("Invalid token"));

            // when + then
            assertThatThrownBy(() -> passwordResetService.resetPassword(INVALID_TOKEN, NEW_PASSWORD))
                    .isInstanceOf(InvalidTokenException.class);

            verifyNoInteractions(userRepository, emailService);
        }

        @Test
        void whenUserNotFound_thenThrowException() {
            // given
            when(jwtService.getEmailFromResetToken(RESET_TOKEN)).thenReturn(USER_EMAIL);
            when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.empty());

            // when + then
            assertThatThrownBy(() -> passwordResetService.resetPassword(RESET_TOKEN, NEW_PASSWORD))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(passwordEncoder, never()).encode(any());
            verify(userRepository, never()).save(any());
        }
    }

    static class TestResources {

        static final String USER_EMAIL = UserTestData.getJeremy().getEmail();
        static final String RESET_TOKEN = "valid.reset.token";
        static final String INVALID_TOKEN = "invalid.token";
        static final String NEW_PASSWORD = "newPassword123";
        static final String ENCODED_PASSWORD = "$2a$10$encodedPassword";

        static User buildUser() {
            return UserTestData.getJeremy();
        }
    }
}
