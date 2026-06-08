package com.challengeteam.shop.service.user.credentials;

import com.challengeteam.shop.dto.user.request.ChangeEmailRequestDto;
import com.challengeteam.shop.dto.user.request.ChangePasswordRequestDto;
import com.challengeteam.shop.entity.user.User;
import com.challengeteam.shop.exceptionHandling.exception.EmailAlreadyExistsException;
import com.challengeteam.shop.exceptionHandling.exception.user.inputData.InvalidInputUserDataException;
import com.challengeteam.shop.persistence.repository.UserRepository;
import com.challengeteam.shop.utility.AuthenticationUserExtractorHelper;
import com.challengeteam.shop.utility.security.jwt.RevokeTokenHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserCredentialsServiceImplTest {

    private static final String ACCESS_TOKEN = "access.token";
    private static final String OLD_PASSWORD = "Password123!";
    private static final String OLD_PASSWORD_HASH = "encoded-old";
    private static final String NEW_PASSWORD = "NewPassword123!";
    private static final String NEW_PASSWORD_HASH = "encoded-new";
    private static final String OLD_EMAIL = "user@example.com";
    private static final String NEW_EMAIL = "new.user@example.com";

    @Mock
    private UserRepository userRepository;
    @Mock
    private AuthenticationUserExtractorHelper authenticationUserExtractorHelper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private RevokeTokenHelper revokeTokenHelper;
    @Mock
    private Authentication authentication;

    private UserCredentialsServiceImpl service;
    private User user;

    @BeforeEach
    void setUp() {
        service = new UserCredentialsServiceImpl(
                userRepository,
                authenticationUserExtractorHelper,
                passwordEncoder,
                revokeTokenHelper
        );
        user = User.builder()
                .id(1L)
                .email(OLD_EMAIL)
                .password(OLD_PASSWORD_HASH)
                .tokenVersion(0L)
                .build();

        when(authenticationUserExtractorHelper.extractUserFromSecurityContextHolder(authentication))
                .thenReturn(Optional.of(user));
        org.mockito.Mockito.lenient()
                .when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void changePassword_whenRequestIsValid_updatesPasswordAndInvalidatesTokens() {
        ChangePasswordRequestDto request = new ChangePasswordRequestDto(
                OLD_PASSWORD,
                NEW_PASSWORD,
                NEW_PASSWORD
        );
        when(passwordEncoder.matches(OLD_PASSWORD, OLD_PASSWORD_HASH)).thenReturn(true);
        when(passwordEncoder.matches(NEW_PASSWORD, OLD_PASSWORD_HASH)).thenReturn(false);
        when(passwordEncoder.encode(NEW_PASSWORD)).thenReturn(NEW_PASSWORD_HASH);

        service.changePassword(request, authentication, ACCESS_TOKEN);

        assertThat(user.getPassword()).isEqualTo(NEW_PASSWORD_HASH);
        assertThat(user.getTokenVersion()).isEqualTo(1L);
        verify(userRepository).save(user);
        verify(revokeTokenHelper).revokeToken(ACCESS_TOKEN);
    }

    @Test
    void changePassword_whenCurrentPasswordIsWrong_throwsBadRequest() {
        ChangePasswordRequestDto request = new ChangePasswordRequestDto(
                "WrongPassword123!",
                NEW_PASSWORD,
                NEW_PASSWORD
        );
        when(passwordEncoder.matches("WrongPassword123!", OLD_PASSWORD_HASH)).thenReturn(false);

        assertThatThrownBy(() -> service.changePassword(request, authentication, ACCESS_TOKEN))
                .isInstanceOf(InvalidInputUserDataException.class)
                .hasMessage("Current password is incorrect");

        verify(userRepository, never()).save(any());
        verify(revokeTokenHelper, never()).revokeToken(any());
    }

    @Test
    void changePassword_whenConfirmationDoesNotMatch_throwsBadRequest() {
        ChangePasswordRequestDto request = new ChangePasswordRequestDto(
                OLD_PASSWORD,
                NEW_PASSWORD,
                "DifferentPassword123!"
        );
        when(passwordEncoder.matches(OLD_PASSWORD, OLD_PASSWORD_HASH)).thenReturn(true);

        assertThatThrownBy(() -> service.changePassword(request, authentication, ACCESS_TOKEN))
                .isInstanceOf(InvalidInputUserDataException.class)
                .hasMessage("New password and confirmation do not match");
    }

    @Test
    void changePassword_whenNewPasswordMatchesCurrent_throwsBadRequest() {
        ChangePasswordRequestDto request = new ChangePasswordRequestDto(
                OLD_PASSWORD,
                OLD_PASSWORD,
                OLD_PASSWORD
        );
        when(passwordEncoder.matches(OLD_PASSWORD, OLD_PASSWORD_HASH)).thenReturn(true);

        assertThatThrownBy(() -> service.changePassword(request, authentication, ACCESS_TOKEN))
                .isInstanceOf(InvalidInputUserDataException.class)
                .hasMessage("New password must differ from current password");
    }

    @Test
    void changeEmail_whenRequestIsValid_updatesEmailAndInvalidatesTokens() {
        ChangeEmailRequestDto request = new ChangeEmailRequestDto(NEW_EMAIL, OLD_PASSWORD);
        when(passwordEncoder.matches(OLD_PASSWORD, OLD_PASSWORD_HASH)).thenReturn(true);
        when(userRepository.existsByEmail(NEW_EMAIL)).thenReturn(false);

        service.changeEmail(request, authentication, ACCESS_TOKEN);

        assertThat(user.getEmail()).isEqualTo(NEW_EMAIL);
        assertThat(user.getTokenVersion()).isEqualTo(1L);
        verify(userRepository).save(user);
        verify(revokeTokenHelper).revokeToken(ACCESS_TOKEN);
    }

    @Test
    void changeEmail_whenEmailIsSameAsCurrent_throwsBadRequest() {
        ChangeEmailRequestDto request = new ChangeEmailRequestDto(OLD_EMAIL, OLD_PASSWORD);
        when(passwordEncoder.matches(OLD_PASSWORD, OLD_PASSWORD_HASH)).thenReturn(true);

        assertThatThrownBy(() -> service.changeEmail(request, authentication, ACCESS_TOKEN))
                .isInstanceOf(InvalidInputUserDataException.class)
                .hasMessage("New email must differ from current email");
    }

    @Test
    void changeEmail_whenEmailAlreadyExists_throwsConflict() {
        ChangeEmailRequestDto request = new ChangeEmailRequestDto(NEW_EMAIL, OLD_PASSWORD);
        when(passwordEncoder.matches(OLD_PASSWORD, OLD_PASSWORD_HASH)).thenReturn(true);
        when(userRepository.existsByEmail(NEW_EMAIL)).thenReturn(true);

        assertThatThrownBy(() -> service.changeEmail(request, authentication, ACCESS_TOKEN))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessage("Email is already in use");
    }
}
