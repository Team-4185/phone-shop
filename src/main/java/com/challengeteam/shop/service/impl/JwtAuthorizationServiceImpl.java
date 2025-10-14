package com.challengeteam.shop.service.impl;

import com.challengeteam.shop.dto.jwt.JwtResponseDto;
import com.challengeteam.shop.dto.user.CreateUserDto;
import com.challengeteam.shop.dto.user.UserLoginRequestDto;
import com.challengeteam.shop.dto.user.UserRegisterRequestDto;
import com.challengeteam.shop.entity.user.User;
import com.challengeteam.shop.exceptionHandling.exception.*;
import com.challengeteam.shop.service.JwtAuthorizationService;
import com.challengeteam.shop.service.JwtService;
import com.challengeteam.shop.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtAuthorizationServiceImpl implements JwtAuthorizationService {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtService jwtService;


    @Override
    public JwtResponseDto register(UserRegisterRequestDto registerRequest) {
        Objects.requireNonNull(registerRequest, "registerRequest");

        if (!registerRequest.password().equals(registerRequest.passwordConfirmation())) {
            throw new InvalidAPIRequestException("Password and confirmation do not match");
        }

        var createUserDto = new CreateUserDto(
                registerRequest.email(),
                registerRequest.password()
        );
        Long id = userService.createDefaultUser(createUserDto);
        User user = userService
                .getById(id)
                .orElseThrow(() -> new CriticalSystemException("Not found user immediately after creating. User id: " + id));

        log.debug("Created user with email: {}", registerRequest.email());
        return createJwtResponse(user);
    }

    @Override
    public JwtResponseDto login(UserLoginRequestDto loginRequest) {
        Objects.requireNonNull(loginRequest, "loginRequest");

        String email = loginRequest.email();
        var authenticationToken = new UsernamePasswordAuthenticationToken(email, loginRequest.password());
        tryAuthenticate(authenticationToken);
        User user = userService
                .getByEmail(email)
                .orElseThrow(() -> new CriticalSystemException("User not found after success authorization. User email: " + email));

        log.debug("Login user with email {}", email);
        return createJwtResponse(user);
    }

    @Override
    public JwtResponseDto refresh(String refreshToken) {
        Objects.requireNonNull(refreshToken, "refreshToken");

        if (jwtService.isValid(refreshToken)) {
            String email = jwtService.getEmailFromToken(refreshToken);
            User user = userService
                    .getByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("Not found user with email: "));

            log.debug("Called refreshing token for user with email: {}", email);
            return jwtService.refreshTokens(refreshToken, user);
        } else {
            throw new InvalidTokenException("Refresh token is invalid");
        }
    }

    private JwtResponseDto createJwtResponse(User user) {
        String accessToken = jwtService.createAccessToken(user);
        String refreshToken = jwtService.createRefreshToken(user);

        return new JwtResponseDto(
                user.getId(),
                user.getEmail(),
                accessToken,
                refreshToken
        );
    }

    private void tryAuthenticate(UsernamePasswordAuthenticationToken authenticationToken) {
        try {
            authenticationManager.authenticate(authenticationToken);
        } catch (BadCredentialsException e) {
            throw new EmailOrPasswordWrongException("Email or password are wrong");
        } catch (LockedException e) {
            throw new AccountLockedException("Account is locked");
        } catch (DisabledException e) {
            throw new AccountDisabledException("Account is disabled");
        } catch (AuthenticationException e) {
            throw new AuthenticationFailedException(e);
        }
    }

}
