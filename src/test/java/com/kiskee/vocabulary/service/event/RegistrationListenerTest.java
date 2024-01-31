package com.kiskee.vocabulary.service.event;

import com.kiskee.vocabulary.model.entity.user.UserVocabularyApplication;
import com.kiskee.vocabulary.repository.user.projections.UserSecureProjection;
import com.kiskee.vocabulary.service.email.EmailSenderService;
import com.kiskee.vocabulary.service.token.TokenGeneratorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RegistrationListenerTest {

    @InjectMocks
    private RegistrationListener registrationListener;

    @Mock
    private TokenGeneratorService<UUID, String> tokenService;
    @Mock
    private EmailSenderService emailSenderService;

    @Test
    void testGenerateVerificationTokenAndSendEmail_When_Then() {
        UUID userId = UUID.fromString("36effc62-d93a-4451-9f7b-7cf82de0d326");
        UserSecureProjection userSecureProjection = new UserVocabularyApplication(userId, "someEmail@gmail.com",
                "username", null, false, null, null);
        OnRegistrationCompleteEvent event = new OnRegistrationCompleteEvent(userSecureProjection);

        String verificationTokenDto = "someVerificationToken";
        when(tokenService.generateToken(userId)).thenReturn(verificationTokenDto);

        registrationListener.onApplicationEvent(event);

        verify(tokenService).generateToken(userId);
        verify(emailSenderService).sendVerificationEmail(userSecureProjection, verificationTokenDto);
    }

}
