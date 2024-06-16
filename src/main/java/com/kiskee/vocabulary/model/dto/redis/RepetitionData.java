package com.kiskee.vocabulary.model.dto.redis;

import com.kiskee.vocabulary.model.dto.vocabulary.word.WordDto;
import java.util.Deque;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
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

    public void addPassedWord() {
        passedWords.add(getCurrentWord());
        setNext();
    }

    public String setNext() {
        WordDto next = repetitionWords.pop();
        setCurrentWord(next);
        return next.getWord();
    }
}
