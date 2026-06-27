package com.challengeteam.shop.service.user.sensetiveData;

import com.challengeteam.shop.constants.security.jwt.JwtTokenNameConstants;
import com.challengeteam.shop.dto.security.jwt.JwtResponseDto;
import com.challengeteam.shop.dto.user.request.sensetiveData.UpdateUserSensitiveDataDto;
import com.challengeteam.shop.entity.user.User;
import com.challengeteam.shop.exceptionHandling.exception.user.InvalidUserCredentialsException;
import com.challengeteam.shop.exceptionHandling.exception.user.inputData.InvalidInputUserDataException;
import com.challengeteam.shop.persistence.repository.UserRepository;
import com.challengeteam.shop.service.security.auth.jwt.JwtService;
import com.challengeteam.shop.utility.AuthenticationUserExtractorHelper;
import com.challengeteam.shop.utility.security.jwt.RevokeTokenHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserSensitiveDataUpdaterImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private AuthenticationUserExtractorHelper authenticationUserExtractorHelper;
    @Mock
    private JwtService jwtService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private RevokeTokenHelper revokeTokenHelper;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserSensitiveDataUpdaterImpl updater;

    private static final String OLD_PASSWORD_RAW = "OldPass1!";
    private static final String OLD_PASSWORD_HASH = "$2a$old_hash";
    private static final String NEW_PASSWORD_RAW = "NewPass2@";
    private static final String NEW_PASSWORD_HASH = "$2a$new_hash";
    private static final String OLD_EMAIL = "user@example.com";
    private static final String NEW_EMAIL = "new@example.com";
    private static final String ACCESS_TOKEN = "access.token.value";
    private static final String REFRESH_TOKEN = "refresh.token.value";

    private User user;
    private Map<String, String> tokens;
    private JwtResponseDto jwtResponse;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail(OLD_EMAIL);
        user.setPassword(OLD_PASSWORD_HASH);

        tokens = Map.of(
                JwtTokenNameConstants.ACCESS_TOKEN_TYPE, ACCESS_TOKEN,
                JwtTokenNameConstants.REFRESH_TOKEN_TYPE, REFRESH_TOKEN
        );

        jwtResponse = new JwtResponseDto(1L, OLD_EMAIL, "new.access", "new.refresh", false);

        when(authenticationUserExtractorHelper.extractUserFromSecurityContextHolder(authentication))
                .thenReturn(Optional.of(user));
        lenient().when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(jwtService.createAccessToken(any(User.class))).thenReturn(jwtResponse.accessToken());
        lenient().when(jwtService.createRefreshToken(any(User.class), eq(false))).thenReturn(jwtResponse.refreshToken());
    }

    @Nested
    class UpdateUserPasswordTest {
        @Test
        @DisplayName("change password — success: updates hash and revokes old tokens")
        void changePassword_success() {
            var dto = new UpdateUserSensitiveDataDto(NEW_PASSWORD_RAW, NEW_PASSWORD_RAW, OLD_PASSWORD_RAW, null);

            when(passwordEncoder.matches(OLD_PASSWORD_RAW, OLD_PASSWORD_HASH)).thenReturn(true);
            when(passwordEncoder.matches(NEW_PASSWORD_RAW, OLD_PASSWORD_HASH)).thenReturn(false);
            when(passwordEncoder.encode(NEW_PASSWORD_RAW)).thenReturn(NEW_PASSWORD_HASH);

            JwtResponseDto result = updater.update(dto, authentication, tokens);

            assertThat(result).isEqualTo(jwtResponse);
            assertThat(user.getPassword()).isEqualTo(NEW_PASSWORD_HASH);
            assertThat(user.getTokenVersion()).isEqualTo(1L);
            verify(revokeTokenHelper).revokeToken(ACCESS_TOKEN);
            verify(revokeTokenHelper).revokeToken(REFRESH_TOKEN);
            verify(jwtService).createAccessToken(user);
            verify(jwtService).createRefreshToken(user, false);
        }

        @Test
        @DisplayName("change password — fail: newPassword and confirmPassword do not match")
        void changePassword_passwordMismatch_throws() {
            var dto = new UpdateUserSensitiveDataDto(NEW_PASSWORD_RAW, "WrongConfirm1!", OLD_PASSWORD_RAW, null);

            assertThatThrownBy(() -> updater.update(dto, authentication, tokens))
                    .isInstanceOf(InvalidInputUserDataException.class)
                    .hasMessageContaining("do not match");

            verify(userRepository, never()).save(any());
            verify(revokeTokenHelper, never()).revokeToken(any());
        }

        @Test
        @DisplayName("change password — fail: oldPassword is wrong")
        void changePassword_wrongOldPassword_throws() {
            var dto = new UpdateUserSensitiveDataDto(NEW_PASSWORD_RAW, NEW_PASSWORD_RAW, "WrongOld1!", null);

            when(passwordEncoder.matches("WrongOld1!", OLD_PASSWORD_HASH)).thenReturn(false);

            assertThatThrownBy(() -> updater.update(dto, authentication, tokens))
                    .isInstanceOf(InvalidInputUserDataException.class)
                    .hasMessageContaining("Wrong password");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("change password — fail: newPassword is same as old")
        void changePassword_sameAsOld_throws() {
            var dto = new UpdateUserSensitiveDataDto(OLD_PASSWORD_RAW, OLD_PASSWORD_RAW, OLD_PASSWORD_RAW, null);

            when(passwordEncoder.matches(OLD_PASSWORD_RAW, OLD_PASSWORD_HASH)).thenReturn(true);

            assertThatThrownBy(() -> updater.update(dto, authentication, tokens))
                    .isInstanceOf(InvalidInputUserDataException.class)
                    .hasMessageContaining("same as the old one");

            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    class UpdateUserEmailTest {
        @Test
        @DisplayName("change email — success: updates email and revokes old tokens")
        void changeEmail_success() {
            var dto = new UpdateUserSensitiveDataDto(null, null, null, NEW_EMAIL);

            JwtResponseDto result = updater.update(dto, authentication, tokens);

            assertThat(result.email()).isEqualTo(NEW_EMAIL);
            assertThat(result.accessToken()).isEqualTo(jwtResponse.accessToken());
            assertThat(result.refreshToken()).isEqualTo(jwtResponse.refreshToken());
            assertThat(user.getEmail()).isEqualTo(NEW_EMAIL);
            assertThat(user.getTokenVersion()).isEqualTo(1L);
            verify(revokeTokenHelper).revokeToken(ACCESS_TOKEN);
            verify(revokeTokenHelper).revokeToken(REFRESH_TOKEN);
        }

        @Test
        @DisplayName("change email — fail: new email is the same as current")
        void changeEmail_sameEmail_throws() {
            var dto = new UpdateUserSensitiveDataDto(null, null, null, OLD_EMAIL);

            assertThatThrownBy(() -> updater.update(dto, authentication, tokens))
                    .isInstanceOf(InvalidInputUserDataException.class)
                    .hasMessageContaining("same as the old one");

            verify(userRepository, never()).save(any());
            verify(revokeTokenHelper, never()).revokeToken(any());
        }
    }

    @Nested
    class UpdateUserEmailAndPasswordTest {

        @Test
        @DisplayName("change both email and password — success")
        void changeBoth_success() {
            var dto = new UpdateUserSensitiveDataDto(NEW_PASSWORD_RAW, NEW_PASSWORD_RAW, OLD_PASSWORD_RAW, NEW_EMAIL);

            when(passwordEncoder.matches(OLD_PASSWORD_RAW, OLD_PASSWORD_HASH)).thenReturn(true);
            when(passwordEncoder.matches(NEW_PASSWORD_RAW, OLD_PASSWORD_HASH)).thenReturn(false);
            when(passwordEncoder.encode(NEW_PASSWORD_RAW)).thenReturn(NEW_PASSWORD_HASH);

            updater.update(dto, authentication, tokens);

            assertThat(user.getPassword()).isEqualTo(NEW_PASSWORD_HASH);
            assertThat(user.getEmail()).isEqualTo(NEW_EMAIL);
            assertThat(user.getTokenVersion()).isEqualTo(1L);
            verify(revokeTokenHelper, times(2)).revokeToken(any());
        }


        @Test
        @DisplayName("no new data provided — throws")
        void noNewData_throws() {
            var dto = new UpdateUserSensitiveDataDto(null, null, null, null);

            assertThatThrownBy(() -> updater.update(dto, authentication, tokens))
                    .isInstanceOf(InvalidInputUserDataException.class)
                    .hasMessageContaining("did not provide any new data");
        }


        @Test
        @DisplayName("user not found in security context — throws")
        void userNotFound_throws() {
            when(authenticationUserExtractorHelper.extractUserFromSecurityContextHolder(authentication))
                    .thenReturn(Optional.empty());
            when(authentication.getName()).thenReturn("ghost@example.com");

            var dto = new UpdateUserSensitiveDataDto(NEW_PASSWORD_RAW, NEW_PASSWORD_RAW, OLD_PASSWORD_RAW, null);

            assertThatThrownBy(() -> updater.update(dto, authentication, tokens))
                    .isInstanceOf(InvalidUserCredentialsException.class);

            verify(userRepository, never()).save(any());
        }
    }
}
