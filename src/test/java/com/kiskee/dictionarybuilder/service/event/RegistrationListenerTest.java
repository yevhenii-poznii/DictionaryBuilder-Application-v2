package com.kiskee.dictionarybuilder.service.event;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kiskee.dictionarybuilder.enums.user.UserRole;
import com.kiskee.dictionarybuilder.model.dto.token.verification.VerificationTokenData;
import com.kiskee.dictionarybuilder.model.entity.user.UserVocabularyApplication;
import com.kiskee.dictionarybuilder.repository.user.projections.UserSecureProjection;
import com.kiskee.dictionarybuilder.service.email.EmailSenderService;
import com.kiskee.dictionarybuilder.service.time.CurrentDateTimeService;
import com.kiskee.dictionarybuilder.service.token.TokenPersistenceService;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class RegistrationListenerTest {

    @InjectMocks
    private RegistrationListener registrationListener;

    @Mock
    private TokenPersistenceService<VerificationTokenData> tokenService;

    @Mock
    private EmailSenderService emailSenderService;

    @Mock
    private CurrentDateTimeService currentDateTimeService;

    @Test
    void testGenerateVerificationTokenAndSendEmail_When_Then() {
        UUID userId = UUID.fromString("36effc62-d93a-4451-9f7b-7cf82de0d326");
        UserSecureProjection userSecureProjection = new UserVocabularyApplication(
                userId, "someEmail@gmail.com", "username", null, false, UserRole.ROLE_USER, null, null);
        OnRegistrationCompleteEvent event = new OnRegistrationCompleteEvent(userSecureProjection);

        String verificationTokenDto = "someVerificationToken";
        when(currentDateTimeService.getCurrentInstant()).thenReturn(Instant.parse("2024-10-12T00:00:00Z"));
        when(tokenService.persistToken(any(VerificationTokenData.class))).thenReturn(verificationTokenDto);

        registrationListener.onApplicationEvent(event);

        verify(tokenService).persistToken(any(VerificationTokenData.class));
        verify(emailSenderService).sendVerificationEmail(userSecureProjection, verificationTokenDto);
    }
}
