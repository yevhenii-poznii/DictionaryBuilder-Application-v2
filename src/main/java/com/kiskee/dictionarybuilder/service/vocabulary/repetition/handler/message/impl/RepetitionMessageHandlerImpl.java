package com.kiskee.dictionarybuilder.service.vocabulary.repetition.handler.message.impl;

import com.kiskee.dictionarybuilder.exception.repetition.RepetitionException;
import com.kiskee.dictionarybuilder.mapper.repetition.RepetitionWordMapper;
import com.kiskee.dictionarybuilder.model.dto.repetition.RepetitionRequest;
import com.kiskee.dictionarybuilder.model.dto.repetition.message.WSRequest;
import com.kiskee.dictionarybuilder.model.dto.repetition.message.WSResponse;
import com.kiskee.dictionarybuilder.model.entity.redis.repetition.RepetitionData;
import com.kiskee.dictionarybuilder.repository.redis.RepetitionDataRepository;
import com.kiskee.dictionarybuilder.service.vocabulary.repetition.RepetitionProgressUpdater;
import com.kiskee.dictionarybuilder.service.vocabulary.repetition.handler.AbstractRepetitionHandler;
import com.kiskee.dictionarybuilder.service.vocabulary.repetition.handler.message.RepetitionMessageHandler;
import com.kiskee.dictionarybuilder.util.IdentityUtil;
import com.kiskee.dictionarybuilder.util.repetition.RepetitionUtils;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Order(3)
@Getter(AccessLevel.PROTECTED)
public class RepetitionMessageHandlerImpl extends AbstractRepetitionHandler implements RepetitionMessageHandler {

    private final RepetitionWordMapper mapper;

    @Value("${vocabulary.repetition.words-to-update-count}")
    private int wordsToUpdateCount;

    public RepetitionMessageHandlerImpl(
            RepetitionDataRepository repository,
            RepetitionProgressUpdater repetitionProgressUpdater,
            RepetitionWordMapper repetitionWordMapper) {
        super(repository, repetitionProgressUpdater);
        this.mapper = repetitionWordMapper;
    }

    @Override
    public Class<? extends RepetitionRequest> getRequestType() {
        return WSRequest.class;
    }

    public WSResponse handleRepetitionMessage(Authentication authentication, WSRequest request) {
        UUID userId = IdentityUtil.getUserId(authentication);
        RepetitionData repetitionData = getRepetitionData(userId);
        return switch (request.getOperation()) {
            case START -> handleStartOperation(repetitionData);
            case SKIP -> handleSkipOperation(repetitionData, userId);
            case CHECK -> handleCheckOperation(request, repetitionData, userId);
        };
    }

    private WSResponse handleStartOperation(RepetitionData repetitionData) {
        RepetitionUtils.validateNextNonNull(repetitionData.getCurrentWord());
        return mapper.toWSResponse(repetitionData);
    }

    private WSResponse handleSkipOperation(RepetitionData repetitionData, UUID userId) {
        RepetitionData updatedData = repetitionData.skip();
        getRepository().save(updatedData);

        RepetitionUtils.validateNextNonNull(updatedData.getCurrentWord());
        log.info("Word has been skipped for user [{}]", userId);
        return mapper.toWSResponse(updatedData);
    }

    private WSResponse handleCheckOperation(WSRequest request, RepetitionData repetitionData, UUID userId) {
        getRepetitionProgressUpdater().updateRightAnswers(repetitionData);
        if (Objects.isNull(repetitionData.getCurrentWord())) {
            getRepetitionProgressUpdater().updateRepetitionProgress(repetitionData);
            throw new RepetitionException("No more words to repeat");
        }
        List<String> translationsToCheck = Arrays.asList(request.getInput().split("\\s*,\\s*"));
        long correctTranslationsCount = RepetitionUtils.calculateCorrectTranslationsCount(
                repetitionData.getTranslations(), translationsToCheck);

        RepetitionData updatedRepetitionData = repetitionData.updateData(correctTranslationsCount > 0);
        getRepository().save(updatedRepetitionData);
        log.info("Word has been checked for user [{}]", userId);
        return mapper.toWSResponse(updatedRepetitionData, correctTranslationsCount);
    }
}
