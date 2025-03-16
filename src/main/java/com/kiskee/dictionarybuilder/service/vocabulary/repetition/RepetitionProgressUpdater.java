package com.kiskee.dictionarybuilder.service.vocabulary.repetition;

import com.kiskee.dictionarybuilder.model.dto.vocabulary.word.WordDto;
import com.kiskee.dictionarybuilder.model.entity.redis.repetition.RepetitionData;
import com.kiskee.dictionarybuilder.service.report.UpdateStatisticReportManager;
import com.kiskee.dictionarybuilder.service.vocabulary.word.WordCounterUpdateService;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
public class RepetitionProgressUpdater {

    private final WordCounterUpdateService wordCounterUpdateService;
    private final UpdateStatisticReportManager updateStatisticReportManager;

    @Value("${vocabulary.repetition.words-to-update-count}")
    private int wordsToUpdateCount;

    public void updateRightAnswers(RepetitionData repetitionData) {
        List<WordDto> passedWords = repetitionData.getPassedWords();
        if (!repetitionData.isShared()
                && CollectionUtils.isNotEmpty(passedWords)
                && passedWords.size() >= wordsToUpdateCount) {
            wordCounterUpdateService.updateRightAnswersCounters(
                    repetitionData.getUserId(), new ArrayList<>(passedWords));
            log.info("Updated right-answers counters for {} words", passedWords.size());
            passedWords.clear();
            log.info("Passed words is cleared");
        }
        log.info("Updated right-answers counters haven't been updated for words");
    }

    public void updateRepetitionProgress(RepetitionData repetitionData) {
        if (repetitionData.getTotalElementsPassed() > 0 && !repetitionData.isShared()) {
            updateStatisticReportManager.updateRepetitionProgress(repetitionData.toResult());
        }
    }
}
