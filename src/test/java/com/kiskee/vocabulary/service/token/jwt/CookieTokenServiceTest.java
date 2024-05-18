package com.kiskee.vocabulary.service.token.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kiskee.vocabulary.enums.ExceptionStatusesEnum;
import com.kiskee.vocabulary.enums.token.TokenEnum;
import com.kiskee.vocabulary.exception.ResourceNotFoundException;
import com.kiskee.vocabulary.model.dto.token.JweToken;
import com.kiskee.vocabulary.model.dto.token.TokenData;
import com.kiskee.vocabulary.model.entity.token.CookieToken;
import com.kiskee.vocabulary.model.entity.token.Token;
import com.kiskee.vocabulary.repository.token.TokenRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CookieTokenServiceTest {

    private static final UUID USER_ID = UUID.fromString("36effc62-d93a-4451-9f7b-7cf82de0d326");

    @InjectMocks
    private CookieTokenService service;

    @Mock
    private TokenRepository tokenRepository;

    @Captor
    private ArgumentCaptor<CookieToken> cookieTokenArgumentCaptor;

    @Test
    void testGenerateToken_WhenJweTokenParamIsProvided_ThenGenerateCookieTokenAndSave() {
        JweToken jweToken = JweToken.builder()
                .setId(USER_ID)
                .setSubject("username")
                .setAuthorities(List.of())
                .setCreatedAt(Instant.parse("2024-01-30T12:00:00Z"))
                .setExpiresAt(Instant.parse("2024-01-31T12:00:00Z"))
                .build();
        String generatedCookieTokenString = "some_cookie_token_string";
        TokenData tokenData = new TokenData(generatedCookieTokenString, jweToken);

        when(tokenRepository.save(cookieTokenArgumentCaptor.capture())).thenReturn(mock(CookieToken.class));

        String cookieToken = service.persistToken(tokenData);

        verify(tokenRepository).save(any(CookieToken.class));

        CookieToken actualToken = cookieTokenArgumentCaptor.getValue();

        assertThat(cookieToken).isEqualTo(actualToken.getToken());
        assertThat(USER_ID).isEqualTo(actualToken.getUserId());
        assertThat(TokenEnum.JWT_REFRESH_TOKEN.getValue()).isEqualTo(actualToken.getDiscriminatorValue());
    }

    @Test
    void testFindTokenOrThrow_WhenCookieTokenExists_ThenReturnCookieToken() {
        String cookieTokenParam = "some_cookie_token_string";

        CookieToken foundToken = mock(CookieToken.class);
        when(foundToken.getToken()).thenReturn(cookieTokenParam);
        when(tokenRepository.findByToken(cookieTokenParam)).thenReturn(Optional.of(foundToken));

        CookieToken result = service.findTokenOrThrow(cookieTokenParam);

        verify(tokenRepository).findByToken(cookieTokenParam);

        assertThat(result.getToken()).isEqualTo(cookieTokenParam);
    }

    @Test
    void testFindTokenOrThrow_WhenCookieTokenDoesNotExist_ThenThrowResourceNotFoundException() {
        String cookieTokenParam = "some_verification_token_string";

        when(tokenRepository.findByToken(cookieTokenParam)).thenReturn(Optional.empty());

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> service.findTokenOrThrow(cookieTokenParam))
                .withMessage(String.format(
                        ExceptionStatusesEnum.RESOURCE_NOT_FOUND.getStatus(),
                        Token.class.getSimpleName(),
                        cookieTokenParam));
    }

    @Test
    void testInvalidateToken_WhenGivenCookieToken_ThenInvalidateUnnecessaryCookieToken() {
        String verificationTokenString = "some_verification_token_string";
        Instant createdAt = Instant.parse("2024-01-30T12:00:00Z");
        Instant expiresAt = Instant.parse("2024-01-31T12:00:00Z");
        CookieToken token = new CookieToken(verificationTokenString, USER_ID, createdAt, expiresAt);

        service.invalidateToken(token);

        verify(tokenRepository).save(cookieTokenArgumentCaptor.capture());

        CookieToken actual = cookieTokenArgumentCaptor.getValue();
        assertThat(actual.getToken()).isEqualTo(verificationTokenString);
        assertThat(actual.isInvalidated()).isTrue();
    }
}
