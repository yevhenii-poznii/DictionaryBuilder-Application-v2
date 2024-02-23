package com.kiskee.vocabulary.service.vocabulary.dictionary;

import com.kiskee.vocabulary.enums.vocabulary.VocabularyResponseMessageEnum;
import com.kiskee.vocabulary.exception.DuplicateResourceException;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DictionaryServiceTest {

    @InjectMocks
    private DictionaryService dictionaryService;
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

        verify(dictionaryRepository).existsByDictionaryNameAndUserProfileId(dictionaryName, null);
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

        when(dictionaryRepository.existsByDictionaryNameAndUserProfileId(saveRequest.getDictionaryName(), USER_ID))
                .thenReturn(false);

        Dictionary savedDictionary = new Dictionary(1L, saveRequest.getDictionaryName(), Collections.emptyList(), USER_ID);
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

        when(dictionaryRepository.existsByDictionaryNameAndUserProfileId(dictionaryName, null))
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

        when(dictionaryRepository.existsByDictionaryNameAndUserProfileId(saveRequest.getDictionaryName(), USER_ID))
                .thenReturn(true);

        assertThatExceptionOfType(DuplicateResourceException.class)
                .isThrownBy(() -> dictionaryService.addDictionary(saveRequest))
                .withMessage(String.format(
                        VocabularyResponseMessageEnum.DICTIONARY_ALREADY_EXISTS.getResponseMessage(), saveRequest.getDictionaryName()));

        verifyNoMoreInteractions(dictionaryRepository);
        verifyNoInteractions(dictionaryMapper);
    }

}
