package com.kiskee.dictionarybuilder.service.vocabulary.repetition.handler.state;

import com.kiskee.dictionarybuilder.enums.vocabulary.filter.CriteriaFilterType;
import com.kiskee.dictionarybuilder.exception.repetition.RepetitionException;
import com.kiskee.dictionarybuilder.exception.token.InvalidTokenException;
import com.kiskee.dictionarybuilder.model.dto.repetition.RepetitionRunningStatus;
import com.kiskee.dictionarybuilder.model.dto.repetition.start.RepetitionStartRequest;
import com.kiskee.dictionarybuilder.model.dto.repetition.start.SharingRepetitionStartFilterRequest;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.dictionary.DictionaryDto;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.word.WordDto;
import com.kiskee.dictionarybuilder.model.entity.redis.repetition.RepetitionData;
import com.kiskee.dictionarybuilder.repository.redis.RepetitionDataRepository;
import com.kiskee.dictionarybuilder.service.vocabulary.dictionary.DictionaryAccessValidator;
import com.kiskee.dictionarybuilder.service.vocabulary.loader.factory.WordLoaderFactory;
import com.kiskee.dictionarybuilder.service.vocabulary.repetition.RepetitionProgressUpdater;
import com.kiskee.dictionarybuilder.service.vocabulary.repetition.handler.AbstractRepetitionHandler;
import com.kiskee.dictionarybuilder.service.vocabulary.repetition.loader.criteria.RepetitionWordCriteriaLoader;
import com.kiskee.dictionarybuilder.util.IdentityUtil;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Getter(AccessLevel.PROTECTED)
public abstract class AbstractRepetitionStateHandler extends AbstractRepetitionHandler {

    protected final DictionaryAccessValidator dictionaryAccessValidator;
    protected final WordLoaderFactory<CriteriaFilterType, RepetitionWordCriteriaLoader> repetitionWordLoaderFactory;

    public AbstractRepetitionStateHandler(
            RepetitionDataRepository repository,
            DictionaryAccessValidator dictionaryAccessValidator,
            WordLoaderFactory<CriteriaFilterType, RepetitionWordCriteriaLoader> wordLoaderFactory,
            RepetitionProgressUpdater repetitionProgressUpdater) {
        super(repository, repetitionProgressUpdater);
        this.dictionaryAccessValidator = dictionaryAccessValidator;
        this.repetitionWordLoaderFactory = wordLoaderFactory;
    }

    protected abstract DictionaryDto verifyUserHasAccessToDictionary(ProcessingData processingData);

    protected DictionaryDto getDictionaryDtoByDictionaryIdAndUserId(Long dictionaryId, UUID userId) {
        return dictionaryAccessValidator.getDictionaryByIdAndUserId(dictionaryId, userId);
    }

    protected abstract RepetitionData buildRepetitionData(
            List<WordDto> words, DictionaryDto dictionaryDto, UUID userId, RepetitionStartRequest request);

    public RepetitionRunningStatus isRepetitionRunning() {
        return getRepository()
                .findById(IdentityUtil.getUserId().toString())
                .map(this::handleRunningStatus)
                .orElseGet(RepetitionRunningStatus::new);
    }

    @Transactional(noRollbackFor = InvalidTokenException.class)
    public RepetitionRunningStatus start(long dictionaryId, RepetitionStartRequest request) {
        UUID userId = IdentityUtil.getUserId();
        if (getRepository().existsById(userId.toString())) {
            log.info("Repetition is already running for user [{}]", userId);
            throw new RepetitionException("Repetition is already running");
        }
        DictionaryDto dictionaryDto = verifyUserHasAccessToDictionary(
                RepetitionProcessingDataResolver.resolve(dictionaryId, userId, request));
        List<WordDto> words = repetitionWordLoaderFactory
                .getLoader(request.getCriteriaFilter().getFilterType())
                .loadWords(dictionaryDto.getId(), request);

        if (words.isEmpty()) {
            log.info("No words to repeat for user [{}]", userId);
            throw new RepetitionException("No words to repeat");
        }
        Collections.shuffle(words);

        RepetitionData repetitionData = buildRepetitionData(words, dictionaryDto, userId, request);
        getRepository().save(repetitionData);
        log.info("Repetition has been started for user [{}]", userId);
        return new RepetitionRunningStatus(true, repetitionData.isPaused(), repetitionData.getRepetitionType());
    }

    public RepetitionRunningStatus pause() {
        UUID userId = IdentityUtil.getUserId();
        RepetitionData repetitionData = getRepetitionData(userId);
        repetitionData.startPause();
        getRepository().save(repetitionData);
        log.info("Repetition has been paused for user [{}]", userId);
        return new RepetitionRunningStatus(Boolean.TRUE, Boolean.TRUE, repetitionData.getRepetitionType());
    }

    public RepetitionRunningStatus unpause() {
        UUID userId = IdentityUtil.getUserId();
        RepetitionData repetitionData = getRepetitionData(userId);
        repetitionData.endPause();
        getRepository().save(repetitionData);
        log.info("Repetition has been unpaused for user [{}]", userId);
        return new RepetitionRunningStatus(true, false, repetitionData.getRepetitionType());
    }

    public RepetitionRunningStatus stop() {
        UUID userId = IdentityUtil.getUserId();
        RepetitionData repetitionData = getRepetitionData(userId);
        getRepetitionProgressUpdater().updateRightAnswers(repetitionData);
        getRepetitionProgressUpdater().updateRepetitionProgress(repetitionData);
        getRepository().delete(repetitionData);
        log.info("Repetition has been stopped for user [{}]", userId);
        return new RepetitionRunningStatus();
    }

    private RepetitionRunningStatus handleRunningStatus(RepetitionData repetitionData) {
        log.info("Repetition is running for user [{}]", repetitionData.getUserId());
        return new RepetitionRunningStatus(Boolean.TRUE, repetitionData.isPaused(), repetitionData.getRepetitionType());
    }

    private static class RepetitionProcessingDataResolver {
        private static ProcessingData resolve(long dictionaryId, UUID userId, RepetitionStartRequest request) {
            return request instanceof SharingRepetitionStartFilterRequest sharingRequest
                    ? new SharingRepetitionProcessingData(sharingRequest.getSharingToken())
                    : new RepetitionProcessingData(dictionaryId, userId);
        }
    }

    protected interface ProcessingData {}

    protected record RepetitionProcessingData(Long dictionaryId, UUID userId) implements ProcessingData {}

    protected record SharingRepetitionProcessingData(String sharingToken) implements ProcessingData {}
}
