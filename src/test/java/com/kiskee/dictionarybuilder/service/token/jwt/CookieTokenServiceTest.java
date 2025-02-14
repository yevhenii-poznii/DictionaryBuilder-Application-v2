package com.kiskee.dictionarybuilder.service.token.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kiskee.dictionarybuilder.enums.ExceptionStatusesEnum;
import com.kiskee.dictionarybuilder.enums.token.TokenType;
import com.kiskee.dictionarybuilder.exception.ResourceNotFoundException;
import com.kiskee.dictionarybuilder.model.dto.token.jwe.JweToken;
import com.kiskee.dictionarybuilder.model.dto.token.jwe.JweTokenData;
import com.kiskee.dictionarybuilder.model.entity.token.CookieToken;
import com.kiskee.dictionarybuilder.model.entity.token.Token;
import com.kiskee.dictionarybuilder.repository.token.TokenRepository;
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
    void testPersistToken_WhenJweTokenParamIsProvided_ThenGenerateCookieTokenAndSave() {
        JweToken jweToken = JweToken.builder()
                .setUserId(USER_ID)
                .setSubject("username")
                .setAuthorities(List.of())
                .setCreatedAt(Instant.parse("2024-01-30T12:00:00Z"))
                .setExpiresAt(Instant.parse("2024-01-31T12:00:00Z"))
                .build();
        String generatedCookieTokenString = "some_cookie_token_string";
        JweTokenData tokenData = new JweTokenData(generatedCookieTokenString, jweToken);

        when(tokenRepository.save(cookieTokenArgumentCaptor.capture())).thenReturn(mock(CookieToken.class));

        String cookieToken = service.persistToken(tokenData);

        verify(tokenRepository).save(any(CookieToken.class));

        CookieToken actualToken = cookieTokenArgumentCaptor.getValue();

        assertThat(cookieToken).isEqualTo(actualToken.getToken());
        assertThat(USER_ID).isEqualTo(actualToken.getUserId());
        assertThat(TokenType.JWT_REFRESH_TOKEN.getValue()).isEqualTo(actualToken.getDiscriminatorValue());
    }

    @Test
    void testFindTokenOrThrow_WhenCookieTokenExists_ThenReturnCookieToken() {
        String cookieTokenParam = "cookieTokenString";

        CookieToken foundToken = mock(CookieToken.class);
        when(foundToken.getToken()).thenReturn(cookieTokenParam);
        when(tokenRepository.findByToken(cookieTokenParam)).thenReturn(Optional.of(foundToken));

        CookieToken result = service.findTokenOrThrow(cookieTokenParam);

        verify(tokenRepository).findByToken(cookieTokenParam);

        assertThat(result.getToken()).isEqualTo(cookieTokenParam);
    }

    @Test
    void testFindTokenOrThrow_WhenCookieTokenDoesNotExist_ThenThrowResourceNotFoundException() {
        String cookieTokenParam = "cookieTokenString";

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
        String cookieTokenString = "cookieTokenString";

        service.invalidateToken(cookieTokenString);

        verify(tokenRepository).invalidateToken(cookieTokenString);
    }

    @Test
    void testIsNotInvalidated_WhenTokenIsNotInvalidated_ThenReturnTrue() {
        String cookieTokenString = "cookieTokenString";
        when(tokenRepository.existsByTokenAndIsInvalidatedFalse(cookieTokenString))
                .thenReturn(true);

        assertThat(service.isNotInvalidated(cookieTokenString)).isTrue();
    }

    @Test
    void testIsNotInvalidated_WhenTokenIsInvalidated_ThenReturnTrue() {
        String cookieTokenString = "cookieTokenString";
        when(tokenRepository.existsByTokenAndIsInvalidatedFalse(cookieTokenString))
                .thenReturn(false);

        assertThat(service.isNotInvalidated(cookieTokenString)).isFalse();
    }

    @Test
    void testGetSupportedTokenDataClass_ThenReturnJweTokenClass() {
        assertThat(service.getSupportedTokenDataClass()).isEqualTo(JweToken.class);
    }
}
