package com.kiskee.dictionarybuilder.service.vocabulary.repetition;

import com.kiskee.dictionarybuilder.enums.repetition.RepetitionType;
import com.kiskee.dictionarybuilder.exception.repetition.RepetitionException;
import com.kiskee.dictionarybuilder.mapper.repetition.RepetitionWordMapper;
import com.kiskee.dictionarybuilder.model.dto.repetition.RepetitionResultDataDto;
import com.kiskee.dictionarybuilder.model.dto.repetition.RepetitionRunningStatus;
import com.kiskee.dictionarybuilder.model.dto.repetition.RepetitionStartFilterRequest;
import com.kiskee.dictionarybuilder.model.dto.repetition.message.WSRequest;
import com.kiskee.dictionarybuilder.model.dto.repetition.message.WSResponse;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.dictionary.DictionaryDto;
import com.kiskee.dictionarybuilder.model.dto.vocabulary.word.WordDto;
import com.kiskee.dictionarybuilder.model.entity.redis.repetition.RepetitionData;
import com.kiskee.dictionarybuilder.repository.redis.RepetitionDataRepository;
import com.kiskee.dictionarybuilder.service.report.StatisticUpdateReportManager;
import com.kiskee.dictionarybuilder.service.vocabulary.dictionary.DictionaryAccessValidator;
import com.kiskee.dictionarybuilder.service.vocabulary.repetition.loader.RepetitionWordLoaderFactory;
import com.kiskee.dictionarybuilder.service.vocabulary.word.WordCounterUpdateService;
import com.kiskee.dictionarybuilder.util.IdentityUtil;
import com.kiskee.dictionarybuilder.util.TimeZoneContextHolder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RepetitionServiceImpl implements RepetitionService {

    private final RepetitionWordLoaderFactory repetitionWordLoaderFactory;
    private final RepetitionDataRepository repository;
    private final RepetitionWordMapper mapper;
    private final DictionaryAccessValidator dictionaryAccessValidator;
    private final WordCounterUpdateService wordCounterUpdateService;
    private final StatisticUpdateReportManager statisticUpdateReportManager;

    @Value("${vocabulary.repetition.words-to-update-count}")
    private int wordsToUpdateCount;

    public RepetitionRunningStatus isRepetitionRunning() {
        UUID userId = IdentityUtil.getUserId();
        Optional<RepetitionData> repetitionDataOpt = repository.findById(userId.toString());
        if (repetitionDataOpt.isPresent()) {
            log.info("Repetition is running for user [{}]", userId);
            RepetitionData repetitionData = repetitionDataOpt.get();
            return new RepetitionRunningStatus(true, repetitionData.isPaused(), repetitionData.getRepetitionType());
        }
        log.info("Repetition is not running for user [{}]", userId);
        return new RepetitionRunningStatus(false, false);
    }

    public RepetitionRunningStatus start(
            long dictionaryId, RepetitionType repetitionType, RepetitionStartFilterRequest request) {
        UUID userId = IdentityUtil.getUserId();
        if (repository.existsById(userId.toString())) {
            log.info("Repetition is already running for user [{}]", userId);
            throw new RepetitionException("Repetition is already running");
        }
        DictionaryDto dictionaryDto = dictionaryAccessValidator.getDictionaryByIdAndUserId(dictionaryId, userId);
        List<WordDto> words = repetitionWordLoaderFactory
                .getLoader(request.getCriteriaFilter().getFilterType())
                .loadRepetitionWordPage(dictionaryId, request);

        if (words.isEmpty()) {
            log.info("No words to repeat for user [{}]", userId);
            throw new RepetitionException("No words to repeat");
        }
        Collections.shuffle(words);

        RepetitionData repetitionData = RepetitionDataFactory.createRepetitionData(
                repetitionType,
                words,
                dictionaryDto,
                userId,
                TimeZoneContextHolder.getTimeZone(),
                request.getReversed());
        repository.save(repetitionData);
        log.info("Repetition has been started for user [{}]", userId);
        return new RepetitionRunningStatus(true, repetitionData.isPaused(), repetitionData.getRepetitionType());
    }

    public RepetitionRunningStatus pause() {
        UUID userId = IdentityUtil.getUserId();
        Optional<RepetitionData> repetitionDataOpt = repository.findById(userId.toString());
        if (repetitionDataOpt.isEmpty()) {
            throw new RepetitionException("Repetition is not running");
        }
        RepetitionData repetitionData = repetitionDataOpt.get();
        repetitionData.startPause();

        repository.save(repetitionData);
        log.info("Repetition has been paused for user [{}]", userId);
        return new RepetitionRunningStatus(true, true, repetitionData.getRepetitionType());
    }

    public RepetitionRunningStatus unpause() {
        UUID userId = IdentityUtil.getUserId();
        RepetitionData repetitionData = getRepetitionData(userId);
        repetitionData.endPause();

        repository.save(repetitionData);
        log.info("Repetition has been unpaused for user [{}]", userId);
        return new RepetitionRunningStatus(true, false, repetitionData.getRepetitionType());
    }

    public RepetitionRunningStatus stop() {
        UUID userId = IdentityUtil.getUserId();
        RepetitionData repetitionData = getRepetitionData(userId);

        List<WordDto> passedWords = repetitionData.getPassedWords();
        if (!passedWords.isEmpty()) {
            ArrayList<WordDto> wordsToUpdate = new ArrayList<>(passedWords);
            wordCounterUpdateService.updateRightAnswersCounters(userId, wordsToUpdate);
        }
        updateRepetitionProgress(repetitionData);

        repository.delete(repetitionData);
        log.info("Repetition has been stopped for user [{}]", userId);
        return new RepetitionRunningStatus(false, false);
    }

    public WSResponse handleRepetitionMessage(Authentication authentication, WSRequest request) {
        UUID userId = IdentityUtil.getUserId(authentication);
        RepetitionData repetitionData = getRepetitionData(userId);

        if (isStartOperation(request)) {
            return handleStartOperation(repetitionData);
        }
        if (isSkipOperation(request)) {
            return handleSkipOperation(repetitionData, userId);
        }
        return handleCheckOperation(request, repetitionData, userId);
    }

    private RepetitionData getRepetitionData(UUID userId) {
        Optional<RepetitionData> repetitionDataOpt = repository.findById(userId.toString());
        if (repetitionDataOpt.isEmpty()) {
            throw new RepetitionException("Repetition is not running");
        }
        return repetitionDataOpt.get();
    }

    private boolean isStartOperation(WSRequest request) {
        return Objects.isNull(request.getInput()) && request.getOperation() == WSRequest.Operation.START;
    }

    private boolean isSkipOperation(WSRequest request) {
        return Objects.isNull(request.getInput()) && request.getOperation() == WSRequest.Operation.SKIP;
    }

    private WSResponse handleStartOperation(RepetitionData repetitionData) {
        WordDto next = repetitionData.getCurrentWord();
        validateNextNonNull(next);
        return mapper.toWSResponse(repetitionData);
    }

    private WSResponse handleSkipOperation(RepetitionData repetitionData, UUID userId) {
        RepetitionData updatedData = repetitionData.skip();
        repository.save(updatedData);

        validateNextNonNull(updatedData.getCurrentWord());
        log.info("Word has been skipped for user [{}]", userId);
        return mapper.toWSResponse(updatedData);
    }

    private WSResponse handleCheckOperation(WSRequest request, RepetitionData repetitionData, UUID userId) {
        List<WordDto> passedWords = repetitionData.getPassedWords();
        if (CollectionUtils.isNotEmpty(passedWords) && passedWords.size() >= wordsToUpdateCount) {
            ArrayList<WordDto> wordsToUpdate = new ArrayList<>(passedWords);
            wordCounterUpdateService.updateRightAnswersCounters(userId, wordsToUpdate);
            passedWords.clear();
        }
        if (Objects.isNull(repetitionData.getCurrentWord())) {
            updateRepetitionProgress(repetitionData);
            throw new RepetitionException("No more words to repeat");
        }
        List<String> translationsToCheck = Arrays.asList(request.getInput().split("\\s*,\\s*"));
        long correctTranslationsCount =
                calculateCorrectTranslationsCount(repetitionData.getTranslations(), translationsToCheck);

        RepetitionData updatedRepetitionData = repetitionData.updateData(correctTranslationsCount > 0);
        repository.save(updatedRepetitionData);
        log.info("Word has been checked for user [{}]", userId);
        return mapper.toWSResponse(updatedRepetitionData, correctTranslationsCount);
    }

    private long calculateCorrectTranslationsCount(Set<String> wordTranslations, List<String> translationsToCheck) {
        return translationsToCheck.stream().filter(wordTranslations::contains).count();
    }

    private void validateNextNonNull(WordDto nextWord) throws RepetitionException {
        if (Objects.isNull(nextWord)) {
            throw new RepetitionException("No more words to repeat");
        }
    }

    private void updateRepetitionProgress(RepetitionData repetitionData) {
        if (repetitionData.getTotalElementsPassed() > 0) {
            RepetitionResultDataDto repetitionResultData = repetitionData.toResult();
            statisticUpdateReportManager.updateRepetitionProgress(repetitionResultData);
        }
    }
}
