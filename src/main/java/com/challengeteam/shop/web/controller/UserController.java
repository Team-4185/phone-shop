package com.challengeteam.shop.web.controller;

import com.challengeteam.shop.dto.user.CreateUserDto;
import com.challengeteam.shop.dto.user.UpdateProfileDto;
import com.challengeteam.shop.dto.user.UserResponseDto;
import com.challengeteam.shop.entity.user.User;
import com.challengeteam.shop.exceptionHandling.exception.ResourceNotFoundException;
import com.challengeteam.shop.mapper.UserMapper;
import com.challengeteam.shop.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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

    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        List<User> users = userService.getAll();
        List<UserResponseDto> responses = userMapper.toResponses(users);

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long id) {
        User user = userService
                .getById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not found user with id: " + id));
        UserResponseDto response = userMapper.toResponse(user);

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<Void> createDefaultUser(@RequestBody CreateUserDto createUserDto) {
        Long id = userService.createDefaultUser(createUserDto);
        URI newUserLocation = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/v1/users/{id}")
                .buildAndExpand(id)
                .toUri();

        return ResponseEntity.created(newUserLocation).build();
    }

    @PatchMapping("/{id}/update-profile")
    public ResponseEntity<Void> updateProfile(@PathVariable Long id,
                                              @RequestBody UpdateProfileDto updateProfileDto) {
        userService.updateProfile(id, updateProfileDto);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        userService.delete(id);

        return ResponseEntity.noContent().build();
    }

}
