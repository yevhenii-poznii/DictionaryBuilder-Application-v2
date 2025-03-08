package com.kiskee.dictionarybuilder.service.vocabulary.repetition.handler.state.impl;

import com.kiskee.dictionarybuilder.enums.vocabulary.filter.CriteriaFilterType;
import com.kiskee.dictionarybuilder.exception.repetition.RepetitionException;
import com.kiskee.dictionarybuilder.model.dto.repetition.start.RepetitionStartFilterRequest;
import com.kiskee.dictionarybuilder.model.dto.repetition.start.RepetitionStartRequest;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.dictionary.DictionaryDto;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.word.WordDto;
import com.kiskee.dictionarybuilder.model.entity.redis.repetition.RepetitionData;
import com.kiskee.dictionarybuilder.repository.redis.RepetitionDataRepository;
import com.kiskee.dictionarybuilder.service.vocabulary.dictionary.DictionaryAccessValidator;
import com.kiskee.dictionarybuilder.service.vocabulary.loader.factory.WordLoaderFactory;
import com.kiskee.dictionarybuilder.service.vocabulary.repetition.RepetitionDataFactory;
import com.kiskee.dictionarybuilder.service.vocabulary.repetition.RepetitionProgressUpdater;
import com.kiskee.dictionarybuilder.service.vocabulary.repetition.handler.state.AbstractRepetitionStateHandler;
import com.kiskee.dictionarybuilder.service.vocabulary.repetition.handler.state.RepetitionStateHandler;
import com.kiskee.dictionarybuilder.service.vocabulary.repetition.loader.criteria.RepetitionWordCriteriaLoader;
import com.kiskee.dictionarybuilder.util.TimeZoneContextHolder;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Order(1)
public class OwnerRepetitionStateHandler extends AbstractRepetitionStateHandler implements RepetitionStateHandler {

    public OwnerRepetitionStateHandler(
            RepetitionDataRepository repository,
            WordLoaderFactory<CriteriaFilterType, RepetitionWordCriteriaLoader> wordLoaderFactory,
            DictionaryAccessValidator dictionaryAccessValidator,
            RepetitionProgressUpdater repetitionProgressUpdater) {
        super(repository, dictionaryAccessValidator, wordLoaderFactory, repetitionProgressUpdater);
    }

    @Override
    public Class<? extends RepetitionStartRequest> getRequestType() {
        return RepetitionStartFilterRequest.class;
    }

    @Override
    protected DictionaryDto verifyUserHasAccessToDictionary(ProcessingData processingData) {
        if (processingData instanceof RepetitionProcessingData repetitionProcessingData) {
            return getDictionaryDtoByDictionaryIdAndUserId(
                    repetitionProcessingData.dictionaryId(), repetitionProcessingData.userId());
        }
        throw new RepetitionException("Invalid repetition processing data");
    }

    @Override
    protected RepetitionData buildRepetitionData(
            List<WordDto> words, DictionaryDto dictionaryDto, UUID userId, RepetitionStartRequest request) {
        return RepetitionDataFactory.createRepetitionData(
                request.getRepetitionType(),
                words,
                dictionaryDto,
                userId,
                TimeZoneContextHolder.getTimeZone(),
                request.getReversed());
    }
}
