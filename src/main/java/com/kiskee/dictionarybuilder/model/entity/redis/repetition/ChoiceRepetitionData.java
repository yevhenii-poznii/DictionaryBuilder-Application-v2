package com.kiskee.dictionarybuilder.model.entity.redis.repetition;

import com.kiskee.dictionarybuilder.enums.repetition.RepetitionType;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.dictionary.DictionaryDto;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.word.WordDto;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.word.WordTranslationDto;
import com.kiskee.dictionarybuilder.util.RandomElementsPicker;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.redis.core.RedisHash;

@Data
@AllArgsConstructor
@RedisHash("repetitionData")
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChoiceRepetitionData extends RepetitionData implements RepetitionDataDto {

    private List<String> translationOptions;
    private Integer currentWordIndex;

    public ChoiceRepetitionData(
            List<WordDto> repetitionWords,
            DictionaryDto dictionaryDto,
            UUID userId,
            ZoneId userTimeZone,
            RepetitionType repetitionType,
            boolean reversed) {
        super(repetitionWords, dictionaryDto, userId, userTimeZone, repetitionType, reversed);
    }

    @Override
    protected void setNext() {
        if (Objects.isNull(currentWordIndex)) {
            this.currentWordIndex = this.getRepetitionWords().size();
        }
        this.currentWordIndex--;
        if (this.currentWordIndex < 0) {
            setCurrentWord(null);
            this.translationOptions = null;
            return;
        }
        WordDto next = this.getRepetitionWords().get(this.currentWordIndex);
        this.setCurrentWord(next);
        this.setWordAndTranslations(next);
        this.setTranslationOptions();
    }

    private void setTranslationOptions() {
        Set<Integer> randomElements =
                RandomElementsPicker.getRandomElements(this.getRepetitionWords(), this.currentWordIndex);
        List<String> randomTranslations = collectRandomTranslations(randomElements);
        Collections.shuffle(randomTranslations);
        this.translationOptions = randomTranslations;
    }

    private List<String> collectRandomTranslations(Set<Integer> randomElements) {
        Stream<WordDto> wordDtoStream = randomElements.stream().map(getRepetitionWords()::get);
        if (this.isReversed()) {
            return wordDtoStream.map(WordDto::getWord).collect(Collectors.toList());
        }
        return wordDtoStream
                .map(WordDto::getWordTranslations)
                .map(ArrayList::new)
                .map(RandomElementsPicker::getRandomElement)
                .map(WordTranslationDto::getTranslation)
                .collect(Collectors.toList());
    }
}
