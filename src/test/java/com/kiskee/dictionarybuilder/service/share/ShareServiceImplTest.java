package com.kiskee.dictionarybuilder.service.share;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kiskee.dictionarybuilder.enums.vocabulary.filter.PageFilter;
import com.kiskee.dictionarybuilder.exception.DuplicateResourceException;
import com.kiskee.dictionarybuilder.exception.ResourceNotFoundException;
import com.kiskee.dictionarybuilder.exception.token.InvalidTokenException;
import com.kiskee.dictionarybuilder.model.dto.share.ShareDictionaryRequest;
import com.kiskee.dictionarybuilder.model.dto.share.SharedDictionaryDto;
import com.kiskee.dictionarybuilder.model.dto.share.SharedDictionaryPage;
import com.kiskee.dictionarybuilder.model.dto.token.share.SharingTokenData;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.dictionary.DictionaryDto;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.dictionary.page.DictionaryPageRequestDto;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.dictionary.page.DictionaryPageResponseDto;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.word.WordDto;
import com.kiskee.dictionarybuilder.model.entity.user.UserVocabularyApplication;
import com.kiskee.dictionarybuilder.service.security.token.deserializer.TokenDeserializationHandler;
import com.kiskee.dictionarybuilder.service.token.TokenPersistenceService;
import com.kiskee.dictionarybuilder.service.vocabulary.dictionary.DictionaryAccessValidator;
import com.kiskee.dictionarybuilder.service.vocabulary.loader.factory.WordLoaderFactory;
import com.kiskee.dictionarybuilder.service.vocabulary.word.page.DictionaryPageLoader;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
public class ShareServiceImplTest {

    @InjectMocks
    private ShareServiceImpl shareService;

    @Mock
    private TokenPersistenceService<SharingTokenData> tokenPersistenceService;

    @Mock
    private TokenDeserializationHandler<SharingTokenData> tokenDeserializationHandler;

    @Mock
    private DictionaryAccessValidator dictionaryAccessValidator;

    @Mock
    private WordLoaderFactory<PageFilter, DictionaryPageLoader> dictionaryPageLoaderFactory;

    @Mock
    private SecurityContext securityContext;

    private static final UUID USER_ID = UUID.fromString("78c87bb3-01b6-41ca-8329-247a72162868");

    @Test
    void testGetSharedDictionaryPage_WhenGivenValidSharingTokenString_ThenReturnSharedDictionaryPage() {
        String sharingToken = "sharingToken";
        DictionaryPageRequestDto dictionaryPageRequestDto = new DictionaryPageRequestDto(0, 100, null);

        SharingTokenData sharingTokenData = mock(SharingTokenData.class);
        long dictionaryId = 10L;
        String dictionaryName = "dictionaryName";
        when(sharingTokenData.getDictionaryId()).thenReturn(dictionaryId);
        when(sharingTokenData.getUserId()).thenReturn(USER_ID);
        when(tokenDeserializationHandler.deserializeToken(sharingToken, SharingTokenData.class))
                .thenReturn(sharingTokenData);

        DictionaryDto dictionaryDto = new DictionaryDto(10L, dictionaryName);
        when(dictionaryAccessValidator.getDictionaryByIdAndUserId(dictionaryId, USER_ID))
                .thenReturn(dictionaryDto);

        DictionaryPageLoader dictionaryPageLoader = mock(DictionaryPageLoader.class);
        when(dictionaryPageLoaderFactory.getLoader(any(PageFilter.class))).thenReturn(dictionaryPageLoader);

        List<WordDto> words =
                List.of(new WordDto(1L, "word", false, null, 1, null), new WordDto(2L, "word2", false, null, 1, null));
        DictionaryPageResponseDto dictionaryPageResponse = new DictionaryPageResponseDto(words, 2, 102);
        when(dictionaryPageLoader.loadWords(eq(dictionaryId), any())).thenReturn(dictionaryPageResponse);

        SharedDictionaryPage result = shareService.getSharedDictionaryPage(sharingToken, dictionaryPageRequestDto);

        assertThat(result.dictionaryName()).isEqualTo(dictionaryName);
        assertThat(result.dictionaryPage().getWords())
                .extracting(WordDto::getWord)
                .containsExactly("word", "word2");
    }

    @Test
    void testGetSharedDictionaryPage_WhenGivenExpiredSharingToken_ThenThrowInvalidTokenException() {
        String sharingToken = "sharingToken";
        DictionaryPageRequestDto dictionaryPageRequestDto = mock(DictionaryPageRequestDto.class);

        when(tokenDeserializationHandler.deserializeToken(sharingToken, SharingTokenData.class))
                .thenThrow(new InvalidTokenException("Token has expired"));

        assertThatExceptionOfType(InvalidTokenException.class)
                .isThrownBy(() -> shareService.getSharedDictionaryPage(sharingToken, dictionaryPageRequestDto))
                .withMessage("Token has expired");
    }

    @Test
    void testGetSharedDictionaryPage_WhenGivenTokenWithInvalidTokenType_ThenThrowInvalidTokenException() {
        String sharingToken = "sharingToken";
        DictionaryPageRequestDto dictionaryPageRequestDto = mock(DictionaryPageRequestDto.class);

        when(tokenDeserializationHandler.deserializeToken(sharingToken, SharingTokenData.class))
                .thenThrow(new InvalidTokenException("Invalid token type"));

        assertThatExceptionOfType(InvalidTokenException.class)
                .isThrownBy(() -> shareService.getSharedDictionaryPage(sharingToken, dictionaryPageRequestDto))
                .withMessage("Invalid token type");
    }

    @Test
    void testGetSharedDictionaryPage_WhenGivenInvalidToken_ThenThrowInvalidTokenException() {
        String sharingToken = "sharingToken";
        DictionaryPageRequestDto dictionaryPageRequestDto = mock(DictionaryPageRequestDto.class);

        when(tokenDeserializationHandler.deserializeToken(sharingToken, SharingTokenData.class))
                .thenThrow(new InvalidTokenException("Invalid token"));

        assertThatExceptionOfType(InvalidTokenException.class)
                .isThrownBy(() -> shareService.getSharedDictionaryPage(sharingToken, dictionaryPageRequestDto))
                .withMessage("Invalid token");
    }

    @Test
    void testGetSharedDictionaryPage_WhenDictionaryDoesNotExist_ThenThrowResourceNotFound() {
        String sharingToken = "sharingToken";
        DictionaryPageRequestDto dictionaryPageRequestDto = new DictionaryPageRequestDto(0, 100, null);

        SharingTokenData sharingTokenData = mock(SharingTokenData.class);
        long dictionaryId = 10L;
        when(sharingTokenData.getDictionaryId()).thenReturn(dictionaryId);
        when(sharingTokenData.getUserId()).thenReturn(USER_ID);
        when(tokenDeserializationHandler.deserializeToken(sharingToken, SharingTokenData.class))
                .thenReturn(sharingTokenData);

        when(dictionaryAccessValidator.getDictionaryByIdAndUserId(dictionaryId, USER_ID))
                .thenThrow(new ResourceNotFoundException(
                        String.format("Dictionary [%s] hasn't been found", dictionaryId)));

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> shareService.getSharedDictionaryPage(sharingToken, dictionaryPageRequestDto))
                .withMessage(String.format("Dictionary [%s] hasn't been found", dictionaryId));
    }

    @Test
    void testShareDictionary_WhenDictionaryExistsForUser_ThenReturnSharingToken() {
        setAuth();

        long dictionaryId = 10L;
        Instant shareToDate = Instant.parse("2024-10-25T14:00:00Z");
        ShareDictionaryRequest request = new ShareDictionaryRequest(dictionaryId, shareToDate);

        String sharingToken = "sharingToken";
        when(tokenPersistenceService.persistToken(any(SharingTokenData.class))).thenReturn(sharingToken);

        SharedDictionaryDto sharedDictionaryDto = shareService.shareDictionary(request);

        assertThat(sharedDictionaryDto.dictionaryId()).isEqualTo(dictionaryId);
        assertThat(sharedDictionaryDto.sharingToken()).isEqualTo(sharingToken);
        assertThat(sharedDictionaryDto.validToDate()).isEqualTo(shareToDate);

        verify(dictionaryAccessValidator).verifyUserHasDictionary(dictionaryId);
    }

    @Test
    void testShareDictionary_WhenDictionaryDoesNotExistForUser_ThenThrowResourceNotFoundException() {
        long dictionaryId = 10L;
        Instant shareToDate = Instant.parse("2024-10-25T14:00:00Z");
        ShareDictionaryRequest request = new ShareDictionaryRequest(dictionaryId, shareToDate);

        doThrow(new ResourceNotFoundException(String.format("Dictionary [%s] hasn't been found", dictionaryId)))
                .when(dictionaryAccessValidator)
                .verifyUserHasDictionary(dictionaryId);

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> shareService.shareDictionary(request))
                .withMessage(String.format("Dictionary [%s] hasn't been found", dictionaryId));
    }

    @Test
    void testShareDictionary_WhenSharingTokenAlreadyExists_ThenThrowDuplicateResourceException() {
        setAuth();

        long dictionaryId = 10L;
        Instant shareToDate = Instant.parse("2024-10-25T14:00:00Z");
        ShareDictionaryRequest request = new ShareDictionaryRequest(dictionaryId, shareToDate);

        when(tokenPersistenceService.persistToken(any(SharingTokenData.class)))
                .thenThrow(new DuplicateResourceException("SharingToken already exists to specified date"));

        assertThatExceptionOfType(DuplicateResourceException.class)
                .isThrownBy(() -> shareService.shareDictionary(request))
                .withMessage("SharingToken already exists to specified date");

        verify(dictionaryAccessValidator).verifyUserHasDictionary(dictionaryId);
    }

    @Test
    void getTokenPersistenceService() {
        assertThat(shareService.getSharingTokenIssuer()).isEqualTo(tokenPersistenceService);
    }

    @Test
    void getTokenDeserializationHandler() {
        assertThat(shareService.getTokenDeserializationHandler()).isEqualTo(tokenDeserializationHandler);
    }

    @Test
    void getDictionaryAccessValidator() {
        assertThat(shareService.getDictionaryAccessValidator()).isEqualTo(dictionaryAccessValidator);
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
