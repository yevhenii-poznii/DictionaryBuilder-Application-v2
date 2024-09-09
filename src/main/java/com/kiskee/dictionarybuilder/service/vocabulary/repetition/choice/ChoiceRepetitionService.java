package com.kiskee.dictionarybuilder.service.vocabulary.repetition.choice;

import com.kiskee.dictionarybuilder.enums.repetition.RepetitionType;
import com.kiskee.dictionarybuilder.exception.repetition.RepetitionException;
import com.kiskee.dictionarybuilder.mapper.repetition.RepetitionWordMapper;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.dictionary.DictionaryDto;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.word.WordDto;
import com.kiskee.dictionarybuilder.model.entity.redis.repetition.ChoiceRepetitionData;
import com.kiskee.dictionarybuilder.model.entity.redis.repetition.RepetitionData;
import com.kiskee.dictionarybuilder.repository.redis.RepetitionDataRepository;
import com.kiskee.dictionarybuilder.service.report.StatisticUpdateReportManager;
import com.kiskee.dictionarybuilder.service.vocabulary.dictionary.DictionaryAccessValidator;
import com.kiskee.dictionarybuilder.service.vocabulary.repetition.AbstractRepetitionService;
import com.kiskee.dictionarybuilder.service.vocabulary.repetition.loader.RepetitionWordLoaderFactory;
import com.kiskee.dictionarybuilder.service.vocabulary.word.WordCounterUpdateService;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Getter
@Service
@RequiredArgsConstructor
public class ChoiceRepetitionService extends AbstractRepetitionService implements ChoiceRepetitionHandler {

    private final RepetitionWordLoaderFactory repetitionWordLoaderFactory;
    private final RepetitionDataRepository repository;
    private final RepetitionWordMapper mapper;
    private final DictionaryAccessValidator dictionaryAccessValidator;
    private final WordCounterUpdateService wordCounterUpdateService;
    private final StatisticUpdateReportManager statisticUpdateReportManager;

    @Value("${vocabulary.repetition.words-to-update-count}")
    private int wordsToUpdateCount;

    @Override
    protected RepetitionData buildRepetitionData(
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
