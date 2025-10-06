package com.challengeteam.shop.service;

import com.challengeteam.shop.dto.jwt.JwtLoginRequest;
import com.challengeteam.shop.dto.jwt.JwtResponse;
import com.challengeteam.shop.dto.user.UserRegisterRequest;
import com.challengeteam.shop.entity.user.Role;
import com.challengeteam.shop.entity.user.User;
import com.challengeteam.shop.mapper.UserMapper;
import com.challengeteam.shop.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {

    @Mock
    private UserService userService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private JwtTokenService jwtTokenService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthServiceImpl authService;

    private final String accessToken = "accessToken";
    private final String refreshToken = "refreshToken";
    private final Long userId = 1L;
    private final String email = "email";
    private final String password = "password";
    private final String passwordConfirmation = "password";

    @Test
    void testRegister() {
        UserRegisterRequest userRegisterRequest =
                new UserRegisterRequest(email, password, passwordConfirmation);

        Role role = new Role();
        role.setName("USER");
        User user = new User();
        user.setId(userId);
        user.setEmail(email);
        user.setRole(role);

        when(userService.existsByEmail(userRegisterRequest.email())).thenReturn(false);
        when(userMapper.toUser(userRegisterRequest)).thenReturn(user);
        when(userService.create(user)).thenReturn(user);
        when(jwtTokenService.createAccessToken(user.getId(), user.getEmail(), user.getRole())).thenReturn(accessToken);
        when(jwtTokenService.createRefreshToken(user.getId(), user.getEmail(), user.getRole())).thenReturn(refreshToken);

        JwtResponse jwtResponse = authService.register(userRegisterRequest);

        assertNotNull(jwtResponse);
        assertEquals(user.getId(), jwtResponse.userId());
        assertEquals(user.getEmail(), jwtResponse.email());
        assertEquals(accessToken, jwtResponse.accessToken());
        assertEquals(refreshToken, jwtResponse.refreshToken());
    }

    @Test
    void testRegister_UsernameAlreadyExists() {
        UserRegisterRequest userRegisterRequest = new
                UserRegisterRequest(email, password, passwordConfirmation);

        when(userService.existsByEmail(userRegisterRequest.email())).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> authService.register(userRegisterRequest));
    }

    @Test
    void testRegister_PasswordsNotMatch() {
        UserRegisterRequest userRegisterRequest = new
                UserRegisterRequest(email, password, passwordConfirmation + "123");

        when(userService.existsByEmail(userRegisterRequest.email())).thenReturn(false);

        assertThrows(IllegalStateException.class, () -> authService.register(userRegisterRequest));
    }

    @Test
    void testLogin() {
        JwtLoginRequest jwtLoginRequest = new JwtLoginRequest();
        jwtLoginRequest.setUsername(email);
        jwtLoginRequest.setPassword(password);

        Role role = new Role();
        role.setName("USER");
        User user = new User();
        user.setId(userId);
        user.setEmail(email);
        user.setRole(role);

        when(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(
                        jwtLoginRequest.getUsername(),
                        jwtLoginRequest.getPassword()
                ));
        when(userService.getByEmail(jwtLoginRequest.getUsername())).thenReturn(user);
        when(jwtTokenService.createAccessToken(user.getId(), user.getEmail(), user.getRole())).thenReturn(accessToken);
        when(jwtTokenService.createRefreshToken(user.getId(), user.getEmail(), user.getRole())).thenReturn(refreshToken);

        JwtResponse jwtResponse = authService.login(jwtLoginRequest);

        assertNotNull(jwtResponse);
        assertEquals(user.getId(), jwtResponse.userId());
        assertEquals(user.getEmail(), jwtResponse.email());
        assertEquals(accessToken, jwtResponse.accessToken());
        assertEquals(refreshToken, jwtResponse.refreshToken());
    }

    @Test
    void testRefresh() {
        JwtResponse jwtResponse =
                new JwtResponse(userId, email, "newRefreshToken", "newAccessToken");

        when(jwtTokenService.refreshTokens(refreshToken)).thenReturn(jwtResponse);

        JwtResponse response = authService.refresh(refreshToken);

        assertNotNull(response);
        assertEquals(jwtResponse.refreshToken(), response.refreshToken());
        assertEquals(jwtResponse.accessToken(), response.accessToken());
    }

}
