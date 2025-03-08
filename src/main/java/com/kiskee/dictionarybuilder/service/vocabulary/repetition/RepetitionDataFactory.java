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
        return createRepetitionData(repetitionType, loadedWords, dictionaryDto, userId, userTimeZone, reversed, false);
    }

    public static RepetitionData createRepetitionData(
            RepetitionType repetitionType,
            List<WordDto> loadedWords,
            DictionaryDto dictionaryDto,
            UUID userId,
            ZoneId userTimeZone,
            boolean reversed,
            boolean shared) {
        return switch (repetitionType) {
            case INPUT -> new RepetitionData(
                    loadedWords, dictionaryDto, userId, userTimeZone, RepetitionType.INPUT, reversed, shared);
            case CHOICE -> {
                validateChoiceRepetitionData(loadedWords, dictionaryDto, userId);
                yield new ChoiceRepetitionData(
                        loadedWords, dictionaryDto, userId, userTimeZone, RepetitionType.CHOICE, reversed, shared);
            }
        };
    }

    private static void validateChoiceRepetitionData(
            List<WordDto> loadedWords, DictionaryDto dictionaryDto, UUID userId) {
        if (loadedWords.size() < 4) {
            log.info(
                    "Not enough words to start repetition for dictionary [{}] for user: [{}]",
                    dictionaryDto.getDictionaryName(),
                    userId);
            throw new RepetitionException("Not enough words to start repetition");
        }
    }
}
