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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.challengeteam.shop.service.impl.UserServiceImplTest.TestResources.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {
    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private UserMerger userMerger;
    @Mock private UserValidator userValidator;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private UserServiceImpl userService;

    @BeforeEach
    public void setup() {
        userService = new UserServiceImpl(userRepository, roleRepository, passwordEncoder, userMerger, userValidator);
    }

    @Nested
    class GetAllTest {

        @Test
        void whenUsersExist_returnAllUsers() {
            // mockito
            Mockito.when(userRepository.findAll())
                    .thenReturn(buildUsersList());

            // when
            List<User> result = userService.getAll();

            // then
            assertThat(result).isEqualTo(buildUsersList());
        }

        @Test
        void whenNoOneUser_thenReturnEmptyList() {
            // mockito
            Mockito.when(userRepository.findAll())
                    .thenReturn(Collections.emptyList());

            // when
            List<User> result = userService.getAll();

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    class GetByIdTest {

        @Test
        void whenUserExists_thenReturnOptionalWithUser() {
            // mockito
            Mockito.when(userRepository.findById(ID_1))
                    .thenReturn(Optional.of(buildUser(ID_1)));

            // when
            Optional<User> result = userService.getById(ID_1);

            // then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(buildUser(ID_1));
        }

        @Test
        void whenUserDoesntExist_thenReturnEmptyOptional() {
            // mockito
            Mockito.when(userRepository.findById(ID_1))
                    .thenReturn(Optional.empty());

            // when
            Optional<User> result = userService.getById(ID_1);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        void whenParameterIdIsNull_whenThrowException() {
            // when + then
            assertThatThrownBy(() -> userService.getById(null)).isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    class GetByEmailTest {

        @Test
        void whenUserExists_thenReturnOptionalWithUser() throws Exception {
            // mockito
            Mockito.when(userRepository.findByEmail(buildEmailForUserWithId(ID_1)))
                    .thenReturn(Optional.of(buildUser(ID_1)));

            // when
            Optional<User> result = userService.getByEmail(buildEmailForUserWithId(ID_1));

            // then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(buildUser(ID_1));
        }

        @Test
        void whenUserDoesntExist_thenReturnEmptyOptional() throws Exception {
            // mockito
            Mockito.when(userRepository.findByEmail(buildEmailForUserWithId(ID_1)))
                    .thenReturn(Optional.empty());

            // when
            Optional<User> result = userService.getByEmail(buildEmailForUserWithId(ID_1));

            // then
            assertThat(result).isEmpty();
        }

        @Test
        void whenParameterEmailIsNull_thenThrowException() throws Exception {
            // when + then
            assertThatThrownBy(() -> userService.getByEmail(null)).isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    class CreateDefaultUserTest {

        @Test
        void whenEmailIsFree_thenCreateAndReturnId() throws Exception {
            // mockito
            Mockito.when(roleRepository.findByName(DEFAULT_ROLE.getName()))
                    .thenReturn(Optional.of(DEFAULT_ROLE));
            Mockito.when(userRepository.save(any()))
                    .thenReturn(buildUser(ID_1));

            // when
            Long result = userService.createDefaultUser(buildCreateUserDto());

            // captor
            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            Mockito.verify(userRepository).save(captor.capture());
            User beforeSave = captor.getValue();

            assertThat(result).isEqualTo(ID_1);
            assertThat(beforeSave.getEmail()).isEqualTo(buildEmailForUserWithId(ID_1));
            assertThat(beforeSave.getRole()).isEqualTo(DEFAULT_ROLE);
            assertThat(beforeSave.getCart()).isNotNull();
            assertThat(passwordEncoder.matches(buildPasswordForUserWithId(ID_1), beforeSave.getPassword())).isTrue();
            Mockito.verify(userValidator).validateEmailIsUnique(buildEmailForUserWithId(ID_1));
        }

        @Test
        void whenEmailIsNotFree_thenThrowException() throws Exception {
            // mockito
            Mockito.doThrow(new EmailAlreadyExistsException())
                    .when(userValidator).validateEmailIsUnique(buildEmailForUserWithId(ID_1));

            // when + then
            assertThatThrownBy(() -> userService.createDefaultUser(buildCreateUserDto()))
                    .isInstanceOf(EmailAlreadyExistsException.class);
        }

        @Test
        void whenDefaultRoleDoesntExist_thenThrowException() throws Exception {
            // mockito
            Mockito.when(roleRepository.findByName(DEFAULT_ROLE.getName()))
                    .thenReturn(Optional.empty());

            // when + then
            assertThatThrownBy(() -> userService.createDefaultUser(buildCreateUserDto()))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        void whenParameterCreateUserDtoIsNull_thenThrowException() throws Exception {
            // when + then
            assertThatThrownBy(() -> userService.createDefaultUser(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    class UpdateProfileTest {

        @Test
        void whenUserExists_thenUpdateProfile() throws Exception {
            // mockito
            Mockito.when(userRepository.findById(ID_1))
                    .thenReturn(Optional.of(buildUser(ID_1)));

            // when
            userService.updateProfile(ID_1, buildUpdateProfileDto(ID_1));

            // then
            Mockito.verify(userMerger).mergeProfile(buildUser(ID_1), buildUpdateProfileDto(ID_1));
            Mockito.verify(userRepository).save(buildUser(ID_1));
            Mockito.verifyNoMoreInteractions(userRepository, userMerger);
        }

        @Test
        void whenUserDoesntExists_thenThrowException() throws Exception {
            // mockito
            Mockito.when(userRepository.findById(ID_1))
                    .thenReturn(Optional.empty());

            // when + then
            assertThatThrownBy(() -> userService.updateProfile(ID_1, buildUpdateProfileDto(ID_1)))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        void whenParameterIdIsNull_thenThrowException() throws Exception {
            // when + then
            assertThatThrownBy(() -> userService.updateProfile(null, buildUpdateProfileDto(ID_1)))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void whenParameterUpdateProfileDtoIsNull_thenThrowException() throws Exception {
            // when + then
            assertThatThrownBy(() -> userService.updateProfile(ID_1, null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    class DeleteTest {

        @Test
        void whenExist_thenDelete() {
            // mockito
            Mockito.when(userRepository.existsById(ID_1))
                    .thenReturn(true);

            // when
            userService.delete(ID_1);

            // then
            Mockito.verify(userRepository).deleteById(ID_1);
        }

        @Test
        void whenDoesntExist_thenThrowException() {
            // mockito
            Mockito.when(userRepository.existsById(ID_1))
                    .thenReturn(false);

            // when + then
            assertThatThrownBy(() -> userService.delete(ID_1)).isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        void whenParameterIdIsNull_thenThrowException() throws Exception {
            // when + then
            assertThatThrownBy(() -> userService.delete(null)).isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    class ExistsByEmailTest {

        @Test
        void whenUserExists_thenReturnTrue() throws Exception {
            // mockito
            Mockito.when(userRepository.existsByEmail(buildEmailForUserWithId(ID_1)))
                    .thenReturn(true);

            // when
            boolean result = userService.existsByEmail(buildEmailForUserWithId(ID_1));

            // then
            assertThat(result).isTrue();
        }

        @Test
        void whenUserDoesntExist_thenReturnFalse() throws Exception {
            // mockito
            Mockito.when(userRepository.existsByEmail(buildEmailForUserWithId(ID_1)))
                    .thenReturn(false);

            // when
            boolean result = userService.existsByEmail(buildEmailForUserWithId(ID_1));

            // then
            assertThat(result).isFalse();
        }

        @Test
        void whenParameterEmailIsNull_thenThrowException() throws Exception {
            // when + then
            assertThatThrownBy(() -> userService.existsByEmail(null)).isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    class ExistsByIdTest {

        @Test
        void whenUserExistsById_thenReturnTrue() throws Exception {
            // mockito
            Mockito.when(userRepository.existsById(ID_1))
                    .thenReturn(true);

            // when
            boolean result = userService.existsById(ID_1);

            // then
            assertThat(result).isTrue();
        }

        @Test
        void whenUserDoesntExistById_thenReturnFalse() throws Exception {
            // mockito
            Mockito.when(userRepository.existsById(ID_1))
                    .thenReturn(false);

            // when
            boolean result = userService.existsById(ID_1);

            // then
            assertThat(result).isFalse();
        }

        @Test
        void whenParameterIdIsNull_thenThrowException() throws Exception {
            // when + then
            assertThatThrownBy(() -> userService.existsById(null)).isInstanceOf(NullPointerException.class);
        }
    }

    static class TestResources {
        public final static Role DEFAULT_ROLE = buildDefaultRole();
        public final static long ID_1 = 1L;
        public final static long ID_2 = 2L;

        public final static String USER_EMAIL_TEMPLATE = "user%s@gmail.com";
        public final static String USER_PASSWORD_TEMPLATE = "Password12%s!";
        public final static String USER_FIRSTNAME_TEMPLATE = "name%s";
        public final static String USER_LASTNAME_TEMPLATE = "lastname%s";
        public final static String USER_CITY_TEMPLATE = "city%s";
        public final static String USER_PHONE_TEMPLATE = "0980000%s";

        private static Role buildDefaultRole() {
            return Role.builder()
                    .id(1L)
                    .name(UserServiceImpl.DEFAULT_ROLE_NAME_FOR_CREATED_USER)
                    .build();
        }

        public static User buildUser(long id) {
            return User.builder()
                    .id(id)
                    .createdAt(Instant.now())
                    .email(USER_EMAIL_TEMPLATE.formatted(id))
                    .firstName(USER_FIRSTNAME_TEMPLATE.formatted(id))
                    .lastName(USER_LASTNAME_TEMPLATE.formatted(id))
                    .city(USER_CITY_TEMPLATE.formatted(id))
                    .phoneNumber(USER_PHONE_TEMPLATE.formatted(id))
                    .role(DEFAULT_ROLE)
                    .build();
        }

        public static List<User> buildUsersList() {
            List<User> result = new ArrayList<>();
            var u1 = buildUser(ID_1);
            var u2 = buildUser(ID_2);
            result.add(u1);
            result.add(u2);

            return result;
        }

        public static String buildEmailForUserWithId(long id) {
            return USER_EMAIL_TEMPLATE.formatted(id);
        }

        public static String buildPasswordForUserWithId(long id) {
            return USER_PASSWORD_TEMPLATE.formatted(id);
        }

        public static CreateUserDto buildCreateUserDto() {
            return new CreateUserDto(
                    buildEmailForUserWithId(ID_1),
                    buildPasswordForUserWithId(ID_1)
            );
        }

        public static UpdateProfileDto buildUpdateProfileDto(long id) {
            return new UpdateProfileDto(
                    USER_FIRSTNAME_TEMPLATE.formatted(id),
                    USER_LASTNAME_TEMPLATE.formatted(id),
                    USER_CITY_TEMPLATE.formatted(id),
                    USER_PHONE_TEMPLATE.formatted(id)
            );
        }
    }
}
