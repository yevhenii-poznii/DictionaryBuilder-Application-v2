package com.kiskee.dictionarybuilder.service.vocabulary.repetition;

import com.kiskee.dictionarybuilder.enums.repetition.RepetitionType;
import com.kiskee.dictionarybuilder.exception.repetition.RepetitionException;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.dictionary.DictionaryDto;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.word.WordDto;
import com.kiskee.dictionarybuilder.model.entity.redis.repetition.ChoiceRepetitionData;
import com.kiskee.dictionarybuilder.model.entity.redis.repetition.RepetitionData;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RepetitionDataFactory {

    public static RepetitionData createRepetitionData(
            RepetitionType repetitionType,
            List<WordDto> loadedWords,
            DictionaryDto dictionaryDto,
            UUID userId,
            ZoneId userTimeZone,
            boolean reversed) {
        return switch (repetitionType) {
            case INPUT -> createInputRepetitionData(loadedWords, dictionaryDto, userId, userTimeZone, reversed);
            case CHOICE -> createChoiceRepetitionData(loadedWords, dictionaryDto, userId, userTimeZone, reversed);
        };
    }

    private static RepetitionData createInputRepetitionData(
            List<WordDto> loadedWords,
            DictionaryDto dictionaryDto,
            UUID userId,
            ZoneId userTimeZone,
            boolean reversed) {
        return new RepetitionData(loadedWords, dictionaryDto, userId, userTimeZone, RepetitionType.INPUT, reversed);
    }

    private static ChoiceRepetitionData createChoiceRepetitionData(
            List<WordDto> loadedWords,
            DictionaryDto dictionaryDto,
            UUID userId,
            ZoneId userTimeZone,
            boolean reversed) {
        if (loadedWords.size() < 4) {
            log.info(
                    "Not enough words to start repetition for dictionary [{}] for user: [{}]",
                    dictionaryDto.getDictionaryName(),
                    userId);
            throw new RepetitionException("Not enough words to start repetition");
        }
        return new ChoiceRepetitionData(
                loadedWords, dictionaryDto, userId, userTimeZone, RepetitionType.CHOICE, reversed);
    }
}
