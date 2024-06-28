package com.kiskee.vocabulary.service.vocabulary.repetition;

import com.kiskee.vocabulary.exception.repetition.RepetitionException;
import com.kiskee.vocabulary.model.dto.redis.RepetitionData;
import com.kiskee.vocabulary.model.dto.repetition.RepetitionRunningStatus;
import com.kiskee.vocabulary.model.dto.repetition.RepetitionStartFilterRequest;
import com.kiskee.vocabulary.model.dto.repetition.message.WSRequest;
import com.kiskee.vocabulary.model.dto.repetition.message.WSResponse;
import com.kiskee.vocabulary.model.dto.vocabulary.word.WordDto;
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
import java.util.HashSet;
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
            // TODO throw custom exception
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
                .totalElements(repetitionWords.size())
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
            return handleNextOperation(repetitionData);
        }
        if (isSkipOperation(request)) {
            return handleSkipOperation(repetitionData);
        }
        return handleCheckOperation(request, repetitionData, userId);
    }

    public void pause() {
    }

    public void stop() {
    }

    private boolean isStartOperation(WSRequest request) {
        return Objects.isNull(request.getInput()) && request.getOperation() == WSRequest.Operation.START;
    }

    private boolean isSkipOperation(WSRequest request) {
        return Objects.isNull(request.getInput()) && request.getOperation() == WSRequest.Operation.SKIP;
    }

    private WSResponse handleNextOperation(RepetitionData repetitionData) {
        return new WSResponse(repetitionData.getCurrentWord().getWord(), null);
    }

    private WSResponse handleSkipOperation(RepetitionData repetitionData) {
        String next = repetitionData.setNext();
        repository.save(repetitionData.getUserId(), repetitionData);
        return new WSResponse(next, null);
    }

    private WSResponse handleCheckOperation(WSRequest request, RepetitionData repetitionData, UUID userId) {
        Deque<WordDto> passedWords = repetitionData.getPassedWords();
        if (CollectionUtils.isNotEmpty(passedWords) && passedWords.size() >= wordsToUpdateCount) {
            // TODO update passed words
            // TODO update report
            passedWords.clear();
        }
        WordDto currentWord = repetitionData.getCurrentWord();
        Set<String> translationsToCheck = new HashSet<>(Arrays.asList(request.getInput().split(", ")));
        long correctTranslationsCount = currentWord.getWordTranslations().stream()
                .filter(translation -> translationsToCheck.contains(translation.getTranslation()))
                .count();
        boolean isCorrect = correctTranslationsCount > 0;
        RepetitionData updatedRepetitionData = repetitionData.updateData(isCorrect);

        repository.save(userId, updatedRepetitionData);
        return new WSResponse(updatedRepetitionData.getCurrentWord().getWord(), isCorrect);
    }
}
