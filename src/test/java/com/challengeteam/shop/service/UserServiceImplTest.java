package com.challengeteam.shop.service;

import com.challengeteam.shop.entity.user.Role;
import com.challengeteam.shop.entity.user.User;
import com.challengeteam.shop.exception.ResourceNotFoundException;
import com.challengeteam.shop.repository.UserRepository;
import com.challengeteam.shop.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void testGetById_UserExists() {
        User user = new User();
        user.setId(1L);
        user.setUsername("username");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = userService.getById(1L);

        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
        assertEquals(user.getUsername(), result.getUsername());
    }

    @Test
    void testGetById_UserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getById(1L));
    }

    @Test
    void testGetByUsername_UserExists() {
        User user = new User();
        user.setId(1L);
        user.setUsername("username");

        when(userRepository.findByUsername("username")).thenReturn(Optional.of(user));

        User result = userService.getByUsername("username");

        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
        assertEquals(user.getUsername(), result.getUsername());
    }

    @Test
    void testGetByUsername_UserNotFound() {
        when(userRepository.findByUsername("username")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getByUsername("username"));
    }

    @Test
    void testCreate() {
        User user = new User();
        user.setId(1L);
        user.setUsername("username");
        user.setPassword("password");

        when(passwordEncoder.encode(user.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.create(user);

        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
        assertEquals(user.getUsername(), result.getUsername());
        assertEquals(Role.USER, result.getRole());
    }

    @Test
    void testExistsByUsername_UserExists() {
        when(userRepository.existsByUsername("username")).thenReturn(true);

        boolean result = userService.existsByUsername("username");

        assertTrue(result);
    }

    @Test
    void testExistsByUsername_UserDoesNotExists() {
        when(userRepository.existsByUsername("username")).thenReturn(false);

        boolean result = userService.existsByUsername("username");

        assertFalse(result);
    }

}
