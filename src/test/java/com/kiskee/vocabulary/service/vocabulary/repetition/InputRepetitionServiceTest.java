package com.kiskee.vocabulary.service.vocabulary.repetition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.kiskee.vocabulary.enums.ExceptionStatusesEnum;
import com.kiskee.vocabulary.enums.user.UserRole;
import com.kiskee.vocabulary.exception.ResourceNotFoundException;
import com.kiskee.vocabulary.exception.repetition.RepetitionException;
import com.kiskee.vocabulary.mapper.repetition.RepetitionWordMapper;
import com.kiskee.vocabulary.model.dto.repetition.RepetitionRunningStatus;
import com.kiskee.vocabulary.model.dto.repetition.RepetitionStartFilterRequest;
import com.kiskee.vocabulary.model.dto.repetition.filter.DefaultCriteriaFilter;
import com.kiskee.vocabulary.model.dto.repetition.message.WSRequest;
import com.kiskee.vocabulary.model.dto.repetition.message.WSResponse;
import com.kiskee.vocabulary.model.dto.vocabulary.dictionary.DictionaryDto;
import com.kiskee.vocabulary.model.dto.vocabulary.word.WordDto;
import com.kiskee.vocabulary.model.dto.vocabulary.word.WordTranslationDto;
import com.kiskee.vocabulary.model.entity.redis.repetition.Pause;
import com.kiskee.vocabulary.model.entity.redis.repetition.RepetitionData;
import com.kiskee.vocabulary.model.entity.user.UserVocabularyApplication;
import com.kiskee.vocabulary.model.entity.vocabulary.Dictionary;
import com.kiskee.vocabulary.repository.redis.RepetitionDataRepository;
import com.kiskee.vocabulary.service.vocabulary.dictionary.DictionaryAccessValidator;
import com.kiskee.vocabulary.service.vocabulary.repetition.loader.RepetitionWordLoaderFactory;
import com.kiskee.vocabulary.service.vocabulary.repetition.loader.criteria.RepetitionWordCriteriaLoader;
import com.kiskee.vocabulary.service.vocabulary.word.WordCounterUpdateService;
import com.kiskee.vocabulary.util.TimeZoneContextHolder;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;
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
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class InputRepetitionServiceTest {

    private static final UUID USER_ID = UUID.fromString("36effc62-d93a-4451-9f7b-7cf82de0d326");

    @InjectMocks
    private InputRepetitionService inputRepetitionService;

    @Mock
    private RepetitionWordLoaderFactory repetitionWordLoaderFactory;

    @Mock
    private RepetitionDataRepository repository;

    @Mock
    private RepetitionWordMapper mapper;

    @Mock
    private DictionaryAccessValidator dictionaryAccessValidator;

    @Mock
    private WordCounterUpdateService wordCounterUpdateService;

    @Mock
    private SecurityContext securityContext;

    @Captor
    private ArgumentCaptor<RepetitionData> repetitionDataCaptor;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(inputRepetitionService, "wordsToUpdateCount", 10);
    }

    @Test
    void
            testIsRepetitionRunning_WhenRepetitionDataExistsForUserAndNotPaused_ThenReturnIsRunningTrueAndPausedFalseRepetitionRunningStatus() {
        setAuth();

        RepetitionData repetitionData = mock(RepetitionData.class);
        when(repetitionData.isPaused()).thenReturn(false);

        when(repository.findById(USER_ID.toString())).thenReturn(Optional.of(repetitionData));

        RepetitionRunningStatus repetitionRunningStatus = inputRepetitionService.isRepetitionRunning();
        assertThat(repetitionRunningStatus.isRunning()).isTrue();
        assertThat(repetitionRunningStatus.isPaused()).isFalse();
    }

    @Test
    void
            testIsRepetitionRunning_WhenRepetitionDataExistsForUserAndPaused_ThenReturnIsRunningTrueAndPausedTrueRepetitionRunningStatus() {
        setAuth();

        RepetitionData repetitionData = mock(RepetitionData.class);
        when(repetitionData.isPaused()).thenReturn(true);

        when(repository.findById(USER_ID.toString())).thenReturn(Optional.of(repetitionData));

        RepetitionRunningStatus repetitionRunningStatus = inputRepetitionService.isRepetitionRunning();
        assertThat(repetitionRunningStatus.isRunning()).isTrue();
        assertThat(repetitionRunningStatus.isPaused()).isTrue();
    }

    @Test
    void
            testIsRepetitionRunning_WhenDataDoesNotExistForUser_ThenReturnIsRunningFalseAndPausedFalseRepetitionRunningStatus() {
        setAuth();

        when(repository.findById(USER_ID.toString())).thenReturn(Optional.empty());

        RepetitionRunningStatus repetitionRunningStatus = inputRepetitionService.isRepetitionRunning();

        verifyNoMoreInteractions(repository);

        assertThat(repetitionRunningStatus.isRunning()).isFalse();
        assertThat(repetitionRunningStatus.isPaused()).isFalse();
    }

    @Test
    void
            testStart_WhenRepetitionIsNotRunning_ThenPrepareRepetitionDataAndReturnIsRunningTrueAndPausedFalseRepetitionRunningStatus() {
        setAuth();
        TimeZoneContextHolder.setTimeZone("Asia/Tokyo");

        long dictionaryId = 1L;
        RepetitionStartFilterRequest startFilterRequest = new RepetitionStartFilterRequest(
                RepetitionStartFilterRequest.RepetitionFilter.REPETITION_ONLY,
                new DefaultCriteriaFilter(DefaultCriteriaFilter.CriteriaFilterType.ALL));

        when(repository.existsById(USER_ID.toString())).thenReturn(false);
        DictionaryDto dictionaryDto = new DictionaryDto(dictionaryId, "SomeDictionaryName");
        when(dictionaryAccessValidator.getDictionaryByIdAndUserId(dictionaryId, USER_ID))
                .thenReturn(dictionaryDto);

        RepetitionWordCriteriaLoader repetitionWordCriteriaLoader = mock(RepetitionWordCriteriaLoader.class);
        when(repetitionWordLoaderFactory.getLoader(
                        startFilterRequest.getCriteriaFilter().getFilterType()))
                .thenReturn(repetitionWordCriteriaLoader);

        List<WordDto> loadedWords = prepareLoadedWords();
        when(repetitionWordCriteriaLoader.loadRepetitionWordPage(dictionaryId, startFilterRequest))
                .thenReturn(loadedWords);

        RepetitionRunningStatus runningStatus = inputRepetitionService.start(dictionaryId, startFilterRequest);

        verify(repository).save(repetitionDataCaptor.capture());

        RepetitionData repetitionData = repetitionDataCaptor.getValue();

        assertThat(runningStatus.isRunning()).isTrue();
        assertThat(runningStatus.isPaused()).isFalse();
        assertThat(repetitionData.getRepetitionWords().size()).isEqualTo(2);
        assertThat(repetitionData.getPassedWords()).isEmpty();
        assertThat(repetitionData.getCurrentWord()).isEqualTo(loadedWords.getLast());
        assertThat(repetitionData.getPauses()).isEmpty();
        assertThat(repetitionData.getTotalElements()).isEqualTo(loadedWords.size());
        assertThat(repetitionData.getId()).isEqualTo(USER_ID.toString());

        TimeZoneContextHolder.clear();
    }

    @Test
    void testStart_WhenRepetitionIsAlreadyRunning_ThenThrowRepetitionException() {
        setAuth();

        long dictionaryId = 1L;
        RepetitionStartFilterRequest startFilterRequest = new RepetitionStartFilterRequest(
                RepetitionStartFilterRequest.RepetitionFilter.REPETITION_ONLY,
                new DefaultCriteriaFilter(DefaultCriteriaFilter.CriteriaFilterType.ALL));

        when(repository.existsById(USER_ID.toString())).thenReturn(true);

        assertThatExceptionOfType(RepetitionException.class)
                .isThrownBy(() -> inputRepetitionService.start(dictionaryId, startFilterRequest))
                .withMessage("Repetition is already running");
    }

    @Test
    void testStart_WhenUserDoesNotHaveSpecifiedDictionary_ThenThrowResourceNotFoundException() {
        setAuth();

        long dictionaryId = 1L;
        RepetitionStartFilterRequest startFilterRequest = new RepetitionStartFilterRequest(
                RepetitionStartFilterRequest.RepetitionFilter.REPETITION_ONLY,
                new DefaultCriteriaFilter(DefaultCriteriaFilter.CriteriaFilterType.ALL));

        when(repository.existsById(USER_ID.toString())).thenReturn(false);
        doThrow(new ResourceNotFoundException(String.format(
                        ExceptionStatusesEnum.RESOURCE_NOT_FOUND.getStatus(),
                        Dictionary.class.getSimpleName(),
                        dictionaryId)))
                .when(dictionaryAccessValidator)
                .getDictionaryByIdAndUserId(dictionaryId, USER_ID);

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> inputRepetitionService.start(dictionaryId, startFilterRequest))
                .withMessage(String.format(
                        ExceptionStatusesEnum.RESOURCE_NOT_FOUND.getStatus(),
                        Dictionary.class.getSimpleName(),
                        dictionaryId));
    }

    @Test
    void testStart_WhenNoWordsToRepeat_ThenThrowRepetitionException() {
        setAuth();

        long dictionaryId = 1L;
        RepetitionStartFilterRequest startFilterRequest = new RepetitionStartFilterRequest(
                RepetitionStartFilterRequest.RepetitionFilter.REPETITION_ONLY,
                new DefaultCriteriaFilter(DefaultCriteriaFilter.CriteriaFilterType.ALL));

        when(repository.existsById(USER_ID.toString())).thenReturn(false);

        DictionaryDto dictionaryDto = new DictionaryDto(dictionaryId, "SomeDictionaryName");
        when(dictionaryAccessValidator.getDictionaryByIdAndUserId(dictionaryId, USER_ID))
                .thenReturn(dictionaryDto);

        RepetitionWordCriteriaLoader repetitionWordCriteriaLoader = mock(RepetitionWordCriteriaLoader.class);
        when(repetitionWordLoaderFactory.getLoader(
                        startFilterRequest.getCriteriaFilter().getFilterType()))
                .thenReturn(repetitionWordCriteriaLoader);

        when(repetitionWordCriteriaLoader.loadRepetitionWordPage(dictionaryId, startFilterRequest))
                .thenReturn(new ArrayList<>());

        assertThatExceptionOfType(RepetitionException.class)
                .isThrownBy(() -> inputRepetitionService.start(dictionaryId, startFilterRequest))
                .withMessage("No words to repeat");
    }

    @Test
    void testPause_WhenRepetitionIsRunningAndNotPaused_ThenStartPause() {
        setAuth();

        RepetitionData repetitionData =
                RepetitionData.builder().pauses(new ArrayList<>()).build();
        when(repository.findById(USER_ID.toString())).thenReturn(Optional.of(repetitionData));

        RepetitionRunningStatus runningStatus = inputRepetitionService.pause();

        verify(repository).save(repetitionDataCaptor.capture());

        RepetitionData pausedRepetitionData = repetitionDataCaptor.getValue();
        assertThat(pausedRepetitionData.isPaused()).isTrue();

        assertThat(runningStatus.isRunning()).isTrue();
        assertThat(runningStatus.isPaused()).isTrue();
    }

    @Test
    void testPause_WhenRepetitionIsRunningAndAlreadyPaused_ThenThrowRepetitionException() {
        setAuth();

        Pause alreadyStartedPause = Pause.start();
        RepetitionData repetitionData =
                RepetitionData.builder().pauses(List.of(alreadyStartedPause)).build();
        when(repository.findById(USER_ID.toString())).thenReturn(Optional.of(repetitionData));

        assertThatExceptionOfType(RepetitionException.class)
                .isThrownBy(() -> inputRepetitionService.pause())
                .withMessage("Pause already started");
    }

    @Test
    void testPause_WhenRepetitionIsNotRunning_ThenThrowRepetitionException() {
        setAuth();

        assertThatExceptionOfType(RepetitionException.class)
                .isThrownBy(() -> inputRepetitionService.pause())
                .withMessage("Repetition is not running");
    }

    @Test
    void testUnpause_WhenRepetitionIsRunningAndPaused_ThenEndPause() {
        setAuth();

        Pause startedPause = Pause.start();
        RepetitionData repetitionData =
                RepetitionData.builder().pauses(List.of(startedPause)).build();
        when(repository.findById(USER_ID.toString())).thenReturn(Optional.of(repetitionData));

        RepetitionRunningStatus runningStatus = inputRepetitionService.unpause();

        verify(repository).save(repetitionDataCaptor.capture());

        RepetitionData unpausedRepetitionData = repetitionDataCaptor.getValue();
        assertThat(unpausedRepetitionData.isPaused()).isFalse();

        assertThat(runningStatus.isRunning()).isTrue();
        assertThat(runningStatus.isPaused()).isFalse();
    }

    @Test
    void testUnpause_WhenRepetitionIsRunningAndHasNoStartedPause_ThenThrowRepetitionException() {
        setAuth();

        RepetitionData repetitionData =
                RepetitionData.builder().pauses(List.of()).build();
        when(repository.findById(USER_ID.toString())).thenReturn(Optional.of(repetitionData));

        assertThatExceptionOfType(RepetitionException.class)
                .isThrownBy(() -> inputRepetitionService.unpause())
                .withMessage("No pause to end");
    }

    @Test
    void testUnpause_WhenRepetitionIsNotRunning_ThenThrowRepetitionException() {
        setAuth();

        assertThatExceptionOfType(RepetitionException.class)
                .isThrownBy(() -> inputRepetitionService.unpause())
                .withMessage("Repetition is not running");
    }

    @Test
    void
            testStop_WhenRepetitionIsRunningAndPassedWordsAreEmpty_ThenClearRepetitionDataAndReturnIsRunningFalseRepetitionRunningStatus() {
        setAuth();

        RepetitionData repetitionData =
                RepetitionData.builder().passedWords(new ArrayList<>()).build();
        when(repository.findById(USER_ID.toString())).thenReturn(Optional.of(repetitionData));

        RepetitionRunningStatus runningStatus = inputRepetitionService.stop();

        verify(repository).delete(repetitionData);

        assertThat(runningStatus.isRunning()).isFalse();
        assertThat(runningStatus.isPaused()).isFalse();
    }

    @Test
    void testStop_WhenRepetitionIsRunningAndPassedWordsAreNotEmpty_ThenUpdateWordCountersAndClearRepetitionData() {
        setAuth();

        List<WordDto> passedWords = List.of(
                new WordDto(1L, "word1", true, Set.of(), 1, null), new WordDto(2L, "word2", true, Set.of(), 1, null));
        RepetitionData repetitionData =
                RepetitionData.builder().passedWords(passedWords).build();
        when(repository.findById(USER_ID.toString())).thenReturn(Optional.of(repetitionData));

        RepetitionRunningStatus runningStatus = inputRepetitionService.stop();

        verify(wordCounterUpdateService).updateRightAnswersCounters(USER_ID, passedWords);
        verify(repository).delete(repetitionData);

        assertThat(runningStatus.isRunning()).isFalse();
        assertThat(runningStatus.isPaused()).isFalse();
    }

    @Test
    void testStop_WhenRepetitionIsNotRunning_ThenThrowRepetitionException() {
        setAuth();

        assertThatExceptionOfType(RepetitionException.class)
                .isThrownBy(() -> inputRepetitionService.stop())
                .withMessage("Repetition is not running");
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

        WSResponse currentWSResponse = inputRepetitionService.handleRepetitionMessage(authentication, wsRequest);

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
                .isThrownBy(() -> inputRepetitionService.handleRepetitionMessage(authentication, wsRequest))
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

        inputRepetitionService.handleRepetitionMessage(authentication, wsRequest);

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
                .isThrownBy(() -> inputRepetitionService.handleRepetitionMessage(authentication, wsRequest))
                .withMessage("No more words to repeat");
    }

    @Test
    void
            testHandleRepetitionMessage_WhenRequestIsCheckOperationAndThereIsCorrectTranslation_ThenHandleCheckOperationAndReturnWSResponse() {
        Authentication authentication = getAuth();

        WSRequest wsRequest = new WSRequest("translation 1, translation 2", WSRequest.Operation.NEXT);

        List<WordDto> repetitionWords = prepareRepetitionWords();
        WordDto currentWord = repetitionWords.getLast();
        RepetitionData repetitionData =
                new RepetitionData(repetitionWords, 1L, "SomeDictionaryName", USER_ID, ZoneId.of("Asia/Tokyo"));
        when(repository.findById(USER_ID.toString())).thenReturn(Optional.of(repetitionData));

        inputRepetitionService.handleRepetitionMessage(authentication, wsRequest);

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

        WSRequest wsRequest = new WSRequest("translation 3, translation 4", WSRequest.Operation.NEXT);

        List<WordDto> repetitionWords = prepareRepetitionWords();
        WordDto currentWord = repetitionWords.removeLast();
        RepetitionData repetitionData = RepetitionData.builder()
                .repetitionWords(repetitionWords)
                .passedWords(new ArrayList<>())
                .currentWord(currentWord)
                .build();
        when(repository.findById(USER_ID.toString())).thenReturn(Optional.of(repetitionData));

        inputRepetitionService.handleRepetitionMessage(authentication, wsRequest);

        verify(repository).save(repetitionDataCaptor.capture());
        verify(mapper).toWSResponse(any(RepetitionData.class), anyLong());

        RepetitionData savedData = repetitionDataCaptor.getValue();
        assertThat(savedData.getPassedWords()).contains(currentWord);
        assertThat(savedData.getWrongAnswersCount()).isEqualTo(1);
        assertThat(savedData.getTotalElementsPassed()).isEqualTo(1);
    }

    @Test
    void
            testHandleRepetitionMessage_WhenRequestIsCheckOperationAndPassedWordsEqualsOrMoreThanWordsToUpdateCount_ThenHandleCheckOperationAndUpdateCounters() {
        Authentication authentication = getAuth();

        WSRequest wsRequest = new WSRequest("translation 1, translation 2", WSRequest.Operation.NEXT);

        List<WordDto> repetitionWords = prepareRepetitionWords();
        WordDto currentWord = repetitionWords.removeLast();

        List<WordDto> passedWord = new ArrayList<>();
        IntStream.range(0, 10).forEach(i -> passedWord.add(mock(WordDto.class)));

        RepetitionData repetitionData = RepetitionData.builder()
                .repetitionWords(repetitionWords)
                .passedWords(passedWord)
                .currentWord(currentWord)
                .build();
        when(repository.findById(USER_ID.toString())).thenReturn(Optional.of(repetitionData));
        doNothing().when(wordCounterUpdateService).updateRightAnswersCounters(eq(USER_ID), eq(passedWord));

        inputRepetitionService.handleRepetitionMessage(authentication, wsRequest);

        verify(repository).save(repetitionDataCaptor.capture());
        verify(mapper).toWSResponse(any(RepetitionData.class), anyLong());

        RepetitionData savedData = repetitionDataCaptor.getValue();
        assertThat(savedData.getPassedWords()).contains(currentWord);
        assertThat(savedData.getPassedWords().size()).isEqualTo(1);
    }

    @Test
    void
            testHandleRepetitionMessage_WhenRequestIsCheckOperationAndCurrentWordIsNull_ThenHandleCheckOperationAndThrowRepetitionException() {
        Authentication authentication = getAuth();

        WSRequest wsRequest = new WSRequest("translation 1, translation 2", WSRequest.Operation.NEXT);

        RepetitionData repetitionData =
                RepetitionData.builder().currentWord(null).build();
        when(repository.findById(USER_ID.toString())).thenReturn(Optional.of(repetitionData));

        assertThatExceptionOfType(RepetitionException.class)
                .isThrownBy(() -> inputRepetitionService.handleRepetitionMessage(authentication, wsRequest))
                .withMessage("No more words to repeat");
    }

    private void setAuth() {
        Authentication authentication = getAuth();
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    private Authentication getAuth() {
        UserVocabularyApplication user = new UserVocabularyApplication(
                USER_ID, "email", "username", "noPassword", true, UserRole.ROLE_USER, null, null);
        return new UsernamePasswordAuthenticationToken(user, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    private List<WordDto> prepareLoadedWords() {
        return new ArrayList<>(List.of(
                new WordDto(1L, "word1", true, Set.of(new WordTranslationDto("translation 1")), 0, null),
                new WordDto(2L, "word2", true, Set.of(), 0, null),
                new WordDto(3L, "word3", true, Set.of(new WordTranslationDto("translation 1")), 0, null)));
    }

    private List<WordDto> prepareRepetitionWords() {
        return new ArrayList<>(prepareLoadedWords());
    }
}
