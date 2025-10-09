package com.challengeteam.shop.service.impl;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class JwtAuthorizationServiceImplTest {

    // todo: write test
//    @Mock
//    private UserService userService;
//
//    @Mock
//    private UserMapper userMapper;
//
//    @Mock
//    private JwtService jwtService;
//
//    @Mock
//    private AuthenticationManager authenticationManager;
//
//    @InjectMocks
//    private JwtAuthorizationServiceImpl authService;
//
//    private final String accessToken = "accessToken";
//    private final String refreshToken = "refreshToken";
//    private final Long userId = 1L;
//    private final String email = "email";
//    private final String password = "password";
//    private final String passwordConfirmation = "password";
//
//    @Test
//    void testRegister() {
//        var registerRequest = new UserRegisterRequestDto(email, password, passwordConfirmation);
//
//        Role role = new Role();
//        role.setName(UserServiceImpl.DEFAULT_ROLE_NAME_FOR_CREATED_USER);
//
//        User user = new User();
//        user.setId(userId);
//        user.setEmail(email);
//        user.setRole(role);
//
//        var createUserDto = new CreateUserDto(email, password);
//
//        when(userService.existsByEmail(registerRequest.email())).thenReturn(false);
//        when(userService.createDefaultUser(createUserDto)).thenReturn(user.getId());
//        when(userService.getById(userId)).thenReturn(Optional.of(user));
//        when(jwtService.createAccessToken(user.getId(), user.getEmail(), user.getRole())).thenReturn(accessToken);
//        when(jwtService.createRefreshToken(user.getId(), user.getEmail(), user.getRole())).thenReturn(refreshToken);
//
//        JwtResponseDto jwtResponseDto = authService.register(registerRequest);
//
//        assertNotNull(jwtResponseDto);
//        assertEquals(user.getId(), jwtResponseDto.userId());
//        assertEquals(user.getEmail(), jwtResponseDto.email());
//        assertEquals(accessToken, jwtResponseDto.accessToken());
//        assertEquals(refreshToken, jwtResponseDto.refreshToken());
//    }
//
//    @Test
//    void testRegister_UsernameAlreadyExists() {
//        UserRegisterRequestDto userRegisterRequestDto = new
//                UserRegisterRequestDto(email, password, passwordConfirmation);
//
//        when(userService.existsByEmail(userRegisterRequestDto.email())).thenReturn(true);
//
//        assertThrows(IllegalStateException.class, () -> authService.register(userRegisterRequestDto));
//    }
//
//    @Test
//    void testRegister_PasswordsNotMatch() {
//        UserRegisterRequestDto userRegisterRequestDto = new
//                UserRegisterRequestDto(email, password, passwordConfirmation + "123");
//
//        when(userService.existsByEmail(userRegisterRequestDto.email())).thenReturn(false);
//
//        assertThrows(IllegalStateException.class, () -> authService.register(userRegisterRequestDto));
//    }
//
//    @Test
//    void testLogin() {
//        UserLoginRequestDto userLoginRequestDto = new UserLoginRequestDto();
//        userLoginRequestDto.setUsername(email);
//        userLoginRequestDto.setPassword(password);
//
//        Role role = new Role();
//        role.setName("USER");
//        User user = new User();
//        user.setId(userId);
//        user.setEmail(email);
//        user.setRole(role);
//
//        when(authenticationManager.authenticate(
//                any(UsernamePasswordAuthenticationToken.class)))
//                .thenReturn(new UsernamePasswordAuthenticationToken(
//                        userLoginRequestDto.getUsername(),
//                        userLoginRequestDto.getPassword()
//                ));
//        when(userService.getByEmail(userLoginRequestDto.getUsername())).thenReturn(Optional.of(user));
//        when(jwtService.createAccessToken(user.getId(), user.getEmail(), user.getRole())).thenReturn(accessToken);
//        when(jwtService.createRefreshToken(user.getId(), user.getEmail(), user.getRole())).thenReturn(refreshToken);
//
//        JwtResponseDto jwtResponseDto = authService.login(userLoginRequestDto);
//
//        assertNotNull(jwtResponseDto);
//        assertEquals(user.getId(), jwtResponseDto.userId());
//        assertEquals(user.getEmail(), jwtResponseDto.email());
//        assertEquals(accessToken, jwtResponseDto.accessToken());
//        assertEquals(refreshToken, jwtResponseDto.refreshToken());
//    }
//
//    @Test
//    void testRefresh() {
//        JwtResponseDto jwtResponseDto =
//                new JwtResponseDto(userId, email, "newRefreshToken", "newAccessToken");
//
//        when(jwtService.refreshTokens(refreshToken)).thenReturn(jwtResponseDto);
//
//        JwtResponseDto response = authService.refresh(refreshToken);
//
//        assertNotNull(response);
//        assertEquals(jwtResponseDto.refreshToken(), response.refreshToken());
//        assertEquals(jwtResponseDto.accessToken(), response.accessToken());
//    }

}
