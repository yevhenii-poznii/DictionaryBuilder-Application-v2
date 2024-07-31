package com.kiskee.dictionarybuilder.service.vocabulary.dictionary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.kiskee.dictionarybuilder.enums.ExceptionStatusesEnum;
import com.kiskee.dictionarybuilder.enums.user.UserRole;
import com.kiskee.dictionarybuilder.enums.vocabulary.PageFilter;
import com.kiskee.dictionarybuilder.enums.vocabulary.VocabularyResponseMessageEnum;
import com.kiskee.dictionarybuilder.exception.DuplicateResourceException;
import com.kiskee.dictionarybuilder.exception.ResourceNotFoundException;
import com.kiskee.dictionarybuilder.mapper.dictionary.DictionaryMapper;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.dictionary.DictionaryDetailDto;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.dictionary.DictionaryDto;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.dictionary.DictionarySaveRequest;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.dictionary.DictionarySaveResponse;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.dictionary.page.DictionaryPageRequestDto;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.dictionary.page.DictionaryPageResponseDto;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.word.WordDto;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.word.WordTranslationDto;
import com.kiskee.dictionarybuilder.model.entity.user.UserVocabularyApplication;
import com.kiskee.dictionarybuilder.model.entity.vocabulary.Dictionary;
import com.kiskee.dictionarybuilder.repository.vocabulary.DictionaryRepository;
import com.kiskee.dictionarybuilder.service.vocabulary.dictionary.page.DictionaryPageLoaderFactory;
import com.kiskee.dictionarybuilder.service.vocabulary.word.page.DictionaryPageLoader;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
public class DictionaryServiceImplTest {

    @InjectMocks
    private DictionaryServiceImpl dictionaryService;

    @Mock
    private DictionaryRepository dictionaryRepository;

    @Mock
    private DictionaryMapper dictionaryMapper;

    @Mock
    private DictionaryPageLoaderFactory dictionaryPageLoaderFactory;

    @Mock
    private SecurityContext securityContext;

    @Captor
    private ArgumentCaptor<Dictionary> dictionaryArgumentCaptor;

    private static final UUID USER_ID = UUID.fromString("78c87bb3-01b6-41ca-8329-247a72162868");

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void
            testAddDictionaryWithStringDictionaryNameParam_WhenDictionaryNameIsGivenWhileProvisioning_ThenCreateEmptyDictionary() {
        String dictionaryName = "Default Dictionary";

        dictionaryService.addDictionary(dictionaryName);

        verify(dictionaryRepository).existsByDictionaryNameEqualsIgnoreCaseAndUserProfileId(dictionaryName, null);
        verify(dictionaryRepository).save(dictionaryArgumentCaptor.capture());

        Dictionary actual = dictionaryArgumentCaptor.getValue();
        assertThat(actual.getDictionaryName()).isEqualTo(dictionaryName);
        assertThat(actual.getWords()).isEqualTo(Collections.emptyList());
    }

    @Test
    void testAddDictionaryWithDictionarySaveRequestParam_WhenGivenDictionarySaveRequest_ThenCreateEmptyDictionary() {
        DictionarySaveRequest saveRequest = new DictionarySaveRequest("dictionaryName");

        UserVocabularyApplication user = new UserVocabularyApplication(
                USER_ID, "email", "username", "noPassword", true, UserRole.ROLE_USER, null, null);
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(user, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(dictionaryRepository.existsByDictionaryNameEqualsIgnoreCaseAndUserProfileId(
                        saveRequest.getDictionaryName(), USER_ID))
                .thenReturn(false);

        Dictionary savedDictionary = Dictionary.builder()
                .id(1L)
                .dictionaryName(saveRequest.getDictionaryName())
                .userProfileId(USER_ID)
                .build();
        when(dictionaryRepository.save(dictionaryArgumentCaptor.capture())).thenReturn(savedDictionary);

        DictionaryDetailDto dictionaryDetailDto = new DictionaryDetailDto(1L, saveRequest.getDictionaryName(), 0, 0, 0);
        when(dictionaryMapper.toDto(savedDictionary)).thenReturn(dictionaryDetailDto);

        DictionarySaveResponse result = dictionaryService.addDictionary(saveRequest);

        Dictionary actual = dictionaryArgumentCaptor.getValue();
        assertThat(actual.getDictionaryName()).isEqualTo(result.getDictionary().getDictionaryName());
        assertThat(actual.getWords()).isEmpty();
        assertThat(result.getResponseMessage())
                .isEqualTo(
                        VocabularyResponseMessageEnum.DICTIONARY_CREATED.getResponseMessage(),
                        saveRequest.getDictionaryName());
        assertThat(result.getDictionary().getWordCount()).isZero();
    }

    @Test
    void
            testAddDictionaryWithStringDictionaryNameParam_WhenDictionaryWithTheSameNameAlreadyExistForUser_ThenThrowDuplicateResourceException() {
        String dictionaryName = "Default Dictionary";

        when(dictionaryRepository.existsByDictionaryNameEqualsIgnoreCaseAndUserProfileId(dictionaryName, null))
                .thenReturn(true);

        assertThatExceptionOfType(DuplicateResourceException.class)
                .isThrownBy(() -> dictionaryService.addDictionary(dictionaryName))
                .withMessage(String.format(
                        VocabularyResponseMessageEnum.DICTIONARY_ALREADY_EXISTS.getResponseMessage(), dictionaryName));

        verifyNoMoreInteractions(dictionaryRepository);
        verifyNoInteractions(dictionaryMapper);
    }

    @Test
    void
            testAddDictionaryWithDictionarySaveRequestParam_WhenDictionaryWithTheSameNameAlreadyExistForUser_ThenThrowDuplicateResourceException() {
        DictionarySaveRequest saveRequest = new DictionarySaveRequest("dictionaryName");

        UserVocabularyApplication user = new UserVocabularyApplication(
                USER_ID, "email", "username", "noPassword", true, UserRole.ROLE_USER, null, null);
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(user, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(dictionaryRepository.existsByDictionaryNameEqualsIgnoreCaseAndUserProfileId(
                        saveRequest.getDictionaryName(), USER_ID))
                .thenReturn(true);

        assertThatExceptionOfType(DuplicateResourceException.class)
                .isThrownBy(() -> dictionaryService.addDictionary(saveRequest))
                .withMessage(String.format(
                        VocabularyResponseMessageEnum.DICTIONARY_ALREADY_EXISTS.getResponseMessage(),
                        saveRequest.getDictionaryName()));

        verifyNoMoreInteractions(dictionaryRepository);
        verifyNoInteractions(dictionaryMapper);
    }

    @Test
    void testGetDictionaryPageByOwner_WhenGivenExistingDictionaryIdForUser_ThenReturnDictionaryPage() {
        Long dictionaryId = 1L;
        DictionaryPageRequestDto pageRequest = new DictionaryPageRequestDto(0, 100, PageFilter.BY_ADDED_AT_ASC);

        UserVocabularyApplication user = new UserVocabularyApplication(
                USER_ID, "email", "username", "noPassword", true, UserRole.ROLE_USER, null, null);
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(user, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        DictionaryPageResponseDto response = returnDictionaryPageResponseDto();

        when(dictionaryRepository.existsByIdAndUserProfileId(dictionaryId, USER_ID))
                .thenReturn(true);

        DictionaryPageLoader loader = mock(DictionaryPageLoader.class);
        when(dictionaryPageLoaderFactory.getLoader(pageRequest.getFilter())).thenReturn(loader);
        when(loader.loadDictionaryPage(eq(dictionaryId), any(PageRequest.class)))
                .thenReturn(response);

        DictionaryPageResponseDto result = dictionaryService.getDictionaryPageByOwner(dictionaryId, pageRequest);

        assertThat(result.getWords()).extracting(WordDto::getId).containsExactly(1L, 2L);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getTotalPages()).isEqualTo(0);
    }

    @Test
    void testGetDictionaryPageByOwner_WhenGivenDictionaryIdDoesNotExistsForUser_ThenThrowResourceNotFoundException() {
        Long dictionaryId = 1L;
        DictionaryPageRequestDto pageRequest = new DictionaryPageRequestDto(0, 100, PageFilter.BY_ADDED_AT_ASC);

        UserVocabularyApplication user = new UserVocabularyApplication(
                USER_ID, "email", "username", "noPassword", true, UserRole.ROLE_USER, null, null);
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(user, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(dictionaryRepository.existsByIdAndUserProfileId(dictionaryId, USER_ID))
                .thenReturn(false);

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> dictionaryService.getDictionaryPageByOwner(dictionaryId, pageRequest))
                .withMessage(String.format(
                        ExceptionStatusesEnum.RESOURCE_NOT_FOUND.getStatus(),
                        Dictionary.class.getSimpleName(),
                        dictionaryId));

        verifyNoInteractions(dictionaryPageLoaderFactory);
    }

    @ParameterizedTest
    @MethodSource("invalidDictionaryPageRequestDto")
    void testGetDictionaryPageByOwner_WhenGivenDictionaryPageRequestDtoHasNullFields_ThenReturnDictionaryPage(
            DictionaryPageRequestDto dictionaryPageRequest) {
        Long dictionaryId = 1L;

        UserVocabularyApplication user = new UserVocabularyApplication(
                USER_ID, "email", "username", "noPassword", true, UserRole.ROLE_USER, null, null);
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(user, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        DictionaryPageResponseDto response = returnDictionaryPageResponseDto();

        when(dictionaryRepository.existsByIdAndUserProfileId(dictionaryId, USER_ID))
                .thenReturn(true);

        DictionaryPageLoader loader = mock(DictionaryPageLoader.class);
        when(dictionaryPageLoaderFactory.getLoader(any(PageFilter.class))).thenReturn(loader);
        when(loader.loadDictionaryPage(eq(dictionaryId), any(PageRequest.class)))
                .thenReturn(response);

        DictionaryPageResponseDto result =
                dictionaryService.getDictionaryPageByOwner(dictionaryId, dictionaryPageRequest);

        assertThat(result.getWords()).extracting(WordDto::getId).containsExactly(1L, 2L);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getTotalPages()).isEqualTo(0);
    }

    @Test
    void testGetDetailedDictionaries_WhenUserHasTwoDictionaries_ThenReturnDetailedDictionaries() {
        UserVocabularyApplication user = new UserVocabularyApplication(
                USER_ID, "email", "username", "noPassword", true, UserRole.ROLE_USER, null, null);
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(user, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        List<DictionaryDetailDto> dictionaries = List.of(
                new DictionaryDetailDto(1L, "Default Dictionary", 0, 0, 0),
                new DictionaryDetailDto(2L, "Dictionary 1", 123, 23, 100));

        when(dictionaryRepository.findDetailedDictionariesByUserProfileId(USER_ID))
                .thenReturn(dictionaries);

        List<DictionaryDetailDto> result = dictionaryService.getDetailedDictionaries();

        assertThat(result.size()).isEqualTo(2);
        assertThat(result).extracting(DictionaryDetailDto::getId).containsExactly(1L, 2L);
        assertThat(result)
                .extracting(DictionaryDetailDto::getDictionaryName)
                .containsExactly("Default Dictionary", "Dictionary 1");
        assertThat(result).extracting(DictionaryDetailDto::getWordCount).containsExactly(0, 123);
    }

    @Test
    void testGetDetailedDictionaries_WhenUserHasNoDictionaries_ThenReturnEmptyList() {
        UserVocabularyApplication user = new UserVocabularyApplication(
                USER_ID, "email", "username", "noPassword", true, UserRole.ROLE_USER, null, null);
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(user, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(dictionaryRepository.findDetailedDictionariesByUserProfileId(USER_ID))
                .thenReturn(Collections.emptyList());

        List<DictionaryDetailDto> result = dictionaryService.getDetailedDictionaries();

        assertThat(result).isEmpty();
    }

    @Test
    void testGetDictionaries_WhenUserHasTwoDictionaries_ThenReturnDetailedDictionaries() {
        UserVocabularyApplication user = new UserVocabularyApplication(
                USER_ID, "email", "username", "noPassword", true, UserRole.ROLE_USER, null, null);
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(user, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        List<DictionaryDto> dictionaries =
                List.of(new DictionaryDto(1L, "Default Dictionary"), new DictionaryDto(2L, "Dictionary 1"));

        when(dictionaryRepository.findAllByUserProfileId(USER_ID)).thenReturn(dictionaries);

        List<DictionaryDto> result = dictionaryService.getDictionaries();

        assertThat(result.size()).isEqualTo(2);
        assertThat(result).extracting(DictionaryDto::getId).containsExactly(1L, 2L);
        assertThat(result)
                .extracting(DictionaryDto::getDictionaryName)
                .containsExactly("Default Dictionary", "Dictionary 1");
    }

    @Test
    void testGetDictionaries_WhenUserHasNoDictionaries_ThenReturnEmptyList() {
        UserVocabularyApplication user = new UserVocabularyApplication(
                USER_ID, "email", "username", "noPassword", true, UserRole.ROLE_USER, null, null);
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(user, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(dictionaryRepository.findAllByUserProfileId(USER_ID)).thenReturn(Collections.emptyList());

        List<DictionaryDto> result = dictionaryService.getDictionaries();

        assertThat(result).isEmpty();
    }

    @Test
    void testUpdateDictionary_WhenGivenDictionaryNameAlreadyExistsForUser_ThenThrowDuplicateResourceException() {
        UserVocabularyApplication user = new UserVocabularyApplication(
                USER_ID, "email", "username", "noPassword", true, UserRole.ROLE_USER, null, null);
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(user, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        Long dictionaryId = 1L;
        DictionarySaveRequest dictionarySaveRequest = new DictionarySaveRequest("dictionaryName");

        when(dictionaryRepository.existsByDictionaryNameEqualsIgnoreCaseAndUserProfileId(
                        dictionarySaveRequest.getDictionaryName(), USER_ID))
                .thenReturn(true);

        assertThatExceptionOfType(DuplicateResourceException.class)
                .isThrownBy(() -> dictionaryService.updateDictionary(dictionaryId, dictionarySaveRequest))
                .withMessage(String.format(
                        VocabularyResponseMessageEnum.DICTIONARY_ALREADY_EXISTS.getResponseMessage(),
                        dictionarySaveRequest.getDictionaryName()));

        verifyNoMoreInteractions(dictionaryRepository);
        verifyNoInteractions(dictionaryMapper);
    }

    @Test
    void testUpdateDictionary_WhenGivenDictionaryIdDoesNotExistForUser_ThenThrowResourceNotFoundException() {
        UserVocabularyApplication user = new UserVocabularyApplication(
                USER_ID, "email", "username", "noPassword", true, UserRole.ROLE_USER, null, null);
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(user, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        Long dictionaryId = 1L;
        DictionarySaveRequest dictionarySaveRequest = new DictionarySaveRequest("dictionaryName");

        when(dictionaryRepository.existsByDictionaryNameEqualsIgnoreCaseAndUserProfileId(
                        dictionarySaveRequest.getDictionaryName(), USER_ID))
                .thenReturn(false);
        when(dictionaryRepository.getUserDictionary(dictionaryId, USER_ID))
                .thenThrow(new ResourceNotFoundException(String.format(
                        ExceptionStatusesEnum.RESOURCE_NOT_FOUND.getStatus(),
                        Dictionary.class.getSimpleName(),
                        dictionaryId)));

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> dictionaryService.updateDictionary(dictionaryId, dictionarySaveRequest))
                .withMessage(String.format(
                        ExceptionStatusesEnum.RESOURCE_NOT_FOUND.getStatus(),
                        Dictionary.class.getSimpleName(),
                        dictionaryId));

        verifyNoMoreInteractions(dictionaryRepository);
        verifyNoInteractions(dictionaryMapper);
    }

    @Test
    void
            testUpdateDictionary_WhenGivenValidDictionaryNameAndDictionaryIdExistsForUser_ThenReturnDictionarySaveResponse() {
        UserVocabularyApplication user = new UserVocabularyApplication(
                USER_ID, "email", "username", "noPassword", true, UserRole.ROLE_USER, null, null);
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(user, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        Long dictionaryId = 1L;
        DictionarySaveRequest dictionarySaveRequest = new DictionarySaveRequest("dictionaryName");

        when(dictionaryRepository.existsByDictionaryNameEqualsIgnoreCaseAndUserProfileId(
                        dictionarySaveRequest.getDictionaryName(), USER_ID))
                .thenReturn(false);

        Dictionary dictionaryToUpdate = Dictionary.builder()
                .id(dictionaryId)
                .dictionaryName("dictionaryName")
                .words(List.of())
                .userProfileId(USER_ID)
                .build();

        when(dictionaryRepository.getUserDictionary(dictionaryId, USER_ID)).thenReturn(dictionaryToUpdate);

        when(dictionaryRepository.save(dictionaryArgumentCaptor.capture())).thenReturn(dictionaryToUpdate);
        when(dictionaryMapper.toDto(dictionaryToUpdate))
                .thenReturn(new DictionaryDetailDto(dictionaryId, "dictionaryName", 0, 0, 0));

        dictionaryService.updateDictionary(dictionaryId, dictionarySaveRequest);

        Dictionary actual = dictionaryArgumentCaptor.getValue();
        assertThat(actual.getDictionaryName()).isEqualTo(dictionarySaveRequest.getDictionaryName());
        assertThat(actual.getWordCount()).isEqualTo(0);
        assertThat(actual.getId()).isEqualTo(1L);
    }

    @Test
    void testDeleteDictionaries_WhenGivenDictionaryIdsExistsForUser_ThenDeleteDictionaries() {
        UserVocabularyApplication user = new UserVocabularyApplication(
                USER_ID, "email", "username", "noPassword", true, UserRole.ROLE_USER, null, null);
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(user, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        Set<Long> dictionaryIds = Set.of(1L, 2L);

        List<Long> existingIdsForUser = List.of(1L, 2L);
        when(dictionaryRepository.findIdsByUserProfileIdAndIdIn(USER_ID, dictionaryIds))
                .thenReturn(existingIdsForUser);

        doNothing().when(dictionaryRepository).deleteAllById(existingIdsForUser);

        dictionaryService.deleteDictionaries(dictionaryIds);
    }

    @Test
    void testDeleteDictionaries_WhenGivenDictionaryIdsDoNoExistForUser_ThenThrowResourceNotFoundException() {
        UserVocabularyApplication user = new UserVocabularyApplication(
                USER_ID, "email", "username", "noPassword", true, UserRole.ROLE_USER, null, null);
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(user, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        Set<Long> dictionaryIds = Set.of(1L, 2L);

        when(dictionaryRepository.findIdsByUserProfileIdAndIdIn(USER_ID, dictionaryIds))
                .thenReturn(List.of());

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> dictionaryService.deleteDictionaries(dictionaryIds))
                .withMessage(String.format(
                        ExceptionStatusesEnum.RESOURCES_NOT_FOUND.getStatus(), "Dictionaries", dictionaryIds));

        verifyNoMoreInteractions(dictionaryRepository);
    }

    @Test
    void testDeleteDictionary_WhenDictionaryExistsForUser_ThenDeleteDictionary() {
        UserVocabularyApplication user = new UserVocabularyApplication(
                USER_ID, "email", "username", "noPassword", true, UserRole.ROLE_USER, null, null);
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(user, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        Long dictionaryId = 1L;
        String dictionaryName = "Default Dictionary";

        Dictionary dictionary = mock(Dictionary.class);
        when(dictionary.getId()).thenReturn(dictionaryId);
        when(dictionary.getDictionaryName()).thenReturn(dictionaryName);

        when(dictionaryRepository.getUserDictionary(dictionaryId, USER_ID)).thenReturn(dictionary);

        doNothing().when(dictionaryRepository).delete(dictionaryArgumentCaptor.capture());

        dictionaryService.deleteDictionary(dictionaryId);

        Dictionary actual = dictionaryArgumentCaptor.getValue();
        assertThat(actual.getId()).isEqualTo(dictionaryId);
        assertThat(actual.getDictionaryName()).isEqualTo(dictionaryName);
    }

    @Test
    void testDeleteDictionary_WhenDictionaryDoesNotExistForUser_ThenThrowResourceNotFound() {
        UserVocabularyApplication user = new UserVocabularyApplication(
                USER_ID, "email", "username", "noPassword", true, UserRole.ROLE_USER, null, null);
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(user, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        Long dictionaryId = 1L;

        when(dictionaryRepository.getUserDictionary(dictionaryId, USER_ID))
                .thenThrow(new ResourceNotFoundException(String.format(
                        ExceptionStatusesEnum.RESOURCE_NOT_FOUND.getStatus(),
                        Dictionary.class.getSimpleName(),
                        dictionaryId)));

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> dictionaryService.deleteDictionary(dictionaryId))
                .withMessage(String.format(
                        ExceptionStatusesEnum.RESOURCE_NOT_FOUND.getStatus(),
                        Dictionary.class.getSimpleName(),
                        dictionaryId));

        verifyNoMoreInteractions(dictionaryRepository);
    }

    @Test
    void testVerifyUserHasDictionary_WhenUserHasDictionary_ThenDoNothing() {
        UserVocabularyApplication user = new UserVocabularyApplication(
                USER_ID, "email", "username", "noPassword", true, UserRole.ROLE_USER, null, null);
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(user, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        Long dictionaryId = 1L;

        when(dictionaryRepository.existsByIdAndUserProfileId(dictionaryId, USER_ID))
                .thenReturn(true);

        dictionaryService.verifyUserHasDictionary(dictionaryId);

        verify(dictionaryRepository).existsByIdAndUserProfileId(dictionaryId, USER_ID);
    }

    @Test
    void testVerifyUserHasDictionary_WhenUserDoesNotHaveDictionary_ThenThrowResourceNotFoundException() {
        UserVocabularyApplication user = new UserVocabularyApplication(
                USER_ID, "email", "username", "noPassword", true, UserRole.ROLE_USER, null, null);
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(user, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        Long dictionaryId = 1L;

        when(dictionaryRepository.existsByIdAndUserProfileId(dictionaryId, USER_ID))
                .thenReturn(false);

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> dictionaryService.verifyUserHasDictionary(dictionaryId))
                .withMessage(String.format(
                        ExceptionStatusesEnum.RESOURCE_NOT_FOUND.getStatus(),
                        Dictionary.class.getSimpleName(),
                        dictionaryId));
    }

    private static DictionaryPageResponseDto returnDictionaryPageResponseDto() {
        List<WordDto> words = List.of(
                new WordDto(
                        1L,
                        "word1",
                        true,
                        Set.of(new WordTranslationDto(1L, "translation1"), new WordTranslationDto(2L, "translation2")),
                        0,
                        "hint1"),
                new WordDto(
                        2L,
                        "word2",
                        false,
                        Set.of(new WordTranslationDto(3L, "translation3"), new WordTranslationDto(4L, "translation4")),
                        0,
                        "hint2"));

        return new DictionaryPageResponseDto(words, 0, 2);
    }

    static Stream<DictionaryPageRequestDto> invalidDictionaryPageRequestDto() {
        return Stream.of(
                new DictionaryPageRequestDto(null, 100, PageFilter.BY_ADDED_AT_ASC),
                new DictionaryPageRequestDto(0, null, PageFilter.BY_ADDED_AT_ASC),
                new DictionaryPageRequestDto(0, 20, null),
                new DictionaryPageRequestDto(null, null, null));
    }
}
