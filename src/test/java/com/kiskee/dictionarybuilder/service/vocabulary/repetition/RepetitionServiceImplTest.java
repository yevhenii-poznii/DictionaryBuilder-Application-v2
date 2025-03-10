package com.kiskee.dictionarybuilder.service.vocabulary.repetition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.kiskee.dictionarybuilder.enums.ExceptionStatusesEnum;
import com.kiskee.dictionarybuilder.enums.repetition.RepetitionType;
import com.kiskee.dictionarybuilder.enums.user.UserRole;
import com.kiskee.dictionarybuilder.enums.vocabulary.filter.CriteriaFilterType;
import com.kiskee.dictionarybuilder.exception.ResourceNotFoundException;
import com.kiskee.dictionarybuilder.exception.repetition.RepetitionException;
import com.kiskee.dictionarybuilder.mapper.repetition.RepetitionWordMapper;
import com.kiskee.dictionarybuilder.model.dto.repetition.RepetitionRunningStatus;
import com.kiskee.dictionarybuilder.model.dto.repetition.filter.DefaultCriteriaFilter;
import com.kiskee.dictionarybuilder.model.dto.repetition.start.RepetitionStartFilterRequest;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.dictionary.DictionaryDto;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.word.WordDto;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.word.WordTranslationDto;
import com.kiskee.dictionarybuilder.model.entity.redis.repetition.Pause;
import com.kiskee.dictionarybuilder.model.entity.redis.repetition.RepetitionData;
import com.kiskee.dictionarybuilder.model.entity.user.UserVocabularyApplication;
import com.kiskee.dictionarybuilder.model.entity.vocabulary.Dictionary;
import com.kiskee.dictionarybuilder.repository.redis.RepetitionDataRepository;
import com.kiskee.dictionarybuilder.service.vocabulary.dictionary.DictionaryAccessValidator;
import com.kiskee.dictionarybuilder.service.vocabulary.loader.factory.WordLoaderFactory;
import com.kiskee.dictionarybuilder.service.vocabulary.repetition.handler.state.impl.OwnerRepetitionStateHandler;
import com.kiskee.dictionarybuilder.service.vocabulary.repetition.loader.criteria.RepetitionWordCriteriaLoader;
import com.kiskee.dictionarybuilder.service.vocabulary.word.WordCounterUpdateService;
import com.kiskee.dictionarybuilder.util.TimeZoneContextHolder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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
import org.springframework.test.util.ReflectionTestUtils;

@Disabled
@ExtendWith(MockitoExtension.class)
public class RepetitionServiceImplTest {

    private static final UUID USER_ID = UUID.fromString("36effc62-d93a-4451-9f7b-7cf82de0d326");

    @InjectMocks
    private OwnerRepetitionStateHandler repetitionService;

    @Mock
    private WordLoaderFactory<CriteriaFilterType, RepetitionWordCriteriaLoader> repetitionWordLoaderFactory;

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
        ReflectionTestUtils.setField(repetitionService, "wordsToUpdateCount", 10);
    }

    @ParameterizedTest
    @MethodSource("provideRepetitionTypes")
    void
            testIsRepetitionRunning_WhenRepetitionDataExistsForUserAndNotPaused_ThenReturnIsRunningTrueAndPausedFalseRepetitionRunningStatus(
                    RepetitionType repetitionType) {
        setAuth();

        RepetitionData repetitionData = mock(RepetitionData.class);
        when(repetitionData.isPaused()).thenReturn(false);
        when(repetitionData.getRepetitionType()).thenReturn(repetitionType);

        when(repository.findById(USER_ID.toString())).thenReturn(Optional.of(repetitionData));

        RepetitionRunningStatus repetitionRunningStatus = repetitionService.isRepetitionRunning();
        assertThat(repetitionRunningStatus.isRunning()).isTrue();
        assertThat(repetitionRunningStatus.isPaused()).isFalse();
        assertThat(repetitionRunningStatus.getRepetitionType()).isEqualTo(repetitionType);
    }

    @ParameterizedTest
    @MethodSource("provideRepetitionTypes")
    void
            testIsRepetitionRunning_WhenRepetitionDataExistsForUserAndPaused_ThenReturnIsRunningTrueAndPausedTrueRepetitionRunningStatus(
                    RepetitionType repetitionType) {
        setAuth();

        RepetitionData repetitionData = mock(RepetitionData.class);
        when(repetitionData.isPaused()).thenReturn(true);
        when(repetitionData.getRepetitionType()).thenReturn(repetitionType);

        when(repository.findById(USER_ID.toString())).thenReturn(Optional.of(repetitionData));

        RepetitionRunningStatus repetitionRunningStatus = repetitionService.isRepetitionRunning();
        assertThat(repetitionRunningStatus.isRunning()).isTrue();
        assertThat(repetitionRunningStatus.isPaused()).isTrue();
        assertThat(repetitionRunningStatus.getRepetitionType()).isEqualTo(repetitionType);
    }

    @Test
    void
            testIsRepetitionRunning_WhenDataDoesNotExistForUser_ThenReturnIsRunningFalseAndPausedFalseRepetitionRunningStatus() {
        setAuth();

        when(repository.findById(USER_ID.toString())).thenReturn(Optional.empty());

        RepetitionRunningStatus repetitionRunningStatus = repetitionService.isRepetitionRunning();

        verifyNoMoreInteractions(repository);

        assertThat(repetitionRunningStatus.isRunning()).isFalse();
        assertThat(repetitionRunningStatus.isPaused()).isFalse();
    }

    @ParameterizedTest
    @MethodSource("provideRepetitionTypes")
    void
            testStart_WhenRepetitionIsNotRunning_ThenPrepareRepetitionDataAndReturnIsRunningTrueAndPausedFalseRepetitionRunningStatus(
                    RepetitionType repetitionType) {
        setAuth();
        TimeZoneContextHolder.setTimeZone("Asia/Tokyo");

        long dictionaryId = 1L;
        RepetitionStartFilterRequest startFilterRequest = new RepetitionStartFilterRequest(
                RepetitionStartFilterRequest.RepetitionFilter.REPETITION_ONLY,
                new DefaultCriteriaFilter(CriteriaFilterType.ALL),
                repetitionType,
                false);

        when(repository.existsById(USER_ID.toString())).thenReturn(false);
        DictionaryDto dictionaryDto = new DictionaryDto(dictionaryId, "SomeDictionaryName");
        when(dictionaryAccessValidator.getDictionaryByIdAndUserId(dictionaryId, USER_ID))
                .thenReturn(dictionaryDto);

        RepetitionWordCriteriaLoader repetitionWordCriteriaLoader = mock(RepetitionWordCriteriaLoader.class);
        when(repetitionWordLoaderFactory.getLoader(
                        startFilterRequest.getCriteriaFilter().getFilterType()))
                .thenReturn(repetitionWordCriteriaLoader);

        List<WordDto> loadedWords = prepareLoadedWords();
        when(repetitionWordCriteriaLoader.loadWords(dictionaryId, startFilterRequest))
                .thenReturn(loadedWords);

        RepetitionRunningStatus runningStatus = repetitionService.start(dictionaryId, startFilterRequest);

        verify(repository).save(repetitionDataCaptor.capture());

        RepetitionData repetitionData = repetitionDataCaptor.getValue();

        assertThat(runningStatus.isRunning()).isTrue();
        assertThat(runningStatus.isPaused()).isFalse();
        assertThat(repetitionData.getPassedWords()).isEmpty();
        assertThat(repetitionData.getCurrentWord()).isEqualTo(loadedWords.getLast());
        assertThat(repetitionData.getPauses()).isEmpty();
        assertThat(repetitionData.getTotalElements()).isEqualTo(loadedWords.size());
        assertThat(repetitionData.getId()).isEqualTo(USER_ID.toString());
        assertThat(repetitionData.getRepetitionType()).isEqualTo(repetitionType);

        TimeZoneContextHolder.clear();
    }

    @Test
    void
            testStart_WhenRepetitionIsNotRunningAndDictionaryHasNotEnoughWords_ThenPrepareRepetitionDataAndReturnIsRunningTrueAndPausedFalseRepetitionRunningStatus() {
        setAuth();
        TimeZoneContextHolder.setTimeZone("Asia/Tokyo");

        long dictionaryId = 1L;
        RepetitionType repetitionType = RepetitionType.CHOICE;
        RepetitionStartFilterRequest startFilterRequest = new RepetitionStartFilterRequest(
                RepetitionStartFilterRequest.RepetitionFilter.REPETITION_ONLY,
                new DefaultCriteriaFilter(CriteriaFilterType.ALL),
                repetitionType,
                false);

        when(repository.existsById(USER_ID.toString())).thenReturn(false);
        DictionaryDto dictionaryDto = new DictionaryDto(dictionaryId, "SomeDictionaryName");
        when(dictionaryAccessValidator.getDictionaryByIdAndUserId(dictionaryId, USER_ID))
                .thenReturn(dictionaryDto);

        RepetitionWordCriteriaLoader repetitionWordCriteriaLoader = mock(RepetitionWordCriteriaLoader.class);
        when(repetitionWordLoaderFactory.getLoader(
                        startFilterRequest.getCriteriaFilter().getFilterType()))
                .thenReturn(repetitionWordCriteriaLoader);

        List<WordDto> loadedWords = prepareLoadedWords();
        loadedWords.removeLast();
        loadedWords.removeLast();
        when(repetitionWordCriteriaLoader.loadWords(dictionaryId, startFilterRequest))
                .thenReturn(loadedWords);

        assertThatExceptionOfType(RepetitionException.class)
                .isThrownBy(() -> repetitionService.start(dictionaryId, startFilterRequest))
                .withMessage("Not enough words to start repetition");

        TimeZoneContextHolder.clear();
    }

    @ParameterizedTest
    @MethodSource("provideRepetitionTypes")
    void testStart_WhenRepetitionIsAlreadyRunning_ThenThrowRepetitionException(RepetitionType repetitionType) {
        setAuth();

        long dictionaryId = 1L;
        RepetitionStartFilterRequest startFilterRequest = new RepetitionStartFilterRequest(
                RepetitionStartFilterRequest.RepetitionFilter.REPETITION_ONLY,
                new DefaultCriteriaFilter(CriteriaFilterType.ALL),
                repetitionType,
                false);

        when(repository.existsById(USER_ID.toString())).thenReturn(true);

        assertThatExceptionOfType(RepetitionException.class)
                .isThrownBy(() -> repetitionService.start(dictionaryId, startFilterRequest))
                .withMessage("Repetition is already running");
    }

    @ParameterizedTest
    @MethodSource("provideRepetitionTypes")
    void testStart_WhenUserDoesNotHaveSpecifiedDictionary_ThenThrowResourceNotFoundException(
            RepetitionType repetitionType) {
        setAuth();

        long dictionaryId = 1L;
        RepetitionStartFilterRequest startFilterRequest = new RepetitionStartFilterRequest(
                RepetitionStartFilterRequest.RepetitionFilter.REPETITION_ONLY,
                new DefaultCriteriaFilter(CriteriaFilterType.ALL),
                repetitionType,
                false);

        String exceptionMessage = String.format(
                ExceptionStatusesEnum.RESOURCE_NOT_FOUND.getStatus(), Dictionary.class.getSimpleName(), dictionaryId);
        when(repository.existsById(USER_ID.toString())).thenReturn(false);
        doThrow(new ResourceNotFoundException(exceptionMessage))
                .when(dictionaryAccessValidator)
                .getDictionaryByIdAndUserId(dictionaryId, USER_ID);

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> repetitionService.start(dictionaryId, startFilterRequest))
                .withMessage(exceptionMessage);
    }

    @ParameterizedTest
    @MethodSource("provideRepetitionTypes")
    void testStart_WhenNoWordsToRepeat_ThenThrowRepetitionException(RepetitionType repetitionType) {
        setAuth();

        long dictionaryId = 1L;
        RepetitionStartFilterRequest startFilterRequest = new RepetitionStartFilterRequest(
                RepetitionStartFilterRequest.RepetitionFilter.REPETITION_ONLY,
                new DefaultCriteriaFilter(CriteriaFilterType.ALL),
                repetitionType,
                false);

        when(repository.existsById(USER_ID.toString())).thenReturn(false);

        DictionaryDto dictionaryDto = new DictionaryDto(dictionaryId, "SomeDictionaryName");
        when(dictionaryAccessValidator.getDictionaryByIdAndUserId(dictionaryId, USER_ID))
                .thenReturn(dictionaryDto);

        RepetitionWordCriteriaLoader repetitionWordCriteriaLoader = mock(RepetitionWordCriteriaLoader.class);
        when(repetitionWordLoaderFactory.getLoader(
                        startFilterRequest.getCriteriaFilter().getFilterType()))
                .thenReturn(repetitionWordCriteriaLoader);

        when(repetitionWordCriteriaLoader.loadWords(dictionaryId, startFilterRequest))
                .thenReturn(new ArrayList<>());

        assertThatExceptionOfType(RepetitionException.class)
                .isThrownBy(() -> repetitionService.start(dictionaryId, startFilterRequest))
                .withMessage("No words to repeat");
    }

    @ParameterizedTest
    @MethodSource("provideRepetitionTypes")
    void testPause_WhenRepetitionIsRunningAndNotPaused_ThenStartPause(RepetitionType repetitionType) {
        setAuth();

        RepetitionData repetitionData = RepetitionData.builder()
                .pauses(new ArrayList<>())
                .repetitionType(repetitionType)
                .build();
        when(repository.findById(USER_ID.toString())).thenReturn(Optional.of(repetitionData));

        RepetitionRunningStatus runningStatus = repetitionService.pause();

        verify(repository).save(repetitionDataCaptor.capture());

        RepetitionData pausedRepetitionData = repetitionDataCaptor.getValue();
        assertThat(pausedRepetitionData.isPaused()).isTrue();

        assertThat(runningStatus.isRunning()).isTrue();
        assertThat(runningStatus.isPaused()).isTrue();
        assertThat(runningStatus.getRepetitionType()).isEqualTo(repetitionType);
    }

    @Test
    void testPause_WhenRepetitionIsRunningAndAlreadyPaused_ThenThrowRepetitionException() {
        setAuth();

        Pause alreadyStartedPause = Pause.start();
        RepetitionData repetitionData =
                RepetitionData.builder().pauses(List.of(alreadyStartedPause)).build();
        when(repository.findById(USER_ID.toString())).thenReturn(Optional.of(repetitionData));

        assertThatExceptionOfType(RepetitionException.class)
                .isThrownBy(() -> repetitionService.pause())
                .withMessage("Pause already started");
    }

    @Test
    void testPause_WhenRepetitionIsNotRunning_ThenThrowRepetitionException() {
        setAuth();

        assertThatExceptionOfType(RepetitionException.class)
                .isThrownBy(() -> repetitionService.pause())
                .withMessage("Repetition is not running");
    }

    @ParameterizedTest
    @MethodSource("provideRepetitionTypes")
    void testUnpause_WhenRepetitionIsRunningAndPaused_ThenEndPause(RepetitionType repetitionType) {
        setAuth();

        Pause startedPause = Pause.start();
        RepetitionData repetitionData = RepetitionData.builder()
                .pauses(List.of(startedPause))
                .repetitionType(repetitionType)
                .build();
        when(repository.findById(USER_ID.toString())).thenReturn(Optional.of(repetitionData));

        RepetitionRunningStatus runningStatus = repetitionService.unpause();

        verify(repository).save(repetitionDataCaptor.capture());

        RepetitionData unpausedRepetitionData = repetitionDataCaptor.getValue();
        assertThat(unpausedRepetitionData.isPaused()).isFalse();

        assertThat(runningStatus.isRunning()).isTrue();
        assertThat(runningStatus.isPaused()).isFalse();
        assertThat(runningStatus.getRepetitionType()).isEqualTo(repetitionType);
    }

    @Test
    void testUnpause_WhenRepetitionIsRunningAndHasNoStartedPause_ThenThrowRepetitionException() {
        setAuth();

        RepetitionData repetitionData =
                RepetitionData.builder().pauses(List.of()).build();
        when(repository.findById(USER_ID.toString())).thenReturn(Optional.of(repetitionData));

        assertThatExceptionOfType(RepetitionException.class)
                .isThrownBy(() -> repetitionService.unpause())
                .withMessage("No pause to end");
    }

    @Test
    void testUnpause_WhenRepetitionIsNotRunning_ThenThrowRepetitionException() {
        setAuth();

        assertThatExceptionOfType(RepetitionException.class)
                .isThrownBy(() -> repetitionService.unpause())
                .withMessage("Repetition is not running");
    }

    @Test
    void
            testStop_WhenRepetitionIsRunningAndPassedWordsAreEmpty_ThenClearRepetitionDataAndReturnIsRunningFalseRepetitionRunningStatus() {
        setAuth();

        RepetitionData repetitionData =
                RepetitionData.builder().passedWords(new ArrayList<>()).build();
        when(repository.findById(USER_ID.toString())).thenReturn(Optional.of(repetitionData));

        RepetitionRunningStatus runningStatus = repetitionService.stop();

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

        RepetitionRunningStatus runningStatus = repetitionService.stop();

        verify(wordCounterUpdateService).updateRightAnswersCounters(USER_ID, passedWords);
        verify(repository).delete(repetitionData);

        assertThat(runningStatus.isRunning()).isFalse();
        assertThat(runningStatus.isPaused()).isFalse();
    }

    @Test
    void testStop_WhenRepetitionIsNotRunning_ThenThrowRepetitionException() {
        setAuth();

        assertThatExceptionOfType(RepetitionException.class)
                .isThrownBy(() -> repetitionService.stop())
                .withMessage("Repetition is not running");
    }

    //    @Test
    //    void
    //
    // testHandleRepetitionMessage_WhenRequestIsStartOperationAndThereIsNextWord_ThenHandleStartOperationAndReturnWSResponse() {
    //        Authentication authentication = getAuth();
    //
    //        WSRequest wsRequest = new WSRequest(null, WSRequest.Operation.START);
    //
    //        WordDto currentWord = prepareRepetitionWords().removeLast();
    //        RepetitionData repetitionData =
    //                RepetitionData.builder().currentWord(currentWord).build();
    //        when(repository.findById(USER_ID.toString())).thenReturn(Optional.of(repetitionData));
    //
    //        WSResponse response = mock(WSResponse.class);
    //        when(response.getWord()).thenReturn(currentWord.getWord());
    //        when(mapper.toWSResponse(repetitionData)).thenReturn(response);
    //
    //        WSResponse currentWSResponse = repetitionService.handleRepetitionMessage(authentication, wsRequest);
    //
    //        assertThat(currentWSResponse.getWord()).isEqualTo(currentWord.getWord());
    //    }
    //
    //    @Test
    //    void
    //
    // testHandleRepetitionMessage_WhenRequestIsStartOperationAndThereIsNoNextWord_ThenHandleStartOperationAndThrowRepetitionException() {
    //        Authentication authentication = getAuth();
    //
    //        WSRequest wsRequest = new WSRequest(null, WSRequest.Operation.START);
    //
    //        RepetitionData repetitionData =
    //                RepetitionData.builder().currentWord(null).build();
    //        when(repository.findById(USER_ID.toString())).thenReturn(Optional.of(repetitionData));
    //
    //        assertThatExceptionOfType(RepetitionException.class)
    //                .isThrownBy(() -> repetitionService.handleRepetitionMessage(authentication, wsRequest))
    //                .withMessage("No more words to repeat");
    //    }
    //
    //    @Test
    //    void
    //
    // testHandleRepetitionMessage_WhenRequestIsSkipOperationAndThereIsNextWord_ThenHandleSkipOperationAndReturnWSResponse() {
    //        Authentication authentication = getAuth();
    //
    //        WSRequest wsRequest = new WSRequest(null, WSRequest.Operation.SKIP);
    //
    //        List<WordDto> repetitionWords = prepareRepetitionWords();
    //        WordDto currentWord = repetitionWords.removeLast();
    //        RepetitionData repetitionData = RepetitionData.builder()
    //                .repetitionWords(repetitionWords)
    //                .currentWord(currentWord)
    //                .build();
    //        when(repository.findById(USER_ID.toString())).thenReturn(Optional.of(repetitionData));
    //
    //        repetitionService.handleRepetitionMessage(authentication, wsRequest);
    //
    //        verify(repository).save(repetitionDataCaptor.capture());
    //
    //        RepetitionData updatedRepetitionData = repetitionDataCaptor.getValue();
    //        assertThat(updatedRepetitionData.getSkippedWordsCount()).isEqualTo(1);
    //    }
    //
    //    @Test
    //    void
    //
    // testHandleRepetitionMessage_WhenRequestIsSkipOperationAndThereIsNoNextWord_ThenHandleSkipOperationAndThrowRepetitionException() {
    //        Authentication authentication = getAuth();
    //
    //        WSRequest wsRequest = new WSRequest(null, WSRequest.Operation.SKIP);
    //
    //        List<WordDto> repetitionWords = new ArrayList<>();
    //        WordDto currentWord = new WordDto(3L, "word3", true, Set.of(), 0, null);
    //        RepetitionData repetitionData = RepetitionData.builder()
    //                .repetitionWords(repetitionWords)
    //                .currentWord(currentWord)
    //                .build();
    //        when(repository.findById(USER_ID.toString())).thenReturn(Optional.of(repetitionData));
    //
    //        assertThatExceptionOfType(RepetitionException.class)
    //                .isThrownBy(() -> repetitionService.handleRepetitionMessage(authentication, wsRequest))
    //                .withMessage("No more words to repeat");
    //    }
    //
    //    @Test
    //    void
    //
    // testHandleRepetitionMessage_WhenRequestIsCheckOperationAndThereIsCorrectTranslation_ThenHandleCheckOperationAndReturnWSResponse() {
    //        Authentication authentication = getAuth();
    //
    //        WSRequest wsRequest = new WSRequest("translation 1, translation 2", WSRequest.Operation.CHECK);
    //
    //        List<WordDto> repetitionWords = prepareRepetitionWords();
    //        WordDto currentWord = repetitionWords.getLast();
    //        DictionaryDto dictionaryDto = new DictionaryDto(1L, "SomeDictionaryName");
    //        RepetitionData repetitionData = new RepetitionData(
    //                repetitionWords, dictionaryDto, USER_ID, ZoneId.of("Asia/Tokyo"), RepetitionType.INPUT, false,
    // false);
    //        when(repository.findById(USER_ID.toString())).thenReturn(Optional.of(repetitionData));
    //
    //        repetitionService.handleRepetitionMessage(authentication, wsRequest);
    //
    //        verify(repository).save(repetitionDataCaptor.capture());
    //        verify(mapper).toWSResponse(any(RepetitionData.class), anyLong());
    //
    //        RepetitionData savedData = repetitionDataCaptor.getValue();
    //        assertThat(savedData.getPassedWords()).contains(currentWord);
    //        assertThat(savedData.getRightAnswersCount()).isEqualTo(1);
    //        assertThat(savedData.getTotalElementsPassed()).isEqualTo(1);
    //    }
    //
    //    @Test
    //    void
    //
    // testHandleRepetitionMessage_WhenRequestIsCheckOperationAndThereIsNoCorrectTranslation_ThenHandleCheckOperationAndReturnWSResponse() {
    //        Authentication authentication = getAuth();
    //
    //        WSRequest wsRequest = new WSRequest("translation 3, translation 4", WSRequest.Operation.CHECK);
    //
    //        List<WordDto> repetitionWords = prepareRepetitionWords();
    //        WordDto currentWord = repetitionWords.getLast();
    //        DictionaryDto dictionaryDto = new DictionaryDto(1L, "SomeDictionaryName");
    //        RepetitionData repetitionData = new RepetitionData(
    //                repetitionWords, dictionaryDto, USER_ID, ZoneId.of("Asia/Tokyo"), RepetitionType.INPUT, false,
    // false);
    //        when(repository.findById(USER_ID.toString())).thenReturn(Optional.of(repetitionData));
    //
    //        repetitionService.handleRepetitionMessage(authentication, wsRequest);
    //
    //        verify(repository).save(repetitionDataCaptor.capture());
    //        verify(mapper).toWSResponse(any(RepetitionData.class), anyLong());
    //
    //        RepetitionData savedData = repetitionDataCaptor.getValue();
    //        assertThat(savedData.getPassedWords()).contains(currentWord);
    //        assertThat(savedData.getWrongAnswersCount()).isEqualTo(1);
    //        assertThat(savedData.getTotalElementsPassed()).isEqualTo(1);
    //    }
    //
    //    @Test
    //    void
    //
    // testHandleRepetitionMessage_WhenRequestIsCheckOperationAndPassedWordsEqualsOrMoreThanWordsToUpdateCount_ThenHandleCheckOperationAndUpdateCounters() {
    //        Authentication authentication = getAuth();
    //
    //        WSRequest wsRequest = new WSRequest("translation 1, translation 2", WSRequest.Operation.CHECK);
    //
    //        List<WordDto> repetitionWords = prepareRepetitionWords();
    //        WordDto currentWord = repetitionWords.removeLast();
    //
    //        List<WordDto> passedWord = new ArrayList<>();
    //        IntStream.range(0, 10).forEach(i -> passedWord.add(mock(WordDto.class)));
    //
    //        DictionaryDto dictionaryDto = new DictionaryDto(1L, "SomeDictionaryName");
    //        RepetitionData repetitionData = new RepetitionData(
    //                repetitionWords, dictionaryDto, USER_ID, ZoneId.of("Asia/Tokyo"), RepetitionType.INPUT, false,
    // false);
    //        repetitionData.setCurrentWord(currentWord);
    //        repetitionData.setPassedWords(passedWord);
    //        when(repository.findById(USER_ID.toString())).thenReturn(Optional.of(repetitionData));
    //        doNothing().when(wordCounterUpdateService).updateRightAnswersCounters(eq(USER_ID), eq(passedWord));
    //
    //        repetitionService.handleRepetitionMessage(authentication, wsRequest);
    //
    //        verify(repository).save(repetitionDataCaptor.capture());
    //        verify(mapper).toWSResponse(any(RepetitionData.class), anyLong());
    //
    //        RepetitionData savedData = repetitionDataCaptor.getValue();
    //        assertThat(savedData.getPassedWords()).contains(currentWord);
    //        assertThat(savedData.getPassedWords().size()).isEqualTo(1);
    //    }
    //
    //    @Test
    //    void
    //
    // testHandleRepetitionMessage_WhenRequestIsCheckOperationAndCurrentWordIsNull_ThenHandleCheckOperationAndThrowRepetitionException() {
    //        Authentication authentication = getAuth();
    //
    //        WSRequest wsRequest = new WSRequest("translation 1, translation 2", WSRequest.Operation.CHECK);
    //
    //        RepetitionData repetitionData =
    //                RepetitionData.builder().currentWord(null).build();
    //        when(repository.findById(USER_ID.toString())).thenReturn(Optional.of(repetitionData));
    //
    //        assertThatExceptionOfType(RepetitionException.class)
    //                .isThrownBy(() -> repetitionService.handleRepetitionMessage(authentication, wsRequest))
    //                .withMessage("No more words to repeat");
    //    }
    //
    //    @Test
    //    void
    // testHandleRepetitionMessage_WhenRequestIsCheckAndReverseMode_ThenHandleCheckOperationAndReturnWSResponse() {
    //        Authentication authentication = getAuth();
    //
    //        WSRequest wsRequest = new WSRequest("word3, word1", WSRequest.Operation.CHECK);
    //
    //        List<WordDto> repetitionWords = prepareRepetitionWords();
    //        WordDto word = repetitionWords.getLast();
    //        DictionaryDto dictionaryDto = new DictionaryDto(1L, "SomeDictionaryName");
    //        RepetitionData repetitionData = new RepetitionData(
    //                repetitionWords, dictionaryDto, USER_ID, ZoneId.of("Asia/Tokyo"), RepetitionType.INPUT, true,
    // false);
    //        when(repository.findById(USER_ID.toString())).thenReturn(Optional.of(repetitionData));
    //
    //        WSResponse response = mock(WSResponse.class);
    //        String responseWord = repetitionWords.get(repetitionWords.size() - 2).getWordTranslations().stream()
    //                .map(WordTranslationDto::getTranslation)
    //                .collect(Collectors.joining(", "));
    //        when(response.getWord()).thenReturn(responseWord);
    //        when(mapper.toWSResponse(eq(repetitionData), anyLong())).thenReturn(response);
    //
    //        WSResponse currentWSResponse = repetitionService.handleRepetitionMessage(authentication, wsRequest);
    //
    //        verify(repository).save(repetitionDataCaptor.capture());
    //
    //        RepetitionData savedData = repetitionDataCaptor.getValue();
    //        Set<String> translationSet = Set.of(savedData.getCurrentWord().getWord());
    //        assertThat(savedData.getPassedWords()).contains(word);
    //        assertThat(savedData.getWord()).isEqualTo(currentWSResponse.getWord());
    //        assertThat(savedData.getTranslations()).isEqualTo(translationSet);
    //        assertThat(savedData.getRightAnswersCount()).isEqualTo(1);
    //        assertThat(savedData.getTotalElementsPassed()).isEqualTo(1);
    //    }

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
                new WordDto(1L, "word6", true, Set.of(new WordTranslationDto("translation 6")), 0, null),
                new WordDto(2L, "word5", true, Set.of(new WordTranslationDto("translation 5")), 0, null),
                new WordDto(2L, "word4", true, Set.of(new WordTranslationDto("translation 4")), 0, null),
                new WordDto(2L, "word3", true, Set.of(new WordTranslationDto("translation 3")), 0, null),
                new WordDto(3L, "word1", true, Set.of(new WordTranslationDto("translation 1")), 0, null)));
    }

    private List<WordDto> prepareRepetitionWords() {
        return new ArrayList<>(prepareLoadedWords());
    }

    private static Stream<RepetitionType> provideRepetitionTypes() {
        return Stream.of(RepetitionType.values());
    }
}
