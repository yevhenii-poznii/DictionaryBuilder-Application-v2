package com.kiskee.dictionarybuilder.model.entity.redis.repetition;

import com.kiskee.dictionarybuilder.enums.repetition.RepetitionType;
import com.kiskee.dictionarybuilder.exception.repetition.RepetitionException;
import com.kiskee.dictionarybuilder.model.dto.repetition.RepetitionResultDataDto;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.dictionary.DictionaryDto;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.word.WordDto;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.word.WordTranslationDto;
import jakarta.persistence.Id;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
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
public class RepetitionData extends RepetitionResultDataDto implements RepetitionDataDto {

    @Id
    private String id;

    private List<WordDto> repetitionWords = new ArrayList<>();
    private List<WordDto> passedWords = new ArrayList<>();
    private WordDto currentWord;
    private String word;
    private Set<String> translations;
    private boolean reversed;
    private boolean shared;
    private RepetitionType repetitionType;

    public RepetitionData(
            List<WordDto> repetitionWords,
            DictionaryDto dictionaryDto,
            UUID userId,
            ZoneId userTimeZone,
            RepetitionType repetitionType,
            boolean reversed,
            boolean shared) {
        this(repetitionWords, dictionaryDto, userId, userTimeZone, repetitionType, reversed);
        this.shared = shared;
    }

    public RepetitionData(
            List<WordDto> repetitionWords,
            DictionaryDto dictionaryDto,
            UUID userId,
            ZoneId userTimeZone,
            RepetitionType repetitionType,
            boolean reversed) {
        super(userId, dictionaryDto.getId(), dictionaryDto.getDictionaryName(), userTimeZone, repetitionWords.size());
        //        this.setId(userId.toString());
        this.id = userId.toString();
        this.repetitionWords = new ArrayList<>(repetitionWords);
        this.repetitionType = repetitionType;
        this.reversed = reversed;
        this.setNext();
        //        super.setUserTimeZone(userTimeZone);
        //        super.setStartTime(Instant.now());
        //        super.setTotalElements(repetitionWords.size());
        //        super.setDictionaryId(dictionaryDto.getId());
        //        super.setDictionaryName(dictionaryDto.getDictionaryName());
        //        super.setUserId(userId);
    }

    public RepetitionResultDataDto toResult() {
        if (this.isPaused()) {
            this.endPause();
        }
        return this;
        //        return RepetitionResultDataDto.builder()
        //                .userId(UUID.fromString(this.getId()))
        //                .dictionaryId(this.getDictionaryId())
        //                .dictionaryName(this.getDictionaryName())
        //                .userTimeZone(this.getUserTimeZone())
        //                .startTime(this.getStartTime())
        //                .endTime(Instant.now())
        //                .pauses(this.getPauses())
        //                .rightAnswersCount(this.getRightAnswersCount())
        //                .wrongAnswersCount(this.getWrongAnswersCount())
        //                .skippedWordsCount(this.getSkippedWordsCount())
        //                .totalElements(this.getTotalElements())
        //                .totalElementsPassed(this.getTotalElementsPassed())
        //                .build();
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

    protected void setNext() {
        if (this.getRepetitionWords().isEmpty()) {
            setCurrentWord(null);
            return;
        }
        WordDto next = this.getRepetitionWords().removeLast();
        this.setCurrentWord(next);
        this.setWordAndTranslations(next);
    }

    protected void setWordAndTranslations(WordDto next) {
        Set<String> translationsSet = next.getWordTranslations().stream()
                .map(WordTranslationDto::getTranslation)
                .collect(Collectors.toSet());
        if (!this.isReversed()) {
            this.word = next.getWord();
            this.translations = translationsSet;
            return;
        }
        this.word = String.join(", ", translationsSet);
        this.translations = Arrays.stream(next.getWord().split("\\s*,\\s*")).collect(Collectors.toSet());
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
