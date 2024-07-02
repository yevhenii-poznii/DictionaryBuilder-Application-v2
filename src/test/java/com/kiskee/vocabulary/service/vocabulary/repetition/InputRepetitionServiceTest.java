package com.kiskee.vocabulary.service.vocabulary.repetition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
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
import com.kiskee.vocabulary.model.dto.redis.Pause;
import com.kiskee.vocabulary.model.dto.redis.RepetitionData;
import com.kiskee.vocabulary.model.dto.repetition.RepetitionRunningStatus;
import com.kiskee.vocabulary.model.dto.repetition.RepetitionStartFilterRequest;
import com.kiskee.vocabulary.model.dto.repetition.filter.DefaultCriteriaFilter;
import com.kiskee.vocabulary.model.dto.repetition.message.WSRequest;
import com.kiskee.vocabulary.model.dto.repetition.message.WSResponse;
import com.kiskee.vocabulary.model.dto.vocabulary.word.WordDto;
import com.kiskee.vocabulary.model.entity.user.UserVocabularyApplication;
import com.kiskee.vocabulary.model.entity.vocabulary.Dictionary;
import com.kiskee.vocabulary.repository.redis.RedisRepository;
import com.kiskee.vocabulary.service.vocabulary.dictionary.DictionaryAccessValidator;
import com.kiskee.vocabulary.service.vocabulary.repetition.loader.RepetitionWordLoaderFactory;
import com.kiskee.vocabulary.service.vocabulary.repetition.loader.criteria.RepetitionWordCriteriaLoader;
import com.kiskee.vocabulary.service.vocabulary.word.WordCounterUpdateService;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Set;
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
public class InputRepetitionServiceTest {

    private static final UUID USER_ID = UUID.fromString("36effc62-d93a-4451-9f7b-7cf82de0d326");

    @InjectMocks
    private InputRepetitionService inputRepetitionService;

    @Mock
    private RepetitionWordLoaderFactory repetitionWordLoaderFactory;

    @Mock
    private RedisRepository repository;

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

    @Test
    void
    testIsRepetitionRunning_WhenRepetitionDataExistsForUserAndNotPaused_ThenReturnIsRunningTrueAndPausedFalseRepetitionRunningStatus() {
        setAuth();

        RepetitionData repetitionData = mock(RepetitionData.class);
        when(repetitionData.isPaused()).thenReturn(false);

        when(repository.existsByUserId(USER_ID)).thenReturn(true);
        when(repository.getByUserId(USER_ID)).thenReturn(repetitionData);

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

        when(repository.existsByUserId(USER_ID)).thenReturn(true);
        when(repository.getByUserId(USER_ID)).thenReturn(repetitionData);

        RepetitionRunningStatus repetitionRunningStatus = inputRepetitionService.isRepetitionRunning();
        assertThat(repetitionRunningStatus.isRunning()).isTrue();
        assertThat(repetitionRunningStatus.isPaused()).isTrue();
    }

    @Test
    void testIsRepetitionRunning_WhenRepetitionDataIsNull_ThenThrowResourceNotFoundException() {
        setAuth();

        when(repository.existsByUserId(USER_ID)).thenReturn(true);
        when(repository.getByUserId(USER_ID))
                .thenThrow(new ResourceNotFoundException(
                        String.format("Repetition data not found for user [%s]", USER_ID)));

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> inputRepetitionService.isRepetitionRunning())
                .withMessage(String.format("Repetition data not found for user [%s]", USER_ID));
    }

    @Test
    void
    testIsRepetitionRunning_WhenDataDoesNotExistForUser_ThenReturnIsRunningFalseAndPausedFalseRepetitionRunningStatus() {
        setAuth();

        when(repository.existsByUserId(USER_ID)).thenReturn(false);

        RepetitionRunningStatus repetitionRunningStatus = inputRepetitionService.isRepetitionRunning();

        verifyNoMoreInteractions(repository);

        assertThat(repetitionRunningStatus.isRunning()).isFalse();
        assertThat(repetitionRunningStatus.isPaused()).isFalse();
    }

    @Test
    void
    testStart_WhenRepetitionIsNotRunning_ThenPrepareRepetitionDataAndReturnIsRunningTrueAndPausedFalseRepetitionRunningStatus() {
        setAuth();

        long dictionaryId = 1L;
        RepetitionStartFilterRequest startFilterRequest = new RepetitionStartFilterRequest(
                RepetitionStartFilterRequest.RepetitionFilter.REPETITION_ONLY,
                new DefaultCriteriaFilter(DefaultCriteriaFilter.CriteriaFilterType.ALL));

        when(repository.existsByUserId(USER_ID)).thenReturn(false);
        doNothing().when(dictionaryAccessValidator).verifyUserHasDictionary(dictionaryId);

        RepetitionWordCriteriaLoader repetitionWordCriteriaLoader = mock(RepetitionWordCriteriaLoader.class);
        when(repetitionWordLoaderFactory.getLoader(
                startFilterRequest.getCriteriaFilter().getFilterType()))
                .thenReturn(repetitionWordCriteriaLoader);

        List<WordDto> loadedWords = prepareLoadedWords();
        when(repetitionWordCriteriaLoader.loadRepetitionWordPage(dictionaryId, startFilterRequest))
                .thenReturn(loadedWords);

        doNothing().when(repository).save(eq(USER_ID), repetitionDataCaptor.capture());

        RepetitionRunningStatus runningStatus = inputRepetitionService.start(dictionaryId, startFilterRequest);

        RepetitionData repetitionData = repetitionDataCaptor.getValue();

        assertThat(runningStatus.isRunning()).isTrue();
        assertThat(runningStatus.isPaused()).isFalse();
        assertThat(repetitionData.getRepetitionWords().size()).isEqualTo(2);
        assertThat(repetitionData.getPassedWords()).isEmpty();
        assertThat(repetitionData.getCurrentWord()).isEqualTo(loadedWords.getFirst());
        assertThat(repetitionData.getPauses()).isEmpty();
        assertThat(repetitionData.getTotalElements()).isEqualTo(loadedWords.size());
        assertThat(repetitionData.getUserId()).isEqualTo(USER_ID);
    }

    @Test
    void testStart_WhenRepetitionIsAlreadyRunning_ThenThrowRepetitionException() {
        setAuth();

        long dictionaryId = 1L;
        RepetitionStartFilterRequest startFilterRequest = new RepetitionStartFilterRequest(
                RepetitionStartFilterRequest.RepetitionFilter.REPETITION_ONLY,
                new DefaultCriteriaFilter(DefaultCriteriaFilter.CriteriaFilterType.ALL));

        when(repository.existsByUserId(USER_ID)).thenReturn(true);

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

        when(repository.existsByUserId(USER_ID)).thenReturn(false);
        doThrow(new ResourceNotFoundException(String.format(
                ExceptionStatusesEnum.RESOURCE_NOT_FOUND.getStatus(),
                Dictionary.class.getSimpleName(),
                dictionaryId)))
                .when(dictionaryAccessValidator)
                .verifyUserHasDictionary(dictionaryId);

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

        when(repository.existsByUserId(USER_ID)).thenReturn(false);
        doNothing().when(dictionaryAccessValidator).verifyUserHasDictionary(dictionaryId);

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

        when(repository.existsByUserId(USER_ID)).thenReturn(true);

        RepetitionData repetitionData =
                RepetitionData.builder().pauses(new ArrayList<>()).build();
        when(repository.getByUserId(USER_ID)).thenReturn(repetitionData);
        doNothing().when(repository).save(eq(USER_ID), repetitionDataCaptor.capture());

        RepetitionRunningStatus runningStatus = inputRepetitionService.pause();

        RepetitionData pausedRepetitionData = repetitionDataCaptor.getValue();
        assertThat(pausedRepetitionData.isPaused()).isTrue();

        assertThat(runningStatus.isRunning()).isTrue();
        assertThat(runningStatus.isPaused()).isTrue();
    }

    @Test
    void testPause_WhenRepetitionIsRunningAndAlreadyPaused_ThenThrowRepetitionException() {
        setAuth();

        when(repository.existsByUserId(USER_ID)).thenReturn(true);

        Pause alreadyStartedPause = Pause.start();
        RepetitionData repetitionData =
                RepetitionData.builder().pauses(List.of(alreadyStartedPause)).build();
        when(repository.getByUserId(USER_ID)).thenReturn(repetitionData);

        assertThatExceptionOfType(RepetitionException.class)
                .isThrownBy(() -> inputRepetitionService.pause())
                .withMessage("Pause already started");
    }

    @Test
    void testPause_WhenRepetitionIsNotRunning_ThenThrowRepetitionException() {
        setAuth();

        when(repository.existsByUserId(USER_ID)).thenReturn(false);

        assertThatExceptionOfType(RepetitionException.class)
                .isThrownBy(() -> inputRepetitionService.pause())
                .withMessage("Repetition is not running");
    }

    @Test
    void testUnpause_WhenRepetitionIsRunningAndPaused_ThenEndPause() {
        setAuth();

        when(repository.existsByUserId(USER_ID)).thenReturn(true);

        Pause startedPause = Pause.start();
        RepetitionData repetitionData =
                RepetitionData.builder().pauses(List.of(startedPause)).build();
        when(repository.getByUserId(USER_ID)).thenReturn(repetitionData);
        doNothing().when(repository).save(eq(USER_ID), repetitionDataCaptor.capture());

        RepetitionRunningStatus runningStatus = inputRepetitionService.unpause();

        RepetitionData unpausedRepetitionData = repetitionDataCaptor.getValue();
        assertThat(unpausedRepetitionData.isPaused()).isFalse();

        assertThat(runningStatus.isRunning()).isTrue();
        assertThat(runningStatus.isPaused()).isFalse();
    }

    @Test
    void testUnpause_WhenRepetitionIsRunningAndHasNoStartedPause_ThenThrowRepetitionException() {
        setAuth();

        when(repository.existsByUserId(USER_ID)).thenReturn(true);

        RepetitionData repetitionData =
                RepetitionData.builder().pauses(List.of()).build();
        when(repository.getByUserId(USER_ID)).thenReturn(repetitionData);

        assertThatExceptionOfType(RepetitionException.class)
                .isThrownBy(() -> inputRepetitionService.unpause())
                .withMessage("No pause to end");
    }

    @Test
    void testUnpause_WhenRepetitionIsNotRunning_ThenThrowRepetitionException() {
        setAuth();

        when(repository.existsByUserId(USER_ID)).thenReturn(false);

        assertThatExceptionOfType(RepetitionException.class)
                .isThrownBy(() -> inputRepetitionService.unpause())
                .withMessage("Repetition is not running");
    }

    @Test
    void
    testStop_WhenRepetitionIsRunningAndPassedWordsAreEmpty_ThenClearRepetitionDataAndReturnIsRunningFalseRepetitionRunningStatus() {
        setAuth();

        when(repository.existsByUserId(USER_ID)).thenReturn(true);

        RepetitionData repetitionData =
                RepetitionData.builder().passedWords(new ArrayList<>()).build();
        when(repository.getByUserId(USER_ID)).thenReturn(repetitionData);

        RepetitionRunningStatus runningStatus = inputRepetitionService.stop();

        verify(repository).clearByUserId(USER_ID);

        assertThat(runningStatus.isRunning()).isFalse();
        assertThat(runningStatus.isPaused()).isFalse();
    }

    @Test
    void testStop_WhenRepetitionIsRunningAndPassedWordsAreNotEmpty_ThenUpdateWordCountersAndClearRepetitionData() {
        setAuth();

        when(repository.existsByUserId(USER_ID)).thenReturn(true);

        List<WordDto> passedWords = List.of(
                new WordDto(1L, "word1", true, Set.of(), 1, null),
                new WordDto(2L, "word2", true, Set.of(), 1, null));
        RepetitionData repetitionData = RepetitionData.builder()
                .passedWords(passedWords)
                .build();
        when(repository.getByUserId(USER_ID)).thenReturn(repetitionData);

        RepetitionRunningStatus runningStatus = inputRepetitionService.stop();

        verify(wordCounterUpdateService).updateRightAnswersCounters(USER_ID, passedWords);
        verify(repository).clearByUserId(USER_ID);

        assertThat(runningStatus.isRunning()).isFalse();
        assertThat(runningStatus.isPaused()).isFalse();
    }

    @Test
    void testStop_WhenRepetitionIsNotRunning_ThenThrowRepetitionException() {
        setAuth();

        when(repository.existsByUserId(USER_ID)).thenReturn(false);

        assertThatExceptionOfType(RepetitionException.class)
                .isThrownBy(() -> inputRepetitionService.stop())
                .withMessage("Repetition is not running");
    }

    @Test
    void testHandleRepetitionMessage_WhenRequestIsStartOperationAndThereIsNextWord_ThenHandleStartOperationAndReturnWSResponse() {
        Authentication authentication = getAuth();

        WSRequest wsRequest = new WSRequest(null, WSRequest.Operation.START);

        WordDto currentWord = prepareRepetitionWords().pop();
        RepetitionData repetitionData = RepetitionData.builder()
                .currentWord(currentWord)
                .build();
        when(repository.getByUserId(USER_ID)).thenReturn(repetitionData);

        WSResponse response = mock(WSResponse.class);
        when(response.getWord()).thenReturn(currentWord.getWord());
        when(mapper.toWSResponse(repetitionData)).thenReturn(response);

        WSResponse currentWSResponse = inputRepetitionService.handleRepetitionMessage(authentication, wsRequest);

        assertThat(currentWSResponse.getWord()).isEqualTo(currentWord.getWord());
    }

    @Test
    void testHandleRepetitionMessage_WhenRequestIsStartOperationAndThereIsNoNextWord_ThenHandleStartOperationAndThrowRepetitionException() {
        Authentication authentication = getAuth();

        WSRequest wsRequest = new WSRequest(null, WSRequest.Operation.START);

        RepetitionData repetitionData = RepetitionData.builder()
                .currentWord(null)
                .build();
        when(repository.getByUserId(USER_ID)).thenReturn(repetitionData);

        assertThatExceptionOfType(RepetitionException.class)
                .isThrownBy(() -> inputRepetitionService.handleRepetitionMessage(authentication, wsRequest))
                .withMessage("No more words to repeat");
    }

    @Test
    void testHandleRepetitionMessage_WhenRequestIsSkipOperationAndThereIsNextWord_ThenHandleSkipOperationAndReturnWSResponse() {
        Authentication authentication = getAuth();

        WSRequest wsRequest = new WSRequest(null, WSRequest.Operation.SKIP);

        Deque<WordDto> repetitionWords = prepareRepetitionWords();
        WordDto currentWord = repetitionWords.pop();
        RepetitionData repetitionData = RepetitionData.builder()
                .repetitionWords(repetitionWords)
                .currentWord(currentWord)
                .build();
        when(repository.getByUserId(USER_ID)).thenReturn(repetitionData);
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
                new WordDto(1L, "word1", true, Set.of(), 0, null),
                new WordDto(2L, "word2", true, Set.of(), 0, null),
                new WordDto(3L, "word3", true, Set.of(), 0, null)));
    }

    private Deque<WordDto> prepareRepetitionWords() {
        return new ArrayDeque<>(prepareLoadedWords());
    }
}
