package com.challengeteam.shop.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailServiceImpl emailService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "from", "test@gmail.com");
    }

    @Test
    void whenValidParams_thenSendEmail() {
        // given
        String to = "user@gmail.com";
        String resetLink = "https://test.com/reset?token=abc";

        // when
        emailService.sendResetLink(to, resetLink);

        // then
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void whenValidParams_thenMessageHasCorrectFields() {
        // given
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // when
        emailService.sendResetLink("user@gmail.com", "https://test.com/reset?token=abc");

        // then
        verify(mailSender).send(captor.capture());
        SimpleMailMessage sent = captor.getValue();
        assertThat(sent.getTo()).containsExactly("user@gmail.com");
        assertThat(sent.getFrom()).isEqualTo("test@gmail.com");
        assertThat(sent.getText()).contains("https://test.com/reset?token=abc");
    }
}
