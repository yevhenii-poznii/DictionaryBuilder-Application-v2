package com.kiskee.dictionarybuilder.service.token.verification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kiskee.dictionarybuilder.enums.token.TokenType;
import com.kiskee.dictionarybuilder.model.dto.token.verification.VerificationTokenData;
import com.kiskee.dictionarybuilder.model.entity.token.VerificationToken;
import com.kiskee.dictionarybuilder.repository.token.TokenRepository;
import com.kiskee.dictionarybuilder.service.security.token.serializer.TokenSerializer;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class VerificationTokenServiceTest {

    private static final UUID USER_ID = UUID.fromString("36effc62-d93a-4451-9f7b-7cf82de0d326");

    @InjectMocks
    private VerificationTokenService service;

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private TokenSerializer<VerificationTokenData, String> tokenSerializer;

    @Captor
    private ArgumentCaptor<VerificationToken> verificationTokenArgumentCaptor;

    @Test
    void testGenerateToken_WhenUserIdParamIsProvided_ThenGenerateVerificationTokenAndSave() {
        VerificationTokenData verificationTokenData =
                new VerificationTokenData(USER_ID, "someEmail@gmail.com", Instant.parse("2024-10-12T12:00:00Z"));
        String generatedVerificationTokenString = "some_verification_token_string";
        when(tokenSerializer.serialize(verificationTokenData)).thenReturn(generatedVerificationTokenString);

        when(tokenRepository.save(verificationTokenArgumentCaptor.capture())).thenReturn(mock(VerificationToken.class));

        String verificationToken = service.persistToken(verificationTokenData);

        verify(tokenRepository).save(any(VerificationToken.class));

        VerificationToken actualToken = verificationTokenArgumentCaptor.getValue();

        assertThat(verificationToken).isEqualTo(actualToken.getToken());
        assertThat(USER_ID).isEqualTo(actualToken.getUserId());
        assertThat(TokenType.VERIFICATION_TOKEN.getValue()).isEqualTo(actualToken.getDiscriminatorValue());
    }

    @Test
    void testInvalidateToken_WhenGivenVerificationToken_ThenInvalidateUnnecessaryVerificationToken() {
        String verificationTokenString = "some_verification_token_string";

        service.invalidateToken(verificationTokenString);

        verify(tokenRepository).invalidateToken(verificationTokenString);
    }
}
