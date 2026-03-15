package com.challengeteam.shop.service.impl;

import com.challengeteam.shop.dto.jwt.JwtResponseDto;
import com.challengeteam.shop.dto.user.CreateUserDto;
import com.challengeteam.shop.dto.user.UserLoginRequestDto;
import com.challengeteam.shop.dto.user.UserRegisterRequestDto;
import com.challengeteam.shop.entity.user.User;
import com.challengeteam.shop.exceptionHandling.exception.EmailOrPasswordWrongException;
import com.challengeteam.shop.exceptionHandling.exception.InvalidAPIRequestException;
import com.challengeteam.shop.exceptionHandling.exception.InvalidTokenException;
import com.challengeteam.shop.exceptionHandling.exception.ResourceNotFoundException;
import com.challengeteam.shop.service.JwtService;
import com.challengeteam.shop.service.UserService;
import com.challengeteam.shop.testData.user.UserTestData;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class JwtAuthorizationServiceImplTest {

    @Mock
    private UserService userService;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private JwtAuthorizationServiceImpl jwtAuthorizationService;

    @Nested
    class RegisterTest {

        @Test
        void whenAllValid_thenRegisterNewUserAndReturnJwtResponseDto() throws Exception {
            // given
            User jeremy = TestResources.buildUser();
            var userRegisterRequestDto = new UserRegisterRequestDto(
                    jeremy.getEmail(),
                    jeremy.getPassword(),
                    jeremy.getPassword()
            );
            var createUserDto = new CreateUserDto(
                    jeremy.getEmail(),
                    jeremy.getPassword()
            );

            //  mockito
            Mockito.when(userService.createDefaultUser(createUserDto))
                    .thenReturn(jeremy.getId());
            Mockito.when(userService.getById(jeremy.getId()))
                    .thenReturn(Optional.of(jeremy));
            mockCreateJwtResponseMethod(jeremy);

            // when
            JwtResponseDto result = jwtAuthorizationService.register(userRegisterRequestDto);

            // then
            assertNotNull(result);
            assertEquals(jeremy.getId(), result.userId());
            assertEquals(jeremy.getEmail(), result.email());
            assertEquals(TestResources.ACCESS_TOKEN, result.accessToken());
            assertEquals(TestResources.REFRESH_TOKEN, result.refreshToken());
        }

        @Test
        void whenPasswordsDontMatch_thenThrowException() throws Exception {
            // given
            User jeremy = UserTestData.getJeremy();
            var userRegisterRequestDto = new UserRegisterRequestDto(
                    jeremy.getEmail(),
                    jeremy.getPassword(),
                    "not_the_same_password"
            );

            // mockito
            // ...

            // when + then
            assertThrows(InvalidAPIRequestException.class, () -> jwtAuthorizationService.register(userRegisterRequestDto));
        }

        @Test
        void whenParameterUserRegisterRequestDtoIsNull_thenThrowException() throws Exception {
            // given
            UserRegisterRequestDto userRegisterRequestDto = null;

            // mockito
            // ...

            // when + then
            assertThrows(NullPointerException.class, () -> jwtAuthorizationService.register(userRegisterRequestDto));
        }

    }

    @Nested
    class LoginTest {

        @Test
        void whenAllValid_thenAuthorizeInSessionAndReturnJwtResponseDto() throws Exception {
            // given
            User jeremy = UserTestData.getJeremy();
            var userLoginRequestDto = new UserLoginRequestDto(
                    jeremy.getEmail(),
                    jeremy.getPassword(),
                    false
            );

            // mockito
            Mockito.when(userService.getByEmail(jeremy.getEmail()))
                    .thenReturn(Optional.of(jeremy));
            mockCreateJwtResponseMethod(jeremy);

            // when
            JwtResponseDto result = jwtAuthorizationService.login(userLoginRequestDto);

            // then
            assertNotNull(result);
            assertEquals(jeremy.getId(), result.userId());
            assertEquals(jeremy.getEmail(), result.email());
            assertEquals(TestResources.ACCESS_TOKEN, result.accessToken());
            assertEquals(TestResources.REFRESH_TOKEN, result.refreshToken());
        }

        @Test
        void whenCredentialsAreWrong_thenThrowException() throws Exception {
            // given
            User jeremy = UserTestData.getJeremy();
            var userLoginRequestDto = new UserLoginRequestDto(
                    jeremy.getEmail(),
                    jeremy.getPassword(),
                    false
            );

            // mockito
            Mockito.when(authenticationManager.authenticate(Mockito.any(Authentication.class)))
                    .thenThrow(BadCredentialsException.class);


            // when + then
            assertThrows(EmailOrPasswordWrongException.class, () -> jwtAuthorizationService.login(userLoginRequestDto));
        }

        // no more tests for "if user is locked" or "if user is disabled",
        // because we don't have and are not planning such functionality

        @Test
        void whenParameterUserLoginRequestDtoIsNull_thenThrowException() throws Exception {
            // given
            UserLoginRequestDto userLoginRequestDto = null;

            // mockito
            // ...

            // when + then
            assertThrows(NullPointerException.class, () -> jwtAuthorizationService.login(userLoginRequestDto));
        }

    }

    @Nested
    class RefreshTest {

        @Test
        void whenRefreshTokenIsValid_thenReturnNewJwtResponseDto() throws Exception {
            // given
            User jeremy = UserTestData.getJeremy();
            var jwtResponseDto = new JwtResponseDto(
                    jeremy.getId(),
                    jeremy.getEmail(),
                    TestResources.ACCESS_TOKEN,
                    TestResources.REFRESH_TOKEN,
                    false

            );

            // mockito
            Mockito.when(jwtService.isRefreshToken(TestResources.REFRESH_TOKEN))
                    .thenReturn(true);
            Mockito.when(jwtService.isValid(TestResources.REFRESH_TOKEN))
                    .thenReturn(true);
            Mockito.when(jwtService.getEmailFromToken(TestResources.REFRESH_TOKEN))
                    .thenReturn(jeremy.getEmail());
            Mockito.when(userService.getByEmail(jeremy.getEmail()))
                    .thenReturn(Optional.of(jeremy));
            Mockito.when(jwtService.refreshTokens(TestResources.REFRESH_TOKEN, jeremy))
                    .thenReturn(jwtResponseDto);

            // when
            JwtResponseDto result = jwtAuthorizationService.refresh(TestResources.REFRESH_TOKEN);

            // then
            assertNotNull(result);
            assertEquals(jeremy.getId(), result.userId());
            assertEquals(jeremy.getEmail(), result.email());
            assertEquals(TestResources.ACCESS_TOKEN, result.accessToken());
            assertEquals(TestResources.REFRESH_TOKEN, result.refreshToken());
        }

        @Test
        void whenTokenIsNotRefresh_thenThrowException() throws Exception {
            // given
            String refreshToken = "refreshToken";

            // mockito
            Mockito.when(jwtService.isRefreshToken(refreshToken))
                    .thenReturn(false);

            // when + then
            assertThrows(InvalidTokenException.class, () -> jwtAuthorizationService.refresh(refreshToken));
        }

        @Test
        void whenTokenIsNotValid_thenThrowException() throws Exception {
            // mockito
            Mockito.when(jwtService.isRefreshToken(TestResources.REFRESH_TOKEN))
                    .thenReturn(true);
            Mockito.when(jwtService.isValid(TestResources.REFRESH_TOKEN))
                    .thenReturn(false);

            // when + then
            assertThrows(InvalidTokenException.class, () -> jwtAuthorizationService.refresh(TestResources.REFRESH_TOKEN));
        }

        @Test
        void whenUserNotExistsByEmail_thenThrowException() throws Exception {
            // mockito
            Mockito.when(jwtService.isRefreshToken(TestResources.REFRESH_TOKEN))
                    .thenReturn(true);
            Mockito.when(jwtService.isValid(TestResources.REFRESH_TOKEN))
                    .thenReturn(true);
            Mockito.when(jwtService.getEmailFromToken(TestResources.REFRESH_TOKEN))
                    .thenReturn(TestResources.USER_EMAIL);
            Mockito.when(userService.getByEmail(TestResources.USER_EMAIL))
                    .thenReturn(Optional.empty());

            // when + then
            assertThrows(ResourceNotFoundException.class, () -> jwtAuthorizationService.refresh(TestResources.REFRESH_TOKEN));
        }

        @Test
        void whenParameterRefreshTokenIsNull_thenThrowException() throws Exception {
            // given
            String refreshToken = null;

            // mockito
            // ...

            // when + then
            assertThrows(NullPointerException.class, () -> jwtAuthorizationService.refresh(refreshToken));
        }

    }

    private void mockCreateJwtResponseMethod(User jeremy) {
        // mocking method: createJwtResponse(User user)
        Mockito.when(jwtService.createAccessToken(jeremy))
                .thenReturn(TestResources.ACCESS_TOKEN);
        Mockito.when(jwtService.createRefreshToken(jeremy, false))
                .thenReturn(TestResources.REFRESH_TOKEN);
    }

    static class TestResources {

        static final Long USER_ID = 1L;
        static final String USER_EMAIL = "jeremy@gmail.com";
        static final String USER_PASSWORD = "Password123!";
        static final String ACCESS_TOKEN = "accessToken";
        static final String REFRESH_TOKEN = "refreshToken";

        static User buildUser() {
            return UserTestData.getJeremy();
        }

        static UserLoginRequestDto buildUserLoginRequestDto() {
            return new UserLoginRequestDto(
                    USER_EMAIL,
                    USER_PASSWORD,
                    false
            );
        }

        static UserLoginRequestDto buildUserLoginRequestDtoWithRememberMe() {
            return new UserLoginRequestDto(
                    USER_EMAIL,
                    USER_PASSWORD,
                    true
            );
        }
    }
}
