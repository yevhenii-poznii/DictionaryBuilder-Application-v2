package com.kiskee.vocabulary.service.vocabulary.dictionary;

import com.kiskee.vocabulary.enums.ExceptionStatusesEnum;
import com.kiskee.vocabulary.enums.vocabulary.VocabularyResponseMessageEnum;
import com.kiskee.vocabulary.exception.DuplicateResourceException;
import com.kiskee.vocabulary.exception.ResourceNotFoundException;
import com.kiskee.vocabulary.mapper.dictionary.DictionaryMapper;
import com.kiskee.vocabulary.model.dto.vocabulary.dictionary.DictionaryDto;
import com.kiskee.vocabulary.model.dto.vocabulary.dictionary.DictionarySaveRequest;
import com.kiskee.vocabulary.model.dto.vocabulary.dictionary.DictionarySaveResponse;
import com.kiskee.vocabulary.model.entity.user.UserVocabularyApplication;
import com.kiskee.vocabulary.model.entity.vocabulary.Dictionary;
import com.kiskee.vocabulary.repository.vocabulary.DictionaryRepository;
import org.junit.jupiter.api.AfterEach;
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

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DictionaryServiceImplTest {

    @InjectMocks
    private DictionaryServiceImpl dictionaryService;
    @Mock
    private DictionaryRepository dictionaryRepository;
    @Mock
    private DictionaryMapper dictionaryMapper;
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
    void testAddDictionaryWithStringDictionaryNameParam_WhenDictionaryNameIsGivenWhileProvisioning_ThenCreateEmptyDictionary() {
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

        UserVocabularyApplication user = new UserVocabularyApplication(USER_ID, "email", "username",
                "noPassword", true, null, null);
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(dictionaryRepository.existsByDictionaryNameEqualsIgnoreCaseAndUserProfileId(saveRequest.getDictionaryName(), USER_ID))
                .thenReturn(false);

        Dictionary savedDictionary = Dictionary.builder()
                .id(1L)
                .dictionaryName(saveRequest.getDictionaryName())
                .userProfileId(USER_ID)
                .build();
        when(dictionaryRepository.save(dictionaryArgumentCaptor.capture())).thenReturn(savedDictionary);

        DictionaryDto dictionaryDto = new DictionaryDto(1L, saveRequest.getDictionaryName(), 0);
        when(dictionaryMapper.toDto(savedDictionary)).thenReturn(dictionaryDto);

        DictionarySaveResponse result = dictionaryService.addDictionary(saveRequest);

        Dictionary actual = dictionaryArgumentCaptor.getValue();
        assertThat(actual.getDictionaryName()).isEqualTo(result.getDictionary().getDictionaryName());
        assertThat(actual.getWords()).isEmpty();
        assertThat(result.getResponseMessage())
                .isEqualTo(VocabularyResponseMessageEnum.DICTIONARY_CREATED.getResponseMessage(), saveRequest.getDictionaryName());
        assertThat(result.getDictionary().getWordCount()).isZero();
    }

    @Test
    void testAddDictionaryWithStringDictionaryNameParam_WhenDictionaryWithTheSameNameAlreadyExistForUser_ThenThrowDuplicateResourceException() {
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
    void testAddDictionaryWithDictionarySaveRequestParam_WhenDictionaryWithTheSameNameAlreadyExistForUser_ThenThrowDuplicateResourceException() {
        DictionarySaveRequest saveRequest = new DictionarySaveRequest("dictionaryName");

        UserVocabularyApplication user = new UserVocabularyApplication(USER_ID, "email", "username",
                "noPassword", true, null, null);
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(dictionaryRepository.existsByDictionaryNameEqualsIgnoreCaseAndUserProfileId(saveRequest.getDictionaryName(), USER_ID))
                .thenReturn(true);

        assertThatExceptionOfType(DuplicateResourceException.class)
                .isThrownBy(() -> dictionaryService.addDictionary(saveRequest))
                .withMessage(String.format(
                        VocabularyResponseMessageEnum.DICTIONARY_ALREADY_EXISTS.getResponseMessage(), saveRequest.getDictionaryName()));

        verifyNoMoreInteractions(dictionaryRepository);
        verifyNoInteractions(dictionaryMapper);
    }

    @Test
    void testGetDictionaries_WhenUserHasTwoDictionaries_Then() {
        UserVocabularyApplication user = new UserVocabularyApplication(USER_ID, "email", "username",
                "noPassword", true, null, null);
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        List<DictionaryDto> dictionaries = List.of(
                new DictionaryDto(1L, "Default Dictionary", 0),
                new DictionaryDto(2L, "Dictionary 1", 123));

        when(dictionaryRepository.findAllByUserProfileId(USER_ID)).thenReturn(dictionaries);

        List<DictionaryDto> result = dictionaryService.getDictionaries();

        assertThat(result.size()).isEqualTo(2);
        assertThat(result).extracting(DictionaryDto::getId)
                .containsExactly(1L, 2L);
        assertThat(result).extracting(DictionaryDto::getDictionaryName)
                .containsExactly("Default Dictionary", "Dictionary 1");
        assertThat(result).extracting(DictionaryDto::getWordCount)
                .containsExactly(0, 123);
    }

    @Test
    void testGetDictionaries_WhenUserHasNoDictionaries_ThenReturnEmptyList() {
        UserVocabularyApplication user = new UserVocabularyApplication(USER_ID, "email", "username",
                "noPassword", true, null, null);
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(dictionaryRepository.findAllByUserProfileId(USER_ID)).thenReturn(Collections.emptyList());

        List<DictionaryDto> result = dictionaryService.getDictionaries();

        assertThat(result).isEmpty();
    }

    @Test
    void testUpdateDictionary_WhenGivenDictionaryNameAlreadyExistsForUser_ThenThrowDuplicateResourceException() {
        UserVocabularyApplication user = new UserVocabularyApplication(USER_ID, "email", "username",
                "noPassword", true, null, null);
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        Long dictionaryId = 1L;
        DictionarySaveRequest dictionarySaveRequest = new DictionarySaveRequest("dictionaryName");

        when(dictionaryRepository.existsByDictionaryNameEqualsIgnoreCaseAndUserProfileId(dictionarySaveRequest.getDictionaryName(), USER_ID))
                .thenReturn(true);

        assertThatExceptionOfType(DuplicateResourceException.class)
                .isThrownBy(() -> dictionaryService.updateDictionary(dictionaryId, dictionarySaveRequest))
                .withMessage(String.format(
                        VocabularyResponseMessageEnum.DICTIONARY_ALREADY_EXISTS.getResponseMessage(), dictionarySaveRequest.getDictionaryName()));

        verifyNoMoreInteractions(dictionaryRepository);
        verifyNoInteractions(dictionaryMapper);
    }

    @Test
    void testUpdateDictionary_WhenGivenDictionaryIdDoesNotExistForUser_ThenThrowResourceNotFoundException() {
        UserVocabularyApplication user = new UserVocabularyApplication(USER_ID, "email", "username",
                "noPassword", true, null, null);
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        Long dictionaryId = 1L;
        DictionarySaveRequest dictionarySaveRequest = new DictionarySaveRequest("dictionaryName");

        when(dictionaryRepository.existsByDictionaryNameEqualsIgnoreCaseAndUserProfileId(dictionarySaveRequest.getDictionaryName(), USER_ID))
                .thenReturn(false);
        when(dictionaryRepository.getUserDictionary(dictionaryId, USER_ID))
                .thenThrow(new ResourceNotFoundException(String.format(
                        ExceptionStatusesEnum.RESOURCE_NOT_FOUND.getStatus(), Dictionary.class.getSimpleName(), dictionaryId)));

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> dictionaryService.updateDictionary(dictionaryId, dictionarySaveRequest))
                .withMessage(String.format(
                        ExceptionStatusesEnum.RESOURCE_NOT_FOUND.getStatus(), Dictionary.class.getSimpleName(), dictionaryId));

        verifyNoMoreInteractions(dictionaryRepository);
        verifyNoInteractions(dictionaryMapper);
    }

    @Test
    void testUpdateDictionary_WhenGivenValidDictionaryNameAndDictionaryIdExistsForUser_ThenReturnDictionarySaveResponse() {
        UserVocabularyApplication user = new UserVocabularyApplication(USER_ID, "email", "username",
                "noPassword", true, null, null);
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        Long dictionaryId = 1L;
        DictionarySaveRequest dictionarySaveRequest = new DictionarySaveRequest("dictionaryName");

        when(dictionaryRepository.existsByDictionaryNameEqualsIgnoreCaseAndUserProfileId(dictionarySaveRequest.getDictionaryName(), USER_ID))
                .thenReturn(false);

        Dictionary dictionaryToUpdate = Dictionary.builder()
                .id(dictionaryId)
                .dictionaryName("dictionaryName")
                .words(List.of())
                .userProfileId(USER_ID)
                .build();

        when(dictionaryRepository.getUserDictionary(dictionaryId, USER_ID))
                .thenReturn(dictionaryToUpdate);

        when(dictionaryRepository.save(dictionaryArgumentCaptor.capture())).thenReturn(dictionaryToUpdate);
        when(dictionaryMapper.toDto(dictionaryToUpdate)).thenReturn(new DictionaryDto(dictionaryId, "dictionaryName", 0));

        dictionaryService.updateDictionary(dictionaryId, dictionarySaveRequest);

        Dictionary actual = dictionaryArgumentCaptor.getValue();
        assertThat(actual.getDictionaryName()).isEqualTo(dictionarySaveRequest.getDictionaryName());
        assertThat(actual.getWordCount()).isEqualTo(0);
        assertThat(actual.getId()).isEqualTo(1L);
    }

    @Test
    void testDeleteDictionary_WhenDictionaryExistsForUser_ThenDeleteDictionary() {
        UserVocabularyApplication user = new UserVocabularyApplication(USER_ID, "email", "username",
                "noPassword", true, null, null);
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
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
        UserVocabularyApplication user = new UserVocabularyApplication(USER_ID, "email", "username",
                "noPassword", true, null, null);
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        Long dictionaryId = 1L;

        when(dictionaryRepository.getUserDictionary(dictionaryId, USER_ID))
                .thenThrow(new ResourceNotFoundException(String.format(
                ExceptionStatusesEnum.RESOURCE_NOT_FOUND.getStatus(), Dictionary.class.getSimpleName(), dictionaryId)));

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> dictionaryService.deleteDictionary(dictionaryId))
                .withMessage(String.format(
                        ExceptionStatusesEnum.RESOURCE_NOT_FOUND.getStatus(), Dictionary.class.getSimpleName(), dictionaryId));

        verifyNoMoreInteractions(dictionaryRepository);
    }

}
