package com.kiskee.vocabulary.model.entity.redis.repetition;

import com.kiskee.vocabulary.exception.repetition.RepetitionException;
import com.kiskee.vocabulary.model.dto.repetition.RepetitionResultDataDto;
import com.kiskee.vocabulary.model.dto.vocabulary.word.WordDto;
import jakarta.persistence.Id;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
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
import org.springframework.data.redis.core.RedisHash;

@Data
@AllArgsConstructor
@RedisHash("repetitionData")
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RepetitionData extends RepetitionResultDataDto {

    @Id
    private String id;

    private List<WordDto> repetitionWords = new ArrayList<>();
    private List<WordDto> passedWords = new ArrayList<>();
    private WordDto currentWord;

    public RepetitionData(
            List<WordDto> repetitionWords, long dictionaryId, String dictionaryName, UUID userId, ZoneId userTimeZone) {
        this.setId(userId.toString());
        this.repetitionWords = new ArrayList<>(repetitionWords);
        this.currentWord = this.repetitionWords.removeLast();
        super.setUserTimeZone(userTimeZone);
        super.setStartTime(Instant.now());
        super.setTotalElements(repetitionWords.size());
        super.setDictionaryId(dictionaryId);
        super.setDictionaryName(dictionaryName);
        super.setUserId(userId);
    }

    public RepetitionResultDataDto toResult() {
        if (this.isPaused()) {
            this.endPause();
        }
        return RepetitionResultDataDto.builder()
                .userId(UUID.fromString(this.getId()))
                .dictionaryId(this.getDictionaryId())
                .dictionaryName(this.getDictionaryName())
                .userTimeZone(this.getUserTimeZone())
                .startTime(this.getStartTime())
                .endTime(Instant.now())
                .pauses(this.getPauses())
                .rightAnswersCount(this.getRightAnswersCount())
                .wrongAnswersCount(this.getWrongAnswersCount())
                .skippedWordsCount(this.getSkippedWordsCount())
                .totalElements(this.getTotalElements())
                .totalElementsPassed(this.getTotalElementsPassed())
                .build();
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
        WordDto next = this.getRepetitionWords().removeLast();
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
