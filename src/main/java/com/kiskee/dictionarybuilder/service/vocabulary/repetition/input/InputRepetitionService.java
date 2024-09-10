package com.kiskee.dictionarybuilder.service.vocabulary.repetition.input;

import com.kiskee.dictionarybuilder.enums.repetition.RepetitionType;
import com.kiskee.dictionarybuilder.mapper.repetition.RepetitionWordMapper;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.dictionary.DictionaryDto;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.word.WordDto;
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
public class InputRepetitionService extends AbstractRepetitionService implements InputRepetitionHandler {

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
        return new RepetitionData(loadedWords, dictionaryDto, userId, userTimeZone, RepetitionType.INPUT, reversed);
    }
}
