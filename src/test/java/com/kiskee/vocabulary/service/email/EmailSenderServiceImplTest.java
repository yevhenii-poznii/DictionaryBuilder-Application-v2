package com.kiskee.vocabulary.service.email;

import com.kiskee.vocabulary.config.properties.email.EmailContextProperties;
import com.kiskee.vocabulary.exception.email.SendEmailException;
import com.kiskee.vocabulary.model.entity.user.UserVocabularyApplication;
import com.kiskee.vocabulary.repository.user.projections.UserSecureProjection;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EmailSenderServiceImplTest {

    @InjectMocks
    private EmailSenderServiceImpl emailSenderService;

    @Mock
    private JavaMailSenderImpl mailSender;
    @Mock
    private TemplateEngine templateEngine;
    @Mock
    private EmailContextProperties emailContextProperties;

    @Test
    void testSendVerificationEmail_WhenParamsProvided_ThenSendEmail() {
        UserSecureProjection userDataForEmail = new UserVocabularyApplication(null, "someEmail@gmail.com",
                "username", null, false, null, null);
        String verificationTokenString = "generatedToken";

        when(emailContextProperties.getConfirmationUrl()).thenReturn("http://localhost/signup?token=");
        when(emailContextProperties.getFrom()).thenReturn("admin@vocabulary.com");
        when(emailContextProperties.getTemplateLocation()).thenReturn("email/confirm-registration.html");
        when(emailContextProperties.getSubject()).thenReturn("Please verify your registration");

        String htmlContent = "HTML_CONTENT";
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn(htmlContent);
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailSenderService.sendVerificationEmail(userDataForEmail, verificationTokenString);

        verify(templateEngine).process(anyString(), any(Context.class));
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    @SneakyThrows
    void testSendVerificationEmail_When_Then() {
        UserSecureProjection userDataForEmail = new UserVocabularyApplication(null, "someEmail@gmail.com",
                "username", null, false, null, null);
        String verificationTokenDto = "generatedToken";

        when(emailContextProperties.getConfirmationUrl()).thenReturn("http://localhost/signup?token=");
        when(emailContextProperties.getFrom()).thenReturn("admin@vocabulary.com");
        when(emailContextProperties.getTemplateLocation()).thenReturn("email/confirm-registration.html");

        String htmlContent = "HTML_CONTENT";
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn(htmlContent);

        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        doThrow(new MessagingException("Some error"))
                .when(mimeMessage).setFrom(new InternetAddress("admin@vocabulary.com"));

        assertThatExceptionOfType(SendEmailException.class)
                .isThrownBy(() -> emailSenderService.sendVerificationEmail(userDataForEmail, verificationTokenDto))
                .withMessage("Some error");
    }

}
