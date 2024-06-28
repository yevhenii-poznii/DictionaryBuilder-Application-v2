package com.kiskee.vocabulary.model.dto.redis;

import com.kiskee.vocabulary.exception.repetition.RepetitionException;
import com.kiskee.vocabulary.model.dto.vocabulary.word.WordDto;

import java.time.Instant;
import java.util.Deque;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    //TODO implement skipped words
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
        this.addPassedWord();
        return this;
    }

    public WordDto setNext() {
        if (this.getRepetitionWords().isEmpty()) {
            setCurrentWord(null);
            return null;
        }
        WordDto next = this.getRepetitionWords().pop();
        this.setCurrentWord(next);
        return next;
    }

    public void startPause() {
        this.getPauses().add(Pause.start());
    }

    public void endPause() throws RepetitionException {
        if (this.getPauses().isEmpty() || Objects.nonNull(this.getPauses().getLast().getEndTime())) {
            throw new RepetitionException("No pause to end");
        }
        this.getPauses().getLast().end();
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

    private void addPassedWord() throws NoSuchElementException {
        this.passedWords.add(getCurrentWord());
        setNext();
    }
}
