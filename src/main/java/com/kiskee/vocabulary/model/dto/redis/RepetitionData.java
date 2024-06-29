package com.kiskee.vocabulary.model.dto.redis;

import com.kiskee.vocabulary.exception.repetition.RepetitionException;
import com.kiskee.vocabulary.model.dto.vocabulary.word.WordDto;
import java.time.Instant;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RepetitionData {

    private Deque<WordDto> repetitionWords;
    private Deque<WordDto> passedWords;
    private WordDto currentWord;
    private Instant startTime;
    private Instant endTime;
    private List<Pause> pauses;
    private int rightAnswersCount;
    private int wrongAnswersCount;
    private int skippedWordsCount;
    private int totalElements;
    private int totalElementsPassed;
    private UUID userId;

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
        this.skippedWordsCount++;
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
        }
        throw new RepetitionException("Pause already started");
    }

    public void endPause() throws RepetitionException {
        if (this.isPaused()) {
            this.getPauses().getLast().end();
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

    private void incrementTotalElementsPassed() {
        this.totalElementsPassed++;
    }

    private void incrementRightAnswersCount() {
        this.rightAnswersCount++;
        this.currentWord.incrementCounterRightAnswers();
        incrementTotalElementsPassed();
    }

    private void incrementWrongAnswersCount() {
        this.wrongAnswersCount++;
        this.currentWord.decrementCounterRightAnswers();
        incrementTotalElementsPassed();
    }
}
