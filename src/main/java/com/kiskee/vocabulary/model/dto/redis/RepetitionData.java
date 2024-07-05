package com.kiskee.vocabulary.model.dto.redis;

import com.kiskee.vocabulary.exception.repetition.RepetitionException;
import com.kiskee.vocabulary.model.dto.report.RepetitionResultDataDto;
import com.kiskee.vocabulary.model.dto.vocabulary.word.WordDto;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.apache.commons.collections4.CollectionUtils;

@Data
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RepetitionData extends RepetitionResultDataDto {

    private Deque<WordDto> repetitionWords;
    private List<WordDto> passedWords;
    private WordDto currentWord;

    public RepetitionData(List<WordDto> repetitionWords, long dictionaryId, UUID userId) {
        this.repetitionWords = new ArrayDeque<>(repetitionWords);
        this.passedWords = new ArrayList<>();
        this.currentWord = this.repetitionWords.pop();
        super.setPauses(new ArrayList<>());
        super.setStartTime(Instant.now());
        super.setTotalElements(repetitionWords.size());
        super.setDictionaryId(dictionaryId);
        super.setUserId(userId);
    }

    public RepetitionResultDataDto toResult() {
        return new RepetitionResultDataDto(this);
    }

    public RepetitionData updateData(boolean isCorrect) {
        if (isCorrect) {
            this.incrementRightAnswersCount();
        } else {
            this.incrementWrongAnswersCount();
        }
        this.getPassedWords().add(getCurrentWord());
        this.setNext();
        return this;
    }

    public RepetitionData skip() {
        this.incrementSkippedWordsCount();
        this.setNext();
        return this;
    }

    public boolean isPaused() {
        return CollectionUtils.isNotEmpty(this.getPauses())
                && Objects.isNull(this.getPauses().getLast().getEndTime());
    }

    public void startPause() throws RepetitionException {
        if (!this.isPaused()) {
            this.getPauses().add(Pause.start());
            return;
        }
        throw new RepetitionException("Pause already started");
    }

    public void endPause() throws RepetitionException {
        if (this.isPaused()) {
            this.getPauses().getLast().end();
            return;
        }
        throw new RepetitionException("No pause to end");
    }

    private void setNext() {
        if (this.getRepetitionWords().isEmpty()) {
            setCurrentWord(null);
            return;
        }
        WordDto next = this.getRepetitionWords().pop();
        this.setCurrentWord(next);
    }

    protected void incrementRightAnswersCount() {
        super.incrementRightAnswersCount();
        this.currentWord.incrementCounterRightAnswers();
        this.incrementTotalElementsPassed();
    }

    protected void incrementWrongAnswersCount() {
        super.incrementWrongAnswersCount();
        this.currentWord.decrementCounterRightAnswers();
        incrementTotalElementsPassed();
    }
}
