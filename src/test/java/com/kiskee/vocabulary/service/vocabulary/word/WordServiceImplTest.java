package com.kiskee.vocabulary.service.vocabulary.word;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kiskee.vocabulary.enums.ExceptionStatusesEnum;
import com.kiskee.vocabulary.enums.user.UserRole;
import com.kiskee.vocabulary.exception.ForbiddenAccessException;
import com.kiskee.vocabulary.exception.ResourceNotFoundException;
import com.kiskee.vocabulary.mapper.dictionary.WordMapper;
import com.kiskee.vocabulary.model.dto.ResponseMessage;
import com.kiskee.vocabulary.model.dto.vocabulary.word.WordDto;
import com.kiskee.vocabulary.model.dto.vocabulary.word.WordSaveRequest;
import com.kiskee.vocabulary.model.dto.vocabulary.word.WordSaveResponse;
import com.kiskee.vocabulary.model.dto.vocabulary.word.WordTranslationDto;
import com.kiskee.vocabulary.model.dto.vocabulary.word.WordUpdateRequest;
import com.kiskee.vocabulary.model.entity.user.UserVocabularyApplication;
import com.kiskee.vocabulary.model.entity.vocabulary.Word;
import com.kiskee.vocabulary.model.entity.vocabulary.WordTranslation;
import com.kiskee.vocabulary.repository.vocabulary.WordRepository;
import com.kiskee.vocabulary.service.vocabulary.dictionary.DictionaryAccessValidator;
import com.kiskee.vocabulary.service.vocabulary.word.translation.WordTranslationService;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
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
public class WordServiceImplTest {

    private static final UUID USER_ID = UUID.fromString("36effc62-d93a-4451-9f7b-7cf82de0d326");

    @InjectMocks
    private WordServiceImpl wordService;

    @Mock
    private WordRepository wordRepository;

    @Mock
    private WordMapper wordMapper;

    @Mock
    private DictionaryAccessValidator dictionaryAccessValidator;

    @Mock
    private WordTranslationService wordTranslationService;

    @Mock
    private SecurityContext securityContext;

    @Captor
    private ArgumentCaptor<Word> wordCaptor;

    @Captor
    private ArgumentCaptor<List<Word>> wordsCaptor;

    @Test
    void testAddWord_WhenDictionaryExistsForUser_ThenAddNewWord() {
        setAuth();

        long dictionaryId = 1L;
        WordSaveRequest saveRequest =
                new WordSaveRequest("word", List.of(new WordTranslationDto(null, "переклад")), "hint");

        Instant addedAt = Instant.parse("2024-03-12T12:12:00Z");
        Word wordToSave = new Word(
                null,
                saveRequest.getWord(),
                true,
                0,
                saveRequest.getWordHint(),
                addedAt,
                null,
                dictionaryId,
                List.of(new WordTranslation(null, "переклад")));

        when(wordMapper.toEntity(saveRequest, dictionaryId)).thenReturn(wordToSave);
        when(wordRepository.save(wordCaptor.capture())).thenReturn(wordToSave);

        WordDto wordDto = new WordDto(
                wordToSave.getId(),
                wordToSave.getWord(),
                wordToSave.isUseInRepetition(),
                new HashSet<>(saveRequest.getWordTranslations()),
                0,
                wordToSave.getWordHint());
        when(wordMapper.toDto(wordToSave)).thenReturn(wordDto);

        WordSaveResponse wordSaveResponse = wordService.addWord(dictionaryId, saveRequest);
        Word actualWord = wordCaptor.getValue();

        verify(dictionaryAccessValidator).verifyUserHasDictionary(dictionaryId);

        assertThat(actualWord.getWord()).isEqualTo(wordSaveResponse.getWord().getWord());
        assertThat(actualWord.isUseInRepetition())
                .isEqualTo(wordSaveResponse.getWord().isUseInRepetition());
        assertThat(actualWord.getWordHint())
                .isEqualTo(wordSaveResponse.getWord().getWordHint());
        assertThat(actualWord.getWordTranslations())
                .extracting(WordTranslation::getTranslation)
                .containsExactlyInAnyOrderElementsOf(wordSaveResponse.getWord().getWordTranslations().stream()
                        .map(WordTranslationDto::getTranslation)
                        .toList());
    }

    @Test
    void testAddWord_WhenDictionaryDoesNotExistForUser_ThenThrowResourceNotFoundException() {
        long dictionaryId = 1L;
        WordSaveRequest saveRequest =
                new WordSaveRequest("word", List.of(new WordTranslationDto(null, "переклад")), "hint");

        doThrow(ResourceNotFoundException.class).when(dictionaryAccessValidator).verifyUserHasDictionary(dictionaryId);

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> wordService.addWord(dictionaryId, saveRequest));
    }

    @Test
    void testUpdateWord_WhenDictionaryAndWordExistForUser_ThenUpdateWord() {
        setAuth();

        long dictionaryId = 1L;
        long wordId = 2L;
        WordUpdateRequest updateRequest = new WordUpdateRequest(
                "word",
                List.of(new WordTranslationDto(3L, "переклад"), new WordTranslationDto(null, "новий переклад")),
                "hint");

        Instant addedAt = Instant.parse("2024-03-12T12:12:00Z");
        Instant editedAt = Instant.parse("2024-03-14T16:56:00Z");
        Word wordToUpdate = new Word(
                wordId,
                updateRequest.getWord(),
                true,
                0,
                updateRequest.getWordHint(),
                addedAt,
                null,
                dictionaryId,
                List.of(new WordTranslation(3L, "переклад")));
        when(wordRepository.getWord(wordId)).thenReturn(wordToUpdate);

        List<WordTranslation> updatedTranslations =
                List.of(new WordTranslation(3L, "переклад"), new WordTranslation(null, "новий переклад"));
        when(wordTranslationService.updateTranslations(
                        updateRequest.getWordTranslations(), wordToUpdate.getWordTranslations()))
                .thenReturn(updatedTranslations);

        Word updatedWord = new Word(
                wordId,
                updateRequest.getWord(),
                wordToUpdate.isUseInRepetition(),
                0,
                updateRequest.getWordHint(),
                addedAt,
                editedAt,
                dictionaryId,
                updatedTranslations);
        when(wordMapper.toEntity(wordToUpdate, updateRequest, updatedTranslations))
                .thenReturn(updatedWord);
        when(wordRepository.save(wordCaptor.capture())).thenReturn(updatedWord);
        when(wordMapper.toDto(updatedWord))
                .thenReturn(new WordDto(
                        updatedWord.getId(),
                        updateRequest.getWord(),
                        updatedWord.isUseInRepetition(),
                        new HashSet<>(updateRequest.getWordTranslations()),
                        0,
                        updateRequest.getWordHint()));

        WordSaveResponse wordSaveResponse = wordService.updateWord(dictionaryId, wordId, updateRequest);
        Word actualWord = wordCaptor.getValue();

        verify(dictionaryAccessValidator).verifyUserHasDictionary(dictionaryId);

        assertThat(actualWord.getWord()).isEqualTo(wordSaveResponse.getWord().getWord());
        assertThat(actualWord.isUseInRepetition())
                .isEqualTo(wordSaveResponse.getWord().isUseInRepetition());
        assertThat(actualWord.getWordHint())
                .isEqualTo(wordSaveResponse.getWord().getWordHint());
        assertThat(actualWord.getWordTranslations())
                .extracting(WordTranslation::getTranslation)
                .containsExactlyInAnyOrderElementsOf(wordSaveResponse.getWord().getWordTranslations().stream()
                        .map(WordTranslationDto::getTranslation)
                        .toList());
    }

    @Test
    void testUpdateWord_WhenDictionaryDoesNotExistsForUser_ThenThrowResourceNotFoundException() {
        long dictionaryId = 1L;
        long wordId = 2L;
        WordUpdateRequest updateRequest = new WordUpdateRequest(
                "word",
                List.of(new WordTranslationDto(3L, "переклад"), new WordTranslationDto(null, "новий переклад")),
                "hint");

        doThrow(ResourceNotFoundException.class).when(dictionaryAccessValidator).verifyUserHasDictionary(dictionaryId);

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> wordService.updateWord(dictionaryId, wordId, updateRequest));
    }

    @Test
    void testUpdateWord_WhenWordDoesNotExist_ThenThrowResourceNotFoundException() {
        long dictionaryId = 1L;
        long wordId = 2L;
        WordUpdateRequest updateRequest = new WordUpdateRequest(
                "word",
                List.of(new WordTranslationDto(3L, "переклад"), new WordTranslationDto(null, "новий переклад")),
                "hint");

        when(wordRepository.getWord(wordId))
                .thenThrow(new ResourceNotFoundException(String.format(
                        ExceptionStatusesEnum.RESOURCE_NOT_FOUND.getStatus(), Word.class.getSimpleName(), wordId)));

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> wordService.updateWord(dictionaryId, wordId, updateRequest));
    }

    @Test
    void testUpdateWord_WhenWordDoesNotExistsForDictionary_ThenThrowForbiddenAccessException() {
        long dictionaryId = 1L;
        long wordId = 2L;
        WordUpdateRequest updateRequest = new WordUpdateRequest(
                "word",
                List.of(new WordTranslationDto(3L, "переклад"), new WordTranslationDto(null, "новий переклад")),
                "hint");

        Word wordToUpdate = new Word(
                wordId,
                updateRequest.getWord(),
                true,
                0,
                updateRequest.getWordHint(),
                null,
                null,
                150L,
                List.of(new WordTranslation(3L, "переклад")));
        when(wordRepository.getWord(wordId)).thenReturn(wordToUpdate);

        assertThatExceptionOfType(ForbiddenAccessException.class)
                .isThrownBy(() -> wordService.updateWord(dictionaryId, wordId, updateRequest))
                .withMessage(String.format(
                        ExceptionStatusesEnum.FORBIDDEN_ACCESS.getStatus(), Word.class.getSimpleName(), wordId));
    }

    @ParameterizedTest
    @MethodSource("updateRepetitionParameters")
    void testUpdateRepetition_WhenDictionaryAndWordExistForUser_ThenUpdateRepetition(boolean useInRepetition) {
        long dictionaryId = 1L;
        long wordId = 2L;

        when(wordRepository.updateUseInRepetitionByIdAndDictionaryId(wordId, dictionaryId, useInRepetition)).thenReturn(1);

        wordService.updateRepetition(dictionaryId, wordId, useInRepetition);

        verify(dictionaryAccessValidator).verifyUserHasDictionary(dictionaryId);
    }

    @Test
    void testUpdateRepetition_WhenDictionaryDoesNotExistForUser_ThenThrowResourceNotFoundException() {
        long dictionaryId = 1L;
        long wordId = 2L;

        doThrow(ResourceNotFoundException.class).when(dictionaryAccessValidator).verifyUserHasDictionary(dictionaryId);

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> wordService.updateRepetition(dictionaryId, wordId, true));
    }

    @Test
    void testUpdateRepetition_WhenWordDoesNotExist_ThenThrowResourceNotFoundException() {
        long dictionaryId = 1L;
        long wordId = 2L;

        when(wordRepository.updateUseInRepetitionByIdAndDictionaryId(wordId, dictionaryId, false))
                .thenReturn(0);

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> wordService.updateRepetition(dictionaryId, wordId, false));
    }

    @Test
    void testDeleteWord_WhenDictionaryAndWordExistForUser_ThenDeleteWord() {
        setAuth();

        long dictionaryId = 1L;
        long wordId = 2L;

        Word wordToDelete = mock(Word.class);
        when(wordToDelete.getId()).thenReturn(wordId);
        when(wordToDelete.getWord()).thenReturn("word");
        when(wordToDelete.getDictionaryId()).thenReturn(dictionaryId);
        when(wordRepository.getWord(wordId)).thenReturn(wordToDelete);

        doNothing().when(wordRepository).delete(wordCaptor.capture());

        wordService.deleteWord(dictionaryId, wordId);
        Word deletedWord = wordCaptor.getValue();

        verify(dictionaryAccessValidator).verifyUserHasDictionary(dictionaryId);

        assertThat(deletedWord.getId()).isEqualTo(wordId);
    }

    @Test
    void testDeleteWord_WhenDictionaryDoesNotExistForUser_ThenThrowResourceNotFoundException() {
        long dictionaryId = 1L;
        long wordId = 2L;

        doThrow(ResourceNotFoundException.class).when(dictionaryAccessValidator).verifyUserHasDictionary(dictionaryId);

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> wordService.deleteWord(dictionaryId, wordId));
    }

    @Test
    void testDeleteWord_WhenWordDoesNotExist_ThenThrowResourceNotFoundException() {
        long dictionaryId = 1L;
        long wordId = 2L;

        when(wordRepository.getWord(wordId))
                .thenThrow(new ResourceNotFoundException(String.format(
                        ExceptionStatusesEnum.RESOURCE_NOT_FOUND.getStatus(), Word.class.getSimpleName(), wordId)));

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> wordService.deleteWord(dictionaryId, wordId));
    }

    @Test
    void testDeleteWord_WhenWordDoesNotExistForDictionary_ThenThrowForbiddenAccessException() {
        long dictionaryId = 1L;
        long wordId = 2L;

        Word wordToDelete = mock(Word.class);
        when(wordToDelete.getId()).thenReturn(wordId);
        when(wordToDelete.getDictionaryId()).thenReturn(150L);
        when(wordRepository.getWord(wordId)).thenReturn(wordToDelete);

        assertThatThrownBy(() -> wordService.deleteWord(dictionaryId, wordId))
                .isInstanceOf(ForbiddenAccessException.class)
                .hasMessage(String.format(
                        ExceptionStatusesEnum.FORBIDDEN_ACCESS.getStatus(), Word.class.getSimpleName(), wordId));
    }

    @Test
    void testDeleteWords_WhenWhenDictionaryAndWordsExistForUser_ThenDeleteWords() {
        long dictionaryId = 1L;
        Set<Long> wordId = Set.of(2L, 15L, 33L);

        Word wordId2 = mock(Word.class);
        when(wordId2.getId()).thenReturn(2L);
        when(wordId2.getDictionaryId()).thenReturn(dictionaryId);
        Word wordId15 = mock(Word.class);
        when(wordId15.getId()).thenReturn(15L);
        when(wordId15.getDictionaryId()).thenReturn(dictionaryId);
        Word wordId33 = mock(Word.class);
        when(wordId33.getId()).thenReturn(33L);
        when(wordId33.getDictionaryId()).thenReturn(dictionaryId);
        List<Word> wordsToDelete = List.of(wordId2, wordId15, wordId33);

        when(wordRepository.findByIdIn(wordId)).thenReturn(wordsToDelete);
        doNothing().when(wordRepository).deleteAll(wordsCaptor.capture());

        ResponseMessage responseMessage = wordService.deleteWords(dictionaryId, wordId);
        System.out.println(responseMessage.getResponseMessage());
        List<Word> deletedWords = wordsCaptor.getValue();

        verify(dictionaryAccessValidator).verifyUserHasDictionary(dictionaryId);

        assertThat(deletedWords).extracting(Word::getId).containsExactlyInAnyOrderElementsOf(wordId);
    }

    @Test
    void testDeleteWords_WhenDictionaryDoesNotExist_ThenThrowResourceNotFoundException() {
        long dictionaryId = 1L;
        Set<Long> wordId = Set.of(2L, 15L, 33L);

        doThrow(ResourceNotFoundException.class).when(dictionaryAccessValidator).verifyUserHasDictionary(dictionaryId);

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> wordService.deleteWords(dictionaryId, wordId));
    }

    @Test
    void testDeleteWords_WhenWordDoesNotExistForDictionary_ThenThrowForbiddenAccessException() {
        long dictionaryId = 1L;
        Set<Long> wordId = Set.of(2L, 15L, 33L);

        Word wordId2 = mock(Word.class);
        when(wordId2.getId()).thenReturn(2L);
        when(wordId2.getDictionaryId()).thenReturn(150L);
        Word wordId15 = mock(Word.class);
        when(wordId15.getDictionaryId()).thenReturn(dictionaryId);
        Word wordId33 = mock(Word.class);
        when(wordId33.getDictionaryId()).thenReturn(dictionaryId);
        List<Word> wordsToDelete = List.of(wordId2, wordId15, wordId33);

        when(wordRepository.findByIdIn(wordId)).thenReturn(wordsToDelete);

        assertThatThrownBy(() -> wordService.deleteWords(dictionaryId, wordId))
                .isInstanceOf(ForbiddenAccessException.class)
                .hasMessage(String.format(
                        ExceptionStatusesEnum.FORBIDDEN_ACCESS.getStatus(),
                        Word.class.getSimpleName(),
                        List.of(wordId2.getId())));
    }

    private void setAuth() {
        UserVocabularyApplication user = new UserVocabularyApplication(
                USER_ID, "email", "username", "noPassword", true, UserRole.ROLE_USER, null, null);
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(user, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    static Stream<Boolean> updateRepetitionParameters() {
        return Stream.of(true, false);
    }
}
