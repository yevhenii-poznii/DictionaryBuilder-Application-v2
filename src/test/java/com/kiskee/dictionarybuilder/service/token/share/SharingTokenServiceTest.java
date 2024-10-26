package com.kiskee.dictionarybuilder.service.token.share;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kiskee.dictionarybuilder.exception.DuplicateResourceException;
import com.kiskee.dictionarybuilder.model.dto.token.share.SharingTokenData;
import com.kiskee.dictionarybuilder.model.entity.token.SharingToken;
import com.kiskee.dictionarybuilder.model.entity.user.UserVocabularyApplication;
import com.kiskee.dictionarybuilder.repository.token.TokenRepository;
import com.kiskee.dictionarybuilder.service.security.token.serializer.TokenSerializer;
import com.kiskee.dictionarybuilder.service.time.CurrentDateTimeService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
public class SharingTokenServiceTest {

    @InjectMocks
    private SharingTokenService service;

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private TokenSerializer<SharingTokenData, String> tokenSerializer;

    @Mock
    private CurrentDateTimeService currentDateTimeService;

    @Mock
    private SecurityContext securityContext;

    @Captor
    private ArgumentCaptor<SharingToken> sharingTokenArgumentCaptor;

    private static final UUID USER_ID = UUID.fromString("36effc62-d93a-4451-9f7b-7cf82de0d326");

    @Test
    void testPersistToken_WhenGiveTokenData_ThenReturnToken() {
        SharingTokenData sharingTokenData = mock(SharingTokenData.class);
        when(sharingTokenData.getUserId()).thenReturn(USER_ID);

        String tokenString = "sharingTokenString";
        when(tokenSerializer.serialize(sharingTokenData)).thenReturn(tokenString);
        when(tokenRepository.save(sharingTokenArgumentCaptor.capture())).thenReturn(mock(SharingToken.class));

        String result = service.persistToken(sharingTokenData);

        verify(tokenRepository).existsByToken(tokenString);

        SharingToken sharingToken = sharingTokenArgumentCaptor.getValue();
        assertThat(result).isEqualTo(sharingToken.getToken());
        assertThat(sharingToken.getUserId()).isEqualTo(USER_ID);
    }

    @Test
    void testPersistToken_WhenTokenAlreadyExists_ThenThrowDuplicateResourceException() {
        setAuth();

        SharingTokenData sharingTokenData = mock(SharingTokenData.class);

        String tokenString = "sharingTokenString";
        when(tokenSerializer.serialize(sharingTokenData)).thenReturn(tokenString);
        when(tokenRepository.existsByToken(tokenString)).thenReturn(true);

        assertThatThrownBy(() -> service.persistToken(sharingTokenData))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("SharingToken already exists to specified date");
    }

    @Test
    void testInvalidateToken_WhenTokenExists_ThenInvalidateToken() {
        String tokenString = "sharingTokenString";
        when(tokenRepository.invalidateToken(tokenString)).thenReturn(1);

        service.invalidateToken(tokenString);
    }

    @Test
    void testInvalidateToken_WhenTokenDoesNotExist_ThenInvalidateToken() {
        String tokenString = "sharingTokenString";
        when(tokenRepository.invalidateToken(tokenString)).thenReturn(0);

        service.invalidateToken(tokenString);
    }

    @Test
    void testIsNotInvalidated_WhenTokenIsNotInvalidated_ThenReturnTrue() {
        String tokenString = "sharingTokenString";
        when(tokenRepository.existsByTokenAndIsInvalidatedFalse(tokenString)).thenReturn(true);

        assertThat(service.isNotInvalidated(tokenString)).isTrue();
    }

    @Test
    void testIsNotInvalidated_WhenTokenIsInvalidated_ThenReturnTrue() {
        String tokenString = "sharingTokenString";
        when(tokenRepository.existsByTokenAndIsInvalidatedFalse(tokenString)).thenReturn(false);

        assertThat(service.isNotInvalidated(tokenString)).isFalse();
    }

    @Test
    void testGetSupportedTokenDataClass_ThenReturnSharingTokenDataClass() {
        assertThat(service.getSupportedTokenDataClass()).isEqualTo(SharingTokenData.class);
    }

    @Test
    void testGetTokenSerializer_ThenReturnTokenSerializer() {
        assertThat(service.getTokenSerializer()).isEqualTo(tokenSerializer);
    }

    @Test
    void testGetCurrentDateTimeService_ThenReturnCurrentDateTimeService() {
        assertThat(service.getCurrentDateTimeService()).isEqualTo(currentDateTimeService);
    }

    private void setAuth() {
        UserVocabularyApplication user = UserVocabularyApplication.builder()
                .setId(USER_ID)
                .setUsername("username")
                .build();
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(user, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }
}
