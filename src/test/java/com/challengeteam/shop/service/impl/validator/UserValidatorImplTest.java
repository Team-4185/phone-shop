package com.challengeteam.shop.service.impl.validator;

import com.challengeteam.shop.exceptionHandling.exception.EmailAlreadyExistsException;
import com.challengeteam.shop.persistence.repository.UserRepository;
import com.challengeteam.shop.service.impl.validator.impl.UserValidatorImpl;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class UserValidatorImplTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserValidatorImpl userValidator;


    @Nested
    class ValidateEmailIsUniqueTest {

        @Test
        void whenEmailIsFree_thenDoNothing() throws Exception {
            // given
            String email = "email1@gmail.com";

            // mockito
            Mockito.when(userRepository.existsByEmail("email1@gmail.com"))
                    .thenReturn(false);

            // when + then
            assertDoesNotThrow(() -> userValidator.validateEmailIsUnique(email));
        }

        @Test
        void whenEmailIsNotFree_thenThrowException() throws Exception {
            // given
            String email = "email1@gmail.com";

            // mockito
            Mockito.when(userRepository.existsByEmail("email1@gmail.com"))
                    .thenReturn(true);

            // when + then
            assertThrows(EmailAlreadyExistsException.class, () -> userValidator.validateEmailIsUnique(email));
        }

    }

}