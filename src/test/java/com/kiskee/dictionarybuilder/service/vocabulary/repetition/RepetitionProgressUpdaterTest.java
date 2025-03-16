package com.kiskee.dictionarybuilder.service.vocabulary.repetition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.kiskee.dictionarybuilder.model.dto.vocabulary.word.WordDto;
import com.kiskee.dictionarybuilder.model.entity.redis.repetition.RepetitionData;
import com.kiskee.dictionarybuilder.service.report.UpdateStatisticReportManager;
import com.kiskee.dictionarybuilder.service.vocabulary.word.WordCounterUpdateService;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class RepetitionProgressUpdaterTest {

    @InjectMocks
    private RepetitionProgressUpdater repetitionProgressUpdater;

    @Mock
    private WordCounterUpdateService wordCounterUpdateService;

    @Mock
    private UpdateStatisticReportManager updateStatisticReportManager;

    private static final UUID USER_ID = UUID.fromString("78c87bb3-01b6-41ca-8329-247a72162868");

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(repetitionProgressUpdater, "wordsToUpdateCount", 10);
    }

    @Test
    void testGetWordCounterUpdateService() {
        assertThat(repetitionProgressUpdater.getWordCounterUpdateService()).isEqualTo(wordCounterUpdateService);
    }

    @Test
    void testGetUpdateStatisticReportManager() {
        assertThat(repetitionProgressUpdater.getUpdateStatisticReportManager()).isEqualTo(updateStatisticReportManager);
    }

    @Test
    void testGetWordsToUpdateCount() {
        assertThat(repetitionProgressUpdater.getWordsToUpdateCount()).isEqualTo(10);
    }

    @Test
    void testUpdateRightAnswers_WhenRepetitionIsShared_ThenDoNothing() {
        RepetitionData repetitionData = mock(RepetitionData.class);
        when(repetitionData.isShared()).thenReturn(Boolean.TRUE);

        repetitionProgressUpdater.updateRightAnswers(repetitionData);

        verifyNoInteractions(wordCounterUpdateService);
    }

    @Test
    void testUpdateRightAnswers_WhenPassedWordsListIsEmpty_ThenDoNothing() {
        RepetitionData repetitionData = mock(RepetitionData.class);
        when(repetitionData.getPassedWords()).thenReturn(List.of());

        repetitionProgressUpdater.updateRightAnswers(repetitionData);

        verifyNoInteractions(wordCounterUpdateService);
    }

    @Test
    void testUpdateRightAnswers_WhenPassedWordsListSizeLessThanWordsToUpdateCount_ThenDoNothing() {
        RepetitionData repetitionData = mock(RepetitionData.class);
        when(repetitionData.getPassedWords()).thenReturn(List.of(mock(WordDto.class)));

        repetitionProgressUpdater.updateRightAnswers(repetitionData);

        verifyNoInteractions(wordCounterUpdateService);
    }

    @Test
    void testUpdateRightAnswers_WhenPassedWordsExceedWordsToUpdateCount_ThenUpdateRightAnswersCountersForPassedWords() {
        RepetitionData repetitionData = mock(RepetitionData.class);
        List<WordDto> passedWords = Stream.iterate(1, i -> i + 1)
                .limit(10)
                .map(i -> mock(WordDto.class))
                .collect(Collectors.toList());
        when(repetitionData.getPassedWords()).thenReturn(passedWords);
        when(repetitionData.getUserId()).thenReturn(USER_ID);

        repetitionProgressUpdater.updateRightAnswers(repetitionData);

        verify(wordCounterUpdateService).updateRightAnswersCounters(any(UUID.class), anyList());
    }

    @Test
    void testUpdateRepetitionProgress_WhenTotalElementsPassedIsZero_ThenDoNothing() {
        RepetitionData repetitionData = mock(RepetitionData.class);
        when(repetitionData.getTotalElementsPassed()).thenReturn(0);

        repetitionProgressUpdater.updateRepetitionProgress(repetitionData);

        verifyNoInteractions(updateStatisticReportManager);
    }

    @Test
    void testUpdateRepetitionProgress_WhenRepetitionIsShared_ThenDoNothing() {
        RepetitionData repetitionData = mock(RepetitionData.class);
        when(repetitionData.getTotalElementsPassed()).thenReturn(1);
        when(repetitionData.isShared()).thenReturn(Boolean.TRUE);

        repetitionProgressUpdater.updateRepetitionProgress(repetitionData);

        verifyNoInteractions(updateStatisticReportManager);
    }

    @Test
    void
            testUpdateRepetitionProgress_WhenTotalElementsPassedGreaterThanZeroAndRepetitionIsNotShared_ThenUpdateRepetitionProgress() {
        RepetitionData repetitionData = mock(RepetitionData.class);
        when(repetitionData.getTotalElementsPassed()).thenReturn(1);
        when(repetitionData.isShared()).thenReturn(Boolean.FALSE);

        repetitionProgressUpdater.updateRepetitionProgress(repetitionData);

        verify(updateStatisticReportManager).updateRepetitionProgress(repetitionData.toResult());
    }
}
