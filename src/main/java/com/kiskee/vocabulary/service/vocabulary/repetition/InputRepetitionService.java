package com.kiskee.vocabulary.service.vocabulary.repetition;

import com.kiskee.vocabulary.exception.repetition.RepetitionException;
import com.kiskee.vocabulary.mapper.repetition.RepetitionWordMapper;
import com.kiskee.vocabulary.model.dto.redis.RepetitionData;
import com.kiskee.vocabulary.model.dto.repetition.RepetitionRunningStatus;
import com.kiskee.vocabulary.model.dto.repetition.RepetitionStartFilterRequest;
import com.kiskee.vocabulary.model.dto.repetition.message.WSRequest;
import com.kiskee.vocabulary.model.dto.repetition.message.WSResponse;
import com.kiskee.vocabulary.model.dto.vocabulary.word.WordDto;
import com.kiskee.vocabulary.model.dto.vocabulary.word.WordTranslationDto;
import com.kiskee.vocabulary.repository.redis.RedisRepository;
import com.kiskee.vocabulary.service.vocabulary.dictionary.DictionaryAccessValidator;
import com.kiskee.vocabulary.service.vocabulary.repetition.loader.RepetitionWordLoaderFactory;
import com.kiskee.vocabulary.util.IdentityUtil;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Getter
@Service
@RequiredArgsConstructor
public class InputRepetitionService implements RepetitionService {

    private final RepetitionWordLoaderFactory repetitionWordLoaderFactory;
    private final RedisRepository repository;
    private final RepetitionWordMapper mapper;
    private final DictionaryAccessValidator dictionaryAccessValidator;

    @Value("${vocabulary.repetition.words-to-update-count}")
    private int wordsToUpdateCount;

    @Override
    public RepetitionRunningStatus isRepetitionRunning() {
        UUID userId = IdentityUtil.getUserId();

        if (repository.existsByUserId(userId)) {
            RepetitionData repetitionData = repository.getByUserId(userId);
            return new RepetitionRunningStatus(true, repetitionData.isPaused());
        }
        return new RepetitionRunningStatus(false, false);
    }

    @Override
    public RepetitionRunningStatus start(long dictionaryId, RepetitionStartFilterRequest request) {
        UUID userId = IdentityUtil.getUserId();

        if (repository.existsByUserId(userId)) {
            throw new RepetitionException("Repetition is already running");
        }
        dictionaryAccessValidator.verifyUserHasDictionary(dictionaryId);
        List<WordDto> words = repetitionWordLoaderFactory
                .getLoader(request.getCriteriaFilter().getFilterType())
                .loadRepetitionWordPage(dictionaryId, request);

        if (words.isEmpty()) {
            throw new RepetitionException("No words to repeat");
        }
        Collections.shuffle(words);

        Deque<WordDto> repetitionWords = new ArrayDeque<>(words);
        WordDto next = repetitionWords.pop();
        RepetitionData repetitionData = RepetitionData.builder()
                .repetitionWords(repetitionWords)
                .passedWords(new ArrayDeque<>())
                .currentWord(next)
                .pauses(new ArrayList<>())
                .startTime(Instant.now())
                .totalElements(words.size())
                .userId(userId)
                .build();

        repository.save(userId, repetitionData);
        return new RepetitionRunningStatus(true, repetitionData.isPaused());
    }

    public RepetitionRunningStatus pause() {
        UUID userId = IdentityUtil.getUserId();

        if (!repository.existsByUserId(userId)) {
            throw new RepetitionException("Repetition is not running");
        }
        RepetitionData repetitionData = repository.getByUserId(userId);
        repetitionData.startPause();

        repository.save(userId, repetitionData);
        return new RepetitionRunningStatus(true, true);
    }

    public RepetitionRunningStatus unpause() {
        UUID userId = IdentityUtil.getUserId();

        if (!repository.existsByUserId(userId)) {
            throw new RepetitionException("Repetition is not running");
        }
        RepetitionData repetitionData = repository.getByUserId(userId);
        repetitionData.endPause();

        repository.save(userId, repetitionData);
        return new RepetitionRunningStatus(true, false);
    }

    public RepetitionRunningStatus stop() {
        UUID userId = IdentityUtil.getUserId();

        if (!repository.existsByUserId(userId)) {
            throw new RepetitionException("Repetition is not running");
        }
        RepetitionData repetitionData = repository.getByUserId(userId);

        if (!repetitionData.getPassedWords().isEmpty()) {
            // TODO update passed words
            repetitionData.getPassedWords().clear();
        }

        // TODO update report

        repository.clearByUserId(userId);
        return new RepetitionRunningStatus(false, false);
    }

    @Override
    public WSResponse handleRepetitionMessage(Authentication authentication, WSRequest request) {
        UUID userId = IdentityUtil.getUserId(authentication);
        RepetitionData repetitionData = repository.getByUserId(userId);

        if (isStartOperation(request)) {
            return handleStartOperation(repetitionData);
        }
        if (isSkipOperation(request)) {
            return handleSkipOperation(repetitionData, userId);
        }
        return handleCheckOperation(request, repetitionData, userId);
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
        repository.save(userId, updatedData);

        validateNextNonNull(updatedData.getCurrentWord());
        return mapper.toWSResponse(updatedData);
    }

    private WSResponse handleCheckOperation(WSRequest request, RepetitionData repetitionData, UUID userId) {
        Deque<WordDto> passedWords = repetitionData.getPassedWords();
        if (CollectionUtils.isNotEmpty(passedWords) && passedWords.size() >= wordsToUpdateCount) {
            // TODO update passed words
            // TODO update report
            passedWords.clear();
        }
        WordDto currentWord = repetitionData.getCurrentWord();

        if (Objects.isNull(currentWord)) {
            // TODO update report
            throw new RepetitionException("No more words to repeat");
        }

        List<String> translationsToCheck = Arrays.asList(request.getInput().split(", "));
        long correctTranslationsCount =
                calculateCorrectTranslationsCount(currentWord.getWordTranslations(), translationsToCheck);

        RepetitionData updatedRepetitionData = repetitionData.updateData(correctTranslationsCount > 0);
        repository.save(userId, updatedRepetitionData);

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
}
