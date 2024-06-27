package com.kiskee.vocabulary.service.vocabulary.repetition;

import com.kiskee.vocabulary.exception.repetition.RepetitionException;
import com.kiskee.vocabulary.model.dto.redis.RepetitionData;
import com.kiskee.vocabulary.model.dto.repetition.RepetitionStartFilterRequest;
import com.kiskee.vocabulary.model.dto.repetition.RepetitionStatusResponse;
import com.kiskee.vocabulary.model.dto.repetition.message.WSRequest;
import com.kiskee.vocabulary.model.dto.repetition.message.WSResponse;
import com.kiskee.vocabulary.model.dto.vocabulary.word.WordDto;
import com.kiskee.vocabulary.repository.redis.RedisRepository;
import com.kiskee.vocabulary.service.vocabulary.AbstractDictionaryService;
import com.kiskee.vocabulary.service.vocabulary.dictionary.page.DictionaryPageLoaderFactory;
import com.kiskee.vocabulary.util.IdentityUtil;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Getter
@Service
@AllArgsConstructor
public class InputRepetitionService extends AbstractDictionaryService implements RepetitionService {

    private final DictionaryPageLoaderFactory dictionaryPageLoaderFactory;
    private final RedisRepository repository;

    private final int amountPassedWordsToUpdate = 10;

    @Override
    public RepetitionStatusResponse isRepetitionRunning() {
        if (repository.existsByUserId(IdentityUtil.getUserId())) {
            return new RepetitionStatusResponse(true);
        }
        return new RepetitionStatusResponse(false);
    }

    @Override
    public void start(long dictionaryId, RepetitionStartFilterRequest request) {
        UUID userId = IdentityUtil.getUserId();

        if (repository.existsByUserId(userId)) {
            // TODO throw custom exception
            throw new RepetitionException("Repetition is already running");
        }

        // TODO verify dictionary owner before load
//        DictionaryPageResponseDto repetitionPage = load(dictionaryId, request);
//
//        Deque<WordDto> repetitionWords = new ArrayDeque<>(repetitionPage.getWords());
//        WordDto next = repetitionWords.pop();
//
//        RepetitionData repetitionData = RepetitionData.builder()
//                .repetitionWords(repetitionWords)
//                .passedWords(new ArrayDeque<>())
//                .currentWord(next)
//                .totalElements(repetitionPage.getTotalElements())
//                .userId(userId)
//                .build();
//
//        repository.save(userId, repetitionData);
    }

    @Override
    public WSResponse handleRepetitionMessage(Authentication authentication, WSRequest request) {
        UUID userId = IdentityUtil.getUserId(authentication);
        RepetitionData repetitionData = repository.getByUserId(userId);

        if (isNextOperation(request)) {
            return handleNextOperation(repetitionData);
        }
        if (isSkipOperation(request)) {
            return handleSkipOperation(repetitionData);
        }
        return handleCheckOperation(request, repetitionData, userId);
    }

    public void pause() {}

    public void stop() {}

    private boolean isNextOperation(WSRequest request) {
        return Objects.isNull(request.getInput()) && request.getOperation() == WSRequest.Operation.NEXT;
    }

    private boolean isSkipOperation(WSRequest request) {
        return Objects.isNull(request.getInput()) && request.getOperation() == WSRequest.Operation.SKIP;
    }

    private WSResponse handleNextOperation(RepetitionData repetitionData) {
        return new WSResponse(repetitionData.getCurrentWord().getWord(), null);
    }

    private WSResponse handleSkipOperation(RepetitionData repetitionData) {
        String next = repetitionData.setNext();
        return new WSResponse(next, null);
    }

    private WSResponse handleCheckOperation(WSRequest request, RepetitionData repetitionData, UUID userId) {
        Deque<WordDto> passedWords = repetitionData.getPassedWords();
        if (CollectionUtils.isNotEmpty(passedWords) && passedWords.size() >= amountPassedWordsToUpdate) {
            // TODO update passed words
            // TODO update report
            passedWords.clear();
        }
        WordDto currentWord = repetitionData.getCurrentWord();
        List<String> translationsToCheck = Arrays.asList(request.getInput().split(", "));
        long correctTranslationsCount = currentWord.getWordTranslations().stream()
                .filter(translation -> translationsToCheck.contains(translation.getTranslation()))
                .count();
        boolean isCorrect = correctTranslationsCount > 0;
        RepetitionData updateRepetitionData = updateRepetitionData(repetitionData, isCorrect);

        repository.save(userId, updateRepetitionData);
        return new WSResponse(repetitionData.getCurrentWord().getWord(), isCorrect);
    }

    private RepetitionData updateRepetitionData(RepetitionData repetitionData, boolean isCorrect) {
        if (isCorrect) {
            repetitionData.incrementRightAnswersCount();
        } else {
            repetitionData.incrementWrongAnswersCount();
        }
        repetitionData.incrementTotalElementsPassed();
        repetitionData.addPassedWord();

        return repetitionData;
    }
}
