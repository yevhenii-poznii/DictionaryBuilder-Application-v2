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
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

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
        if (repository.existsByUserId(IdentityUtil.getUserId())) {
            return new RepetitionRunningStatus(true);
        }
        return new RepetitionRunningStatus(false);
    }

    public List<WordDto> test(Long dictionaryId, RepetitionStartFilterRequest request) {
        List<WordDto> words = repetitionWordLoaderFactory
                .getLoader(request.getCriteriaFilter().getFilterType())
                .loadRepetitionWordPage(dictionaryId, request);
        Collections.shuffle(words);
        return words;
    }

    @Override
    public void start(long dictionaryId, RepetitionStartFilterRequest request) {
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
                .totalElements(words.size())
                .pauses(new ArrayList<>())
                .startTime(Instant.now())
                .userId(userId)
                .build();

        repository.save(userId, repetitionData);
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

    public void pause() {
        UUID userId = IdentityUtil.getUserId();

        if (!repository.existsByUserId(userId)) {
            throw new RepetitionException("Repetition is not running");
        }
        RepetitionData repetitionData = repository.getByUserId(userId);
        repetitionData.startPause();

        repository.save(userId, repetitionData);
    }

    public void unpause() {
        UUID userId = IdentityUtil.getUserId();

        if (!repository.existsByUserId(userId)) {
            throw new RepetitionException("Repetition is not running");
        }
        RepetitionData repetitionData = repository.getByUserId(userId);
        repetitionData.endPause();

        repository.save(userId, repetitionData);
    }

    public void stop() {
        UUID userId = IdentityUtil.getUserId();

        if (!repository.existsByUserId(userId)) {
            throw new RepetitionException("Repetition is not running");
        }

        RepetitionData repetitionData = repository.getByUserId(userId);

        if (!repetitionData.getPassedWords().isEmpty()) {
            // TODO update passed words
            // TODO update report
            repetitionData.getPassedWords().clear();
        }

        // TODO update report

        repository.clearByUserId(userId);
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
//        return new WSResponse(next.getWord(), next.getWordHint(), null);
    }

    private WSResponse handleSkipOperation(RepetitionData repetitionData, UUID userId) {
        WordDto next = repetitionData.setNext();
        repository.save(userId, repetitionData);

        validateNextNonNull(next);
        return mapper.toWSResponse(repetitionData);
//        return new WSResponse(next.getWord(), next.getWordHint(), null);
    }

    private WSResponse handleCheckOperation(WSRequest request, RepetitionData repetitionData, UUID userId) {
        Deque<WordDto> passedWords = repetitionData.getPassedWords();
        if (CollectionUtils.isNotEmpty(passedWords) && passedWords.size() >= wordsToUpdateCount) {
            // TODO update passed words
            // TODO update report
            passedWords.clear();
        }
        WordDto currentWord = repetitionData.getCurrentWord();

        List<String> translationsToCheck = Arrays.asList(request.getInput().split(", "));
        long correctTranslationsCount = calculateCorrectTranslationsCount(currentWord.getWordTranslations(), translationsToCheck);
        boolean isCorrect = correctTranslationsCount > 0;

        RepetitionData updatedRepetitionData = repetitionData.updateData(isCorrect);
        repository.save(userId, updatedRepetitionData);

//        validateNextNonNull(updatedRepetitionData.getCurrentWord());
        return mapper.toWSResponse(updatedRepetitionData, isCorrect);
//        return new WSResponse(updatedRepetitionData.getCurrentWord().getWord(), updatedRepetitionData.getCurrentWord().getWordHint(), isCorrect);
    }

    private long calculateCorrectTranslationsCount(Set<WordTranslationDto> actualTranslations, List<String> translationsToCheck) {
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
