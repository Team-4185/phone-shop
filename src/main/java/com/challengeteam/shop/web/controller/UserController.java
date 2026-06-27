package com.challengeteam.shop.web.controller;

import com.challengeteam.shop.constants.security.jwt.JwtTokenNameConstants;
import com.challengeteam.shop.dto.security.jwt.JwtPublicResponseDto;
import com.challengeteam.shop.dto.security.jwt.JwtResponseDto;
import com.challengeteam.shop.dto.user.request.ChangeEmailRequestDto;
import com.challengeteam.shop.dto.user.request.ChangePasswordRequestDto;
import com.challengeteam.shop.dto.user.request.CreateUserDto;
import com.challengeteam.shop.dto.user.request.UpdateProfileDto;
import com.challengeteam.shop.dto.user.request.sensetiveData.UpdateUserSensitiveDataDto;
import com.challengeteam.shop.dto.user.response.UserPersonalInfoResponseDto;
import com.challengeteam.shop.dto.user.response.UserResponseDto;
import com.challengeteam.shop.entity.user.User;
import com.challengeteam.shop.exceptionHandling.exception.ResourceNotFoundException;
import com.challengeteam.shop.mapper.user.UserMapper;
import com.challengeteam.shop.properties.JwtProperties;
import com.challengeteam.shop.service.UserService;
import com.challengeteam.shop.service.user.credentials.UserCredentialsService;
import com.challengeteam.shop.service.user.personalInfo.UserPersonalInfoService;
import com.challengeteam.shop.service.user.sensetiveData.UserSensitiveDataUpdater;
import com.challengeteam.shop.utility.web.headers.AccessTokenHeaderExtractor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/users")
@SecurityRequirement(name = "bearer-jwt")
@Validated
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;
    private final UserPersonalInfoService userPersonalInfoService;
    private final UserSensitiveDataUpdater userSensitiveDataUpdater;
    private final UserCredentialsService userCredentialsService;
    private final JwtProperties jwtProperties;

    @Operation(
            deprecated = true,
            summary = "temporary: Get all users",
            description = "Returns a list of all users. Later there will be a lot of users, " +
                    "so for efficient work user retrieving should be pageable."
    )
    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        List<User> users = userService.getAll();
        List<UserResponseDto> responses = userMapper.toResponses(users);

        return ResponseEntity.ok(responses);
    }

    @Operation(
            summary = "Get user by id",
            description = "Returns user by id"
    )
    @GetMapping("/{id:\\d+}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long id) {
        User user = userService
                .getById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not found user with id: " + id));
        UserResponseDto response = userMapper.toResponse(user);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Create new user",
            description = "Creates new user based on input data. Email must be unique. " +
                    "Password and password confirmation must be equal. The user will have role: USER."
    )
    @PostMapping
    public ResponseEntity<Void> createDefaultUser(@Valid @RequestBody CreateUserDto createUserDto) {
        Long id = userService.createDefaultUser(createUserDto);
        URI newUserLocation = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/v1/users/{id}")
                .buildAndExpand(id)
                .toUri();

        return ResponseEntity.created(newUserLocation).build();
    }

    @Operation(
            summary = "Update user by id",
            description = "Updates user by id based on input data. " +
                    "If field empty in request, the field won't be changed."
    )
    @PatchMapping("/{id:\\d+}/update-profile")
    public ResponseEntity<Void> updateProfile(@PathVariable Long id,
                                              @Valid @RequestBody UpdateProfileDto updateProfileDto) {
        userService.updateProfile(id, updateProfileDto);

        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Delete user by id",
            description = "Deletes user by id"
    )
    @DeleteMapping("/{id:\\d+}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        userService.delete(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserPersonalInfoResponseDto> getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        UserPersonalInfoResponseDto userPersonalInfo = userPersonalInfoService.getUserPersonalInfo(username);
        return ResponseEntity.ok(userPersonalInfo);
    }

    @PatchMapping("/me/password")
    public ResponseEntity<Void> changeCurrentUserPassword(
            @RequestBody @Valid ChangePasswordRequestDto dto,
            Authentication authentication,
            HttpServletRequest request) {
        userCredentialsService.changePassword(
                dto,
                authentication,
                AccessTokenHeaderExtractor.extractAccessToken(request)
        );
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/me/email")
    public ResponseEntity<Void> changeCurrentUserEmail(
            @RequestBody @Valid ChangeEmailRequestDto dto,
            Authentication authentication,
            HttpServletRequest request) {
        userCredentialsService.changeEmail(
                dto,
                authentication,
                AccessTokenHeaderExtractor.extractAccessToken(request)
        );
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/sensitive")
    public ResponseEntity<JwtPublicResponseDto> updateSensitiveData(
            @RequestBody @Valid UpdateUserSensitiveDataDto dto,
            Authentication authentication,
            @CookieValue(JwtTokenNameConstants.REFRESH_TOKEN_HEADER) String refreshToken,
            HttpServletResponse response,
            HttpServletRequest request) {

        Map<String, String> tokens = Map.of(
                JwtTokenNameConstants.ACCESS_TOKEN_TYPE, AccessTokenHeaderExtractor.extractAccessToken(request),
                JwtTokenNameConstants.REFRESH_TOKEN_TYPE, refreshToken
        );

        JwtResponseDto result = userSensitiveDataUpdater.update(dto, authentication, tokens);

        ResponseCookie cookie = ResponseCookie.from(JwtTokenNameConstants.REFRESH_TOKEN_HEADER, result.refreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/api")
                .maxAge(jwtProperties.getRefreshTokenExpiration())
                .sameSite("Strict")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(new JwtPublicResponseDto(
                result.userId(),
                result.email(),
                result.accessToken()));
    }
}
