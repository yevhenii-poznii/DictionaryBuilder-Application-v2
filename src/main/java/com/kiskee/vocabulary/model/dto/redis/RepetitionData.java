package com.kiskee.vocabulary.model.dto.redis;

import com.kiskee.vocabulary.model.dto.vocabulary.word.WordDto;
import java.util.Deque;
import java.util.NoSuchElementException;
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
    private int rightAnswersCount;
    private int wrongAnswersCount;
    private int totalElements;
    private int totalElementsPassed;
    private UUID userId;

    public void incrementTotalElementsPassed() {
        this.totalElementsPassed++;
    }

    public void incrementRightAnswersCount() {
        this.rightAnswersCount++;
        currentWord.incrementCounterRightAnswers();
    }

    public void incrementWrongAnswersCount() {
        this.wrongAnswersCount++;
        currentWord.decrementCounterRightAnswers();
    }

    public void addPassedWord() throws NoSuchElementException {
        passedWords.add(getCurrentWord());
        setNext();
    }

    public String setNext() throws NoSuchElementException {
        WordDto next = repetitionWords.pop();
        setCurrentWord(next);
        return next.getWord();
    }
}
