package com.challengeteam.shop.service.impl;

import com.challengeteam.shop.dto.user.CreateUserDto;
import com.challengeteam.shop.dto.user.UpdateProfileDto;
import com.challengeteam.shop.entity.user.Role;
import com.challengeteam.shop.entity.user.User;
import com.challengeteam.shop.exceptionHandling.exception.EmailAlreadyExistsException;
import com.challengeteam.shop.exceptionHandling.exception.ResourceNotFoundException;
import com.challengeteam.shop.persistence.repository.RoleRepository;
import com.challengeteam.shop.persistence.repository.UserRepository;
import com.challengeteam.shop.service.impl.merger.UserMerger;
import com.challengeteam.shop.service.impl.validator.UserValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserMerger userMerger;

    @Mock
    private UserValidator userValidator;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private UserServiceImpl userService;


    @BeforeEach
    public void initBeforeEach() {
        userService = new UserServiceImpl(userRepository, roleRepository, passwordEncoder, userMerger, userValidator);
    }

    @Nested
    class GetAllTest {

        @Test
        void whenUsersExist_returnAllUsers() {
            // given
            List<User> users = buildUserFromTo(1, 11);
            List<User> expected = buildUserFromTo(1, 11);

            // mockito
            Mockito.when(userRepository.findAll())
                    .thenReturn(users);

            // when
            List<User> result = userService.getAll();

            // then
            assertNotNull(result);
            assertEquals(expected, result);
        }

        @Test
        void whenNoOneUser_thenReturnEmptyList() {
            // given
            List<User> users = List.of();
            List<User> expected = List.of();

            // mockito
            Mockito.when(userRepository.findAll())
                    .thenReturn(users);

            // when
            List<User> result = userService.getAll();

            // then
            assertNotNull(result);
            assertEquals(expected, result);
        }

    }

    @Nested
    class GetByIdTest {

        @Test
        void whenUserExists_thenReturnOptionalWithUser() {
            // given
            User user = buildUser(10L);
            User expected = buildUser(10L);

            // mockito
            Mockito.when(userRepository.findById(10L))
                    .thenReturn(Optional.of(user));

            // when
            Optional<User> result = userService.getById(10L);

            // then
            assertNotNull(result);
            assertTrue(result.isPresent());
            assertEquals(expected, result.get());
        }

        @Test
        void whenUserDoesntExist_thenReturnEmptyOptional() {
            // given
            // ...

            // mockito
            Mockito.when(userRepository.findById(10L))
                    .thenReturn(Optional.empty());

            // when
            Optional<User> result = userService.getById(10L);

            // then
            assertNotNull(result);
            assertFalse(result.isPresent());
        }

        @Test
        void whenParameterIdIsNull_whenThrowException() {
            // given
            Long id = null;

            // mockito
            // ...

            // when + then
            assertThrows(NullPointerException.class, () -> userService.getById(id));
        }

    }

    @Nested
    class GetByEmailTest {

        @Test
        void whenUserExists_thenReturnOptionalWithUser() throws Exception {
            // given
            User user = buildUser(10L);
            String email = "user" + 10L + "@gmail.com";
            User expected = buildUser(10L);

            // mockito
            Mockito.when(userRepository.findByEmail(email))
                    .thenReturn(Optional.of(user));

            // when
            Optional<User> result = userService.getByEmail(email);

            // then
            assertNotNull(result);
            assertTrue(result.isPresent());
            assertEquals(expected, result.get());
        }

        @Test
        void whenUserDoesntExist_thenReturnEmptyOptional() throws Exception {
            // given
            String email = "user" + 10L + "@gmail.com";

            // mockito
            Mockito.when(userRepository.findByEmail(email))
                    .thenReturn(Optional.empty());

            // when
            Optional<User> result = userService.getByEmail(email);

            // then
            assertNotNull(result);
            assertFalse(result.isPresent());
        }

        @Test
        void whenParameterEmailIsNull_thenThrowException() throws Exception {
            // given
            String email = null;

            // mockito
            // ...

            // when + then
            assertThrows(NullPointerException.class, () -> userService.getByEmail(email));
        }

    }

    @Nested
    class CreateDefaultUserTest {

        @Test
        void whenEmailIsFree_thenCreateAndReturnId() throws Exception {
            // given
            String email = "user" + 10L + "@gmail.com";
            String password = "secure1234";
            var createUserDto = new CreateUserDto(email, password);

            // mockito
            Mockito.when(roleRepository.findByName(UserServiceImpl.DEFAULT_ROLE_NAME_FOR_CREATED_USER))
                    .thenReturn(Optional.of(getDefaultRole()));
            Mockito.when(userRepository.save(any()))
                    .thenReturn(buildUser(10L));

            // when
            Long result = userService.createDefaultUser(createUserDto);

            // then
            assertNotNull(result);
            assertEquals(10L, result);

            // captor
            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            Mockito.verify(userRepository).save(captor.capture());
            User beforeSave = captor.getValue();

            assertTrue(passwordEncoder.matches(password, beforeSave.getPassword()));
            assertEquals(email, beforeSave.getEmail());
            assertEquals(getDefaultRole(), beforeSave.getRole());
        }

        @Test
        void whenEmailIsNotFree_thenThrowException() throws Exception {
            // given
            String email = "user" + 10L + "@gmail.com";
            String password = "secure1234";
            var createUserDto = new CreateUserDto(email, password);

            // mockito
            Mockito.doThrow(new EmailAlreadyExistsException())
                    .when(userValidator).validateEmailIsUnique(email);

            // when + then
            assertThrows(EmailAlreadyExistsException.class, () -> userService.createDefaultUser(createUserDto));
        }

        @Test
        void whenDefaultRoleDoesntExist_thenThrowException() throws Exception {
            // given
            String email = "user" + 10L + "@gmail.com";
            String password = "secure1234";
            var createUserDto = new CreateUserDto(email, password);

            // mockito
            Mockito.when(roleRepository.findByName(any()))
                    .thenReturn(Optional.empty());

            // when + then
            assertThrows(ResourceNotFoundException.class, () -> userService.createDefaultUser(createUserDto));
        }

        @Test
        void whenParameterCreateUserDtoIsNull_thenThrowException() throws Exception {
            // given
            CreateUserDto createUserDto = null;

            // mockito
            // ...

            // when + then
            assertThrows(NullPointerException.class, () -> userService.createDefaultUser(createUserDto));
        }

    }

    @Nested
    class UpdateProfileTest {

        @Test
        void whenUserExists_thenUpdateProfile() throws Exception {
            // given
            Long id = 10L;
            User user = buildUser(id);
            var updateProfileDto = new UpdateProfileDto(
                    "newFirstname",
                    "newLastname",
                    "newCity",
                    "newPhoneNumber"
            );

            // mockito
            Mockito.when(userRepository.findById(id))
                    .thenReturn(Optional.of(user));

            // when
            userService.updateProfile(id, updateProfileDto);

            // then
            Mockito.verify(userRepository).findById(id);
            Mockito.verify(userMerger).mergeProfile(user, updateProfileDto);
            Mockito.verify(userRepository).save(user);
            Mockito.verifyNoMoreInteractions(userRepository, userMerger);
        }

        @Test
        void whenUserDoesntExists_thenThrowException() throws Exception {
            // given
            Long id = 10L;
            var updateProfileDto = new UpdateProfileDto(
                    "newFirstname",
                    "newLastname",
                    "newCity",
                    "newPhoneNumber"
            );

            // mockito
            Mockito.when(userRepository.findById(id))
                    .thenReturn(Optional.empty());

            // when + then
            assertThrows(ResourceNotFoundException.class, () -> userService.updateProfile(id, updateProfileDto));
        }

        @Test
        void whenParameterIdIsNull_thenThrowException() throws Exception {
            // given
            Long id = null;
            var updateProfileDto = new UpdateProfileDto(
                    "newFirstname",
                    "newLastname",
                    "newCity",
                    "newPhoneNumber"
            );

            // mockito
            // ..

            // when + then
            assertThrows(NullPointerException.class, () -> userService.updateProfile(id, updateProfileDto));
        }

        @Test
        void whenParameterUpdateProfileDtoIsNull_thenThrowException() throws Exception {
            // given
            Long id = 10L;
            UpdateProfileDto updateProfileDto = null;

            // mockito
            // ..

            // when + then
            assertThrows(NullPointerException.class, () -> userService.updateProfile(id, updateProfileDto));
        }

    }

    @Nested
    class DeleteTest {

        @Test
        void whenCalled_thenReturnNothing() throws Exception {
            // given
            Long id = 10L;

            // mockito
            // ..

            // when
            userService.delete(id);

            // then
            Mockito.verify(userRepository).deleteById(id);
            Mockito.verifyNoMoreInteractions(userRepository);
        }

        @Test
        void whenParameterIdIsNull_thenThrowException() throws Exception {
            // given
            Long id = null;

            // mockito
            // ..

            // when + then
            assertThrows(NullPointerException.class, () -> userService.delete(id));
        }

    }

    @Nested
    class ExistsByEmailTest {

        @Test
        void whenUserExists_thenReturnTrue() throws Exception {
            // given
            String email = "user" + 10L + "@gmail.com";

            // mockito
            Mockito.when(userRepository.existsByEmail(email))
                    .thenReturn(true);

            // when
            boolean result = userService.existsByEmail(email);

            // then
            assertTrue(result);
        }

        @Test
        void whenParameterEmailIsNull_thenThrowException() throws Exception {
            // given
            String email = null;

            // mockito
            // ..

            // when + then
            assertThrows(NullPointerException.class, () -> userService.existsByEmail(email));
        }

    }

    private List<User> buildUserFromTo(long from, long to) {
        return LongStream.range(from, to)
                .mapToObj(this::buildUser)
                .toList();
    }

    private User buildUser(Long id) {
        var user = User.builder()
                .email("user" + id + "@gmail.com")
                .firstName("name" + id)
                .lastName("lastname" + id)
                .city("city" + id)
                .phoneNumber("0999999" + id)
                .role(getDefaultRole())
                .build();
        user.setId(id);
        user.setCreatedAt(Instant.now());

        return user;
    }

    private Role getDefaultRole() {
        var role = new Role();
        role.setId(1L);
        role.setName(UserServiceImpl.DEFAULT_ROLE_NAME_FOR_CREATED_USER);

        return role;
    }

}
