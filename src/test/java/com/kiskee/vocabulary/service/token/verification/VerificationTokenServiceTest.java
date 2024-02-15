package com.kiskee.vocabulary.service.token.verification;

import com.kiskee.vocabulary.enums.ExceptionStatusesEnum;
import com.kiskee.vocabulary.enums.token.TokenEnum;
import com.kiskee.vocabulary.exception.ResourceNotFoundException;
import com.kiskee.vocabulary.model.entity.token.Token;
import com.kiskee.vocabulary.model.entity.token.VerificationToken;
import com.kiskee.vocabulary.repository.token.TokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class VerificationTokenServiceTest {

    private static final UUID USER_ID = UUID.fromString("36effc62-d93a-4451-9f7b-7cf82de0d326");

    @InjectMocks
    private VerificationTokenService service;

    @Mock
    private TokenRepository tokenRepository;
    @Mock
    private Supplier<String> tokenGenerator;

    @Captor
    private ArgumentCaptor<VerificationToken> verificationTokenArgumentCaptor;

    @Test
    void testGenerateToken_WhenUserIdParamIsProvided_ThenGenerateVerificationTokenAndSave() {
        String generatedVerificationTokenString = "some_verification_token_string";
        when(tokenGenerator.get()).thenReturn(generatedVerificationTokenString);

        when(tokenRepository.save(verificationTokenArgumentCaptor.capture())).thenReturn(mock(VerificationToken.class));

        String verificationToken = service.persistToken(USER_ID);

        verify(tokenGenerator).get();
        verify(tokenRepository).save(any(VerificationToken.class));

        VerificationToken actualToken = verificationTokenArgumentCaptor.getValue();

        assertThat(verificationToken).isEqualTo(actualToken.getToken());
        assertThat(USER_ID).isEqualTo(actualToken.getUserId());
        assertThat(TokenEnum.VERIFICATION_TOKEN.getValue()).isEqualTo(actualToken.getDiscriminatorValue());
    }

    @Test
    void testFindTokenOrThrow_WhenVerificationTokenExists_ThenReturnVerificationToken() {
        String verificationTokenParam = "some_verification_token_string";

        VerificationToken foundToken = mock(VerificationToken.class);
        when(foundToken.getToken()).thenReturn(verificationTokenParam);
        when(tokenRepository.findByToken(verificationTokenParam)).thenReturn(Optional.of(foundToken));

        VerificationToken result = service.findTokenOrThrow(verificationTokenParam);

        verify(tokenRepository).findByToken(verificationTokenParam);

        assertThat(result.getToken()).isEqualTo(verificationTokenParam);
    }

    @Test
    void testFindTokenOrThrow_WhenVerificationTokenDoesNotExist_ThenThrowResourceNotFoundException() {
        String verificationTokenParam = "some_verification_token_string";

        when(tokenRepository.findByToken(verificationTokenParam)).thenReturn(Optional.empty());

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> service.findTokenOrThrow(verificationTokenParam))
                .withMessage(String.format(ExceptionStatusesEnum.RESOURCE_NOT_FOUND.getStatus(),
                        Token.class.getSimpleName(), verificationTokenParam));
    }

    @Test
    void testInvalidateToken_WhenGivenVerificationToken_ThenInvalidateUnnecessaryVerificationToken() {
        String verificationTokenString = "some_verification_token_string";
        Instant createdAt = Instant.parse("2024-01-30T12:00:00Z");
        VerificationToken token = new VerificationToken(verificationTokenString, USER_ID, createdAt);

        service.invalidateToken(token);

        verify(tokenRepository).save(verificationTokenArgumentCaptor.capture());

        VerificationToken actual = verificationTokenArgumentCaptor.getValue();
        assertThat(actual.getToken()).isEqualTo(verificationTokenString);
        assertThat(actual.isInvalidated()).isTrue();
    }

}
