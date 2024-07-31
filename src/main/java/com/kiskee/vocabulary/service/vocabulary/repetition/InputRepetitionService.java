package com.kiskee.vocabulary.service.vocabulary.repetition;

import com.kiskee.vocabulary.exception.repetition.RepetitionException;
import com.kiskee.vocabulary.mapper.repetition.RepetitionWordMapper;
import com.kiskee.vocabulary.model.dto.repetition.RepetitionResultDataDto;
import com.kiskee.vocabulary.model.dto.repetition.RepetitionRunningStatus;
import com.kiskee.vocabulary.model.dto.repetition.RepetitionStartFilterRequest;
import com.kiskee.vocabulary.model.dto.repetition.message.WSRequest;
import com.kiskee.vocabulary.model.dto.repetition.message.WSResponse;
import com.kiskee.vocabulary.model.dto.vocabulary.dictionary.DictionaryDto;
import com.kiskee.vocabulary.model.dto.vocabulary.word.WordDto;
import com.kiskee.vocabulary.model.dto.vocabulary.word.WordTranslationDto;
import com.kiskee.vocabulary.model.entity.redis.repetition.RepetitionData;
import com.kiskee.vocabulary.repository.redis.RepetitionDataRepository;
import com.kiskee.vocabulary.service.report.StatisticUpdateReportManager;
import com.kiskee.vocabulary.service.vocabulary.dictionary.DictionaryAccessValidator;
import com.kiskee.vocabulary.service.vocabulary.repetition.loader.RepetitionWordLoaderFactory;
import com.kiskee.vocabulary.service.vocabulary.word.WordCounterUpdateService;
import com.kiskee.vocabulary.util.IdentityUtil;
import com.kiskee.vocabulary.util.TimeZoneContextHolder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Slf4j
@Getter
@Service
@RequiredArgsConstructor
public class InputRepetitionService implements RepetitionService {

    private final RepetitionWordLoaderFactory repetitionWordLoaderFactory;
    private final RepetitionDataRepository repository;
    private final RepetitionWordMapper mapper;
    private final DictionaryAccessValidator dictionaryAccessValidator;
    private final WordCounterUpdateService wordCounterUpdateService;
    private final StatisticUpdateReportManager statisticUpdateReportManager;

    @Value("${vocabulary.repetition.words-to-update-count}")
    private int wordsToUpdateCount;

    @Override
    public RepetitionRunningStatus isRepetitionRunning() {
        UUID userId = IdentityUtil.getUserId();
        Optional<RepetitionData> repetitionDataOpt = repository.findById(userId.toString());
        if (repetitionDataOpt.isPresent()) {
            log.info("Repetition is running for user [{}]", userId);
            return new RepetitionRunningStatus(true, repetitionDataOpt.get().isPaused());
        }
        log.info("Repetition is not running for user [{}]", userId);
        return new RepetitionRunningStatus(false, false);
    }

    @Override
    public RepetitionRunningStatus start(long dictionaryId, RepetitionStartFilterRequest request) {
        UUID userId = IdentityUtil.getUserId();
        if (repository.existsById(userId.toString())) {
            log.info("Repetition is already running for user [{}]", userId);
            throw new RepetitionException("Repetition is already running");
        }
        DictionaryDto dictionary = dictionaryAccessValidator.getDictionaryByIdAndUserId(dictionaryId, userId);
        List<WordDto> words = repetitionWordLoaderFactory
                .getLoader(request.getCriteriaFilter().getFilterType())
                .loadRepetitionWordPage(dictionaryId, request);

        if (words.isEmpty()) {
            log.info("No words to repeat for user [{}]", userId);
            throw new RepetitionException("No words to repeat");
        }
        Collections.shuffle(words);

        RepetitionData repetitionData = new RepetitionData(
                words, dictionaryId, dictionary.getDictionaryName(), userId, TimeZoneContextHolder.getTimeZone());
        repository.save(repetitionData);
        log.info("Repetition has been started for user [{}]", userId);
        return new RepetitionRunningStatus(true, repetitionData.isPaused());
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
        return new RepetitionRunningStatus(true, true);
    }

    public RepetitionRunningStatus unpause() {
        UUID userId = IdentityUtil.getUserId();
        RepetitionData repetitionData = getRepetitionData(userId);
        repetitionData.endPause();

        repository.save(repetitionData);
        log.info("Repetition has been unpaused for user [{}]", userId);
        return new RepetitionRunningStatus(true, false);
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

    @Override
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
        WordDto currentWord = repetitionData.getCurrentWord();

        if (Objects.isNull(currentWord)) {
            updateRepetitionProgress(repetitionData);
            throw new RepetitionException("No more words to repeat");
        }
        List<String> translationsToCheck = Arrays.asList(request.getInput().split(", "));
        long correctTranslationsCount =
                calculateCorrectTranslationsCount(currentWord.getWordTranslations(), translationsToCheck);

        RepetitionData updatedRepetitionData = repetitionData.updateData(correctTranslationsCount > 0);
        repository.save(updatedRepetitionData);
        log.info("Word has been checked for user [{}]", userId);
        return mapper.toWSResponse(updatedRepetitionData, correctTranslationsCount);
    }

    private long calculateCorrectTranslationsCount(
            Set<WordTranslationDto> actualTranslations, List<String> translationsToCheck) {
        return translationsToCheck.stream()
                .filter(translation -> actualTranslations.contains(new WordTranslationDto(translation)))
                .count();
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
