package com.challengeteam.shop.web.controller;

import com.challengeteam.shop.dto.user.CreateUserDto;
import com.challengeteam.shop.dto.user.UpdateProfileDto;
import com.challengeteam.shop.dto.user.UserResponseDto;
import com.challengeteam.shop.entity.user.User;
import com.challengeteam.shop.exceptionHandling.exception.ResourceNotFoundException;
import com.challengeteam.shop.mapper.UserMapper;
import com.challengeteam.shop.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/users")
@SecurityRequirement(name = "bearer-jwt")
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;

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

}
