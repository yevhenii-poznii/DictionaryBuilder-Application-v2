package com.kiskee.dictionarybuilder.service.vocabulary.repetition.handler.message.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kiskee.dictionarybuilder.enums.repetition.RepetitionType;
import com.kiskee.dictionarybuilder.enums.user.UserRole;
import com.kiskee.dictionarybuilder.exception.repetition.RepetitionException;
import com.kiskee.dictionarybuilder.mapper.repetition.RepetitionWordMapper;
import com.kiskee.dictionarybuilder.model.dto.repetition.message.WSRequest;
import com.kiskee.dictionarybuilder.model.dto.repetition.message.WSResponse;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.dictionary.DictionaryDto;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.word.WordDto;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.word.WordTranslationDto;
import com.kiskee.dictionarybuilder.model.entity.redis.repetition.RepetitionData;
import com.kiskee.dictionarybuilder.model.entity.user.UserVocabularyApplication;
import com.kiskee.dictionarybuilder.repository.redis.RepetitionDataRepository;
import com.kiskee.dictionarybuilder.service.vocabulary.repetition.RepetitionProgressUpdater;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class RepetitionMessageHandlerImplTest {

    private static final UUID USER_ID = UUID.fromString("36effc62-d93a-4451-9f7b-7cf82de0d326");

    @InjectMocks
    private RepetitionMessageHandlerImpl repetitionMessageHandler;

    @Mock
    private RepetitionDataRepository repository;

    @Mock
    private RepetitionWordMapper mapper;

    @Mock
    private RepetitionProgressUpdater repetitionProgressUpdater;

    @Captor
    private ArgumentCaptor<RepetitionData> repetitionDataCaptor;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(repetitionMessageHandler, "wordsToUpdateCount", 10);
    }

    @Test
    void
            testHandleRepetitionMessage_WhenRequestIsStartOperationAndThereIsNextWord_ThenHandleStartOperationAndReturnWSResponse() {
        Authentication authentication = getAuth();

        WSRequest wsRequest = new WSRequest(null, WSRequest.Operation.START);

        WordDto currentWord = prepareRepetitionWords().removeLast();
        RepetitionData repetitionData =
                RepetitionData.builder().currentWord(currentWord).build();
        when(repository.findById(USER_ID.toString())).thenReturn(Optional.of(repetitionData));

        WSResponse response = mock(WSResponse.class);
        when(response.getWord()).thenReturn(currentWord.getWord());
        when(mapper.toWSResponse(repetitionData)).thenReturn(response);

        WSResponse currentWSResponse = repetitionMessageHandler.handleRepetitionMessage(authentication, wsRequest);

        assertThat(currentWSResponse.getWord()).isEqualTo(currentWord.getWord());
    }

    @Test
    void
            testHandleRepetitionMessage_WhenRequestIsStartOperationAndThereIsNoNextWord_ThenHandleStartOperationAndThrowRepetitionException() {
        Authentication authentication = getAuth();

        WSRequest wsRequest = new WSRequest(null, WSRequest.Operation.START);

        RepetitionData repetitionData =
                RepetitionData.builder().currentWord(null).build();
        when(repository.findById(USER_ID.toString())).thenReturn(Optional.of(repetitionData));

        assertThatExceptionOfType(RepetitionException.class)
                .isThrownBy(() -> repetitionMessageHandler.handleRepetitionMessage(authentication, wsRequest))
                .withMessage("No more words to repeat");
    }

    @Test
    void
            testHandleRepetitionMessage_WhenRequestIsSkipOperationAndThereIsNextWord_ThenHandleSkipOperationAndReturnWSResponse() {
        Authentication authentication = getAuth();

        WSRequest wsRequest = new WSRequest(null, WSRequest.Operation.SKIP);

        List<WordDto> repetitionWords = prepareRepetitionWords();
        WordDto currentWord = repetitionWords.removeLast();
        RepetitionData repetitionData = RepetitionData.builder()
                .repetitionWords(repetitionWords)
                .currentWord(currentWord)
                .build();
        when(repository.findById(USER_ID.toString())).thenReturn(Optional.of(repetitionData));

        repetitionMessageHandler.handleRepetitionMessage(authentication, wsRequest);

        verify(repository).save(repetitionDataCaptor.capture());

        RepetitionData updatedRepetitionData = repetitionDataCaptor.getValue();
        assertThat(updatedRepetitionData.getSkippedWordsCount()).isEqualTo(1);
    }

    @Test
    void
            testHandleRepetitionMessage_WhenRequestIsSkipOperationAndThereIsNoNextWord_ThenHandleSkipOperationAndThrowRepetitionException() {
        Authentication authentication = getAuth();

        WSRequest wsRequest = new WSRequest(null, WSRequest.Operation.SKIP);

        List<WordDto> repetitionWords = new ArrayList<>();
        WordDto currentWord = new WordDto(3L, "word3", true, Set.of(), 0, null);
        RepetitionData repetitionData = RepetitionData.builder()
                .repetitionWords(repetitionWords)
                .currentWord(currentWord)
                .build();
        when(repository.findById(USER_ID.toString())).thenReturn(Optional.of(repetitionData));

        assertThatExceptionOfType(RepetitionException.class)
                .isThrownBy(() -> repetitionMessageHandler.handleRepetitionMessage(authentication, wsRequest))
                .withMessage("No more words to repeat");
    }

    @Test
    void
            testHandleRepetitionMessage_WhenRequestIsCheckOperationAndThereIsCorrectTranslation_ThenHandleCheckOperationAndReturnWSResponse() {
        Authentication authentication = getAuth();

        WSRequest wsRequest = new WSRequest("translation 1, translation 2", WSRequest.Operation.CHECK);

        List<WordDto> repetitionWords = prepareRepetitionWords();
        WordDto currentWord = repetitionWords.getLast();
        DictionaryDto dictionaryDto = new DictionaryDto(1L, "SomeDictionaryName");
        RepetitionData repetitionData = new RepetitionData(
                repetitionWords, dictionaryDto, USER_ID, ZoneId.of("Asia/Tokyo"), RepetitionType.INPUT, false, false);
        when(repository.findById(USER_ID.toString())).thenReturn(Optional.of(repetitionData));

        repetitionMessageHandler.handleRepetitionMessage(authentication, wsRequest);

        verify(repository).save(repetitionDataCaptor.capture());
        verify(mapper).toWSResponse(any(RepetitionData.class), anyLong());

        RepetitionData savedData = repetitionDataCaptor.getValue();
        assertThat(savedData.getPassedWords()).contains(currentWord);
        assertThat(savedData.getRightAnswersCount()).isEqualTo(1);
        assertThat(savedData.getTotalElementsPassed()).isEqualTo(1);
    }

    @Test
    void
            testHandleRepetitionMessage_WhenRequestIsCheckOperationAndThereIsNoCorrectTranslation_ThenHandleCheckOperationAndReturnWSResponse() {
        Authentication authentication = getAuth();

        WSRequest wsRequest = new WSRequest("translation 3, translation 4", WSRequest.Operation.CHECK);

        List<WordDto> repetitionWords = prepareRepetitionWords();
        WordDto currentWord = repetitionWords.getLast();
        DictionaryDto dictionaryDto = new DictionaryDto(1L, "SomeDictionaryName");
        RepetitionData repetitionData = new RepetitionData(
                repetitionWords, dictionaryDto, USER_ID, ZoneId.of("Asia/Tokyo"), RepetitionType.INPUT, false, false);
        when(repository.findById(USER_ID.toString())).thenReturn(Optional.of(repetitionData));

        repetitionMessageHandler.handleRepetitionMessage(authentication, wsRequest);

        verify(repository).save(repetitionDataCaptor.capture());
        verify(mapper).toWSResponse(any(RepetitionData.class), anyLong());

        RepetitionData savedData = repetitionDataCaptor.getValue();
        assertThat(savedData.getPassedWords()).contains(currentWord);
        assertThat(savedData.getWrongAnswersCount()).isEqualTo(1);
        assertThat(savedData.getTotalElementsPassed()).isEqualTo(1);
    }

    @Test
    void
            testHandleRepetitionMessage_WhenRequestIsCheckOperationAndCurrentWordIsNull_ThenHandleCheckOperationAndThrowRepetitionException() {
        Authentication authentication = getAuth();

        WSRequest wsRequest = new WSRequest("translation 1, translation 2", WSRequest.Operation.CHECK);

        RepetitionData repetitionData =
                RepetitionData.builder().currentWord(null).build();
        when(repository.findById(USER_ID.toString())).thenReturn(Optional.of(repetitionData));

        assertThatExceptionOfType(RepetitionException.class)
                .isThrownBy(() -> repetitionMessageHandler.handleRepetitionMessage(authentication, wsRequest))
                .withMessage("No more words to repeat");
    }

    @Test
    void testHandleRepetitionMessage_WhenRequestIsCheckAndReverseMode_ThenHandleCheckOperationAndReturnWSResponse() {
        Authentication authentication = getAuth();

        WSRequest wsRequest = new WSRequest("word3, word1", WSRequest.Operation.CHECK);

        List<WordDto> repetitionWords = prepareRepetitionWords();
        WordDto word = repetitionWords.getLast();
        DictionaryDto dictionaryDto = new DictionaryDto(1L, "SomeDictionaryName");
        RepetitionData repetitionData = new RepetitionData(
                repetitionWords, dictionaryDto, USER_ID, ZoneId.of("Asia/Tokyo"), RepetitionType.INPUT, true, false);
        when(repository.findById(USER_ID.toString())).thenReturn(Optional.of(repetitionData));

        WSResponse response = mock(WSResponse.class);
        String responseWord = repetitionWords.get(repetitionWords.size() - 2).getWordTranslations().stream()
                .map(WordTranslationDto::getTranslation)
                .collect(Collectors.joining(", "));
        when(response.getWord()).thenReturn(responseWord);
        when(mapper.toWSResponse(eq(repetitionData), anyLong())).thenReturn(response);

        WSResponse currentWSResponse = repetitionMessageHandler.handleRepetitionMessage(authentication, wsRequest);

        verify(repository).save(repetitionDataCaptor.capture());

        RepetitionData savedData = repetitionDataCaptor.getValue();
        Set<String> translationSet = Set.of(savedData.getCurrentWord().getWord());
        assertThat(savedData.getPassedWords()).contains(word);
        assertThat(savedData.getWord()).isEqualTo(currentWSResponse.getWord());
        assertThat(savedData.getTranslations()).isEqualTo(translationSet);
        assertThat(savedData.getRightAnswersCount()).isEqualTo(1);
        assertThat(savedData.getTotalElementsPassed()).isEqualTo(1);
    }

    @Test
    void testGetRequestType() {
        assertThat(repetitionMessageHandler.getRequestType()).isEqualTo(WSRequest.class);
    }

    @Test
    void testGetRepetitionWordMapper() {
        assertThat(repetitionMessageHandler.getMapper()).isEqualTo(mapper);
    }

    @Test
    void testGetWordsToUpdateCount() {
        assertThat(repetitionMessageHandler.getWordsToUpdateCount()).isEqualTo(10);
    }

    private List<WordDto> prepareLoadedWords() {
        return new ArrayList<>(List.of(
                new WordDto(1L, "word6", true, Set.of(new WordTranslationDto("translation 6")), 0, null),
                new WordDto(2L, "word5", true, Set.of(new WordTranslationDto("translation 5")), 0, null),
                new WordDto(2L, "word4", true, Set.of(new WordTranslationDto("translation 4")), 0, null),
                new WordDto(2L, "word3", true, Set.of(new WordTranslationDto("translation 3")), 0, null),
                new WordDto(3L, "word1", true, Set.of(new WordTranslationDto("translation 1")), 0, null)));
    }

    private List<WordDto> prepareRepetitionWords() {
        return new ArrayList<>(prepareLoadedWords());
    }

    private Authentication getAuth() {
        UserVocabularyApplication user = new UserVocabularyApplication(
                USER_ID, "email", "username", "noPassword", true, UserRole.ROLE_USER, null, null);
        return new UsernamePasswordAuthenticationToken(user, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }
}
