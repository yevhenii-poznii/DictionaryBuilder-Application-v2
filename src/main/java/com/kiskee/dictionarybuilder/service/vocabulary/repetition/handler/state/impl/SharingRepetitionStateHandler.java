package com.kiskee.dictionarybuilder.service.vocabulary.repetition.handler.state.impl;

import com.kiskee.dictionarybuilder.enums.vocabulary.filter.CriteriaFilterType;
import com.kiskee.dictionarybuilder.exception.repetition.RepetitionException;
import com.kiskee.dictionarybuilder.model.dto.repetition.start.RepetitionStartRequest;
import com.kiskee.dictionarybuilder.model.dto.repetition.start.SharingRepetitionStartFilterRequest;
import com.kiskee.dictionarybuilder.model.dto.token.share.SharingTokenData;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.dictionary.DictionaryDto;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.word.WordDto;
import com.kiskee.dictionarybuilder.model.entity.redis.repetition.RepetitionData;
import com.kiskee.dictionarybuilder.repository.redis.RepetitionDataRepository;
import com.kiskee.dictionarybuilder.service.security.token.deserializer.TokenDeserializationHandler;
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
@Order(2)
public class SharingRepetitionStateHandler extends AbstractRepetitionStateHandler implements RepetitionStateHandler {

    private final TokenDeserializationHandler<SharingTokenData> tokenDeserializationHandler;

    public SharingRepetitionStateHandler(
            RepetitionDataRepository repository,
            WordLoaderFactory<CriteriaFilterType, RepetitionWordCriteriaLoader> wordLoaderFactory,
            DictionaryAccessValidator dictionaryAccessValidator,
            RepetitionProgressUpdater repetitionProgressUpdater,
            TokenDeserializationHandler<SharingTokenData> tokenDeserializationHandler) {
        super(repository, dictionaryAccessValidator, wordLoaderFactory, repetitionProgressUpdater);
        this.tokenDeserializationHandler = tokenDeserializationHandler;
    }

    @Override
    public Class<? extends RepetitionStartRequest> getRequestType() {
        return SharingRepetitionStartFilterRequest.class;
    }

    @Override
    protected DictionaryDto verifyUserHasAccessToDictionary(ProcessingData processingData) {
        if (processingData instanceof SharingRepetitionProcessingData sharingRepetitionProcessingData) {
            SharingTokenData sharingTokenData = tokenDeserializationHandler.deserializeToken(
                    sharingRepetitionProcessingData.sharingToken(), SharingTokenData.class);
            return getDictionaryDtoByDictionaryIdAndUserId(
                    sharingTokenData.getDictionaryId(), sharingTokenData.getUserId());
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
                request.getReversed(),
                true);
    }
}
