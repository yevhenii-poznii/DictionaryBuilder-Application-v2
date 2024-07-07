package com.kiskee.vocabulary.service.vocabulary.word;

import com.kiskee.vocabulary.enums.ExceptionStatusesEnum;
import com.kiskee.vocabulary.enums.LogMessageEnum;
import com.kiskee.vocabulary.enums.vocabulary.VocabularyResponseMessageEnum;
import com.kiskee.vocabulary.exception.ForbiddenAccessException;
import com.kiskee.vocabulary.exception.ResourceNotFoundException;
import com.kiskee.vocabulary.mapper.dictionary.WordMapper;
import com.kiskee.vocabulary.model.dto.ResponseMessage;
import com.kiskee.vocabulary.model.dto.report.goal.WordAdditionData;
import com.kiskee.vocabulary.model.dto.vocabulary.word.WordDto;
import com.kiskee.vocabulary.model.dto.vocabulary.word.WordSaveRequest;
import com.kiskee.vocabulary.model.dto.vocabulary.word.WordSaveResponse;
import com.kiskee.vocabulary.model.dto.vocabulary.word.WordUpdateRequest;
import com.kiskee.vocabulary.model.entity.vocabulary.Word;
import com.kiskee.vocabulary.model.entity.vocabulary.WordTranslation;
import com.kiskee.vocabulary.repository.vocabulary.WordRepository;
import com.kiskee.vocabulary.service.report.UpdateGoalReportService;
import com.kiskee.vocabulary.service.user.preference.WordPreferenceService;
import com.kiskee.vocabulary.service.vocabulary.dictionary.DictionaryAccessValidator;
import com.kiskee.vocabulary.service.vocabulary.word.translation.WordTranslationService;
import com.kiskee.vocabulary.util.IdentityUtil;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WordServiceImpl implements WordService, WordCounterUpdateService {

    private final WordRepository repository;
    private final WordMapper mapper;

    private final DictionaryAccessValidator dictionaryAccessValidator;
    private final WordTranslationService wordTranslationService;

    private final WordPreferenceService wordPreferenceService;
    private final UpdateGoalReportService updateWordAdditionGoalReportService;

    @Override
    @Transactional
    public WordSaveResponse addWord(Long dictionaryId, WordSaveRequest wordSaveRequest) {
        dictionaryAccessValidator.verifyUserHasDictionary(dictionaryId);
        Word wordToSave = mapper.toEntity(wordSaveRequest, dictionaryId);
        Word saved = repository.save(wordToSave);

        UUID userId = IdentityUtil.getUserId();
        int newWordsPerDayGoal = wordPreferenceService.getWordPreference(userId).newWordsPerDayGoal();
        updateWordAdditionGoalReportService.updateReport(userId, dictionaryId, newWordsPerDayGoal);

        return mapToResponse(saved, VocabularyResponseMessageEnum.WORD_ADDED, LogMessageEnum.WORD_ADDED);
    }

    @Override
    @Transactional
    public WordSaveResponse updateWord(Long dictionaryId, Long wordId, WordUpdateRequest updateRequest) {
        Word wordToUpdate = getWord(wordId, dictionaryId);

        List<WordTranslation> wordTranslations = wordTranslationService.updateTranslations(
                updateRequest.getWordTranslations(), wordToUpdate.getWordTranslations());

        wordToUpdate = mapper.toEntity(wordToUpdate, updateRequest, wordTranslations);
        Word updated = repository.save(wordToUpdate);

        return mapToResponse(updated, VocabularyResponseMessageEnum.WORD_UPDATED, LogMessageEnum.WORD_UPDATED);
    }

    @Override
    @Transactional
    public ResponseMessage updateRepetition(Long dictionaryId, Long wordId, Boolean useInRepetition) {
        dictionaryAccessValidator.verifyUserHasDictionary(dictionaryId);

        int updatedRow = repository.updateUseInRepetitionByIdAndDictionaryId(wordId, dictionaryId, useInRepetition);
        if (updatedRow > 0) {
            String message = useInRepetition
                    ? VocabularyResponseMessageEnum.WORD_FOR_REPETITION_IS_SET.getResponseMessage()
                    : VocabularyResponseMessageEnum.WORD_NOT_FOR_REPETITION_IS_SET.getResponseMessage();

            return new ResponseMessage(message);
        }
        throw new ResourceNotFoundException(String.format(
                ExceptionStatusesEnum.RESOURCE_NOT_FOUND.getStatus(), Word.class.getSimpleName(), wordId));
    }

    @Override
    @Transactional
    public ResponseMessage deleteWord(Long dictionaryId, Long wordId) {
        Word wordToDelete = getWord(wordId, dictionaryId);

        repository.delete(wordToDelete);

        // TODO update word addition goal report if deleted word was added today

        displayLog(LogMessageEnum.WORD_DELETED, wordToDelete);

        return new ResponseMessage(
                String.format(VocabularyResponseMessageEnum.WORD_DELETED.getResponseMessage(), wordToDelete));
    }

    @Override
    @Transactional
    public ResponseMessage deleteWords(Long dictionaryId, Set<Long> wordIds) {
        dictionaryAccessValidator.verifyUserHasDictionary(dictionaryId);

        List<Word> wordsToDelete = repository.findByIdIn(wordIds);

        verifyWordBelongsToSpecifiedDictionary(wordsToDelete, dictionaryId);

        repository.deleteAll(wordsToDelete);

        // TODO update word addition goal report if deleted words were added today

        return new ResponseMessage(
                String.format(VocabularyResponseMessageEnum.WORDS_DELETE.getResponseMessage(), wordsToDelete));
    }

    /* TODO implement method for move word to another dictionary
    implementation
     */


    // TODO
    @Async
    @Override
    @Retryable
    @Transactional
    public void updateRightAnswersCounters(UUID userId, List<WordDto> wordsToUpdate) {
        if (CollectionUtils.isEmpty(wordsToUpdate)) {
            return;
        }
        Map<Long, Integer> wordIdsToUpdate =
                wordsToUpdate.stream().collect(Collectors.toMap(WordDto::getId, WordDto::getCounterRightAnswers));

        List<Word> existingWords = repository.findByIdIn(wordIdsToUpdate.keySet());

        int rightAnswersToDisableInRepetition =
                wordPreferenceService.getWordPreference(userId).rightAnswersToDisableInRepetition();
        existingWords.forEach(word ->
                word.setCounterRightAnswers(wordIdsToUpdate.get(word.getId()), rightAnswersToDisableInRepetition));

        repository.saveAll(existingWords);
        log.info(
                "Right answers counters have been updated for words: {} for user: {}",
                wordIdsToUpdate.keySet(),
                userId);
    }

    private Word getWord(Long wordId, Long dictionaryId) {
        dictionaryAccessValidator.verifyUserHasDictionary(dictionaryId);
        Word word = repository.getWord(wordId);
        verifyWordBelongsToSpecifiedDictionary(word, dictionaryId);
        return word;
    }

    private void verifyWordBelongsToSpecifiedDictionary(List<Word> words, Long dictionaryId) {
        List<Long> forbiddenWords = words.stream()
                .filter(word -> !word.getDictionaryId().equals(dictionaryId))
                .map(Word::getId)
                .toList();

        if (CollectionUtils.isNotEmpty(forbiddenWords)) {
            throw new ForbiddenAccessException(String.format(
                    ExceptionStatusesEnum.FORBIDDEN_ACCESS.getStatus(), Word.class.getSimpleName(), forbiddenWords));
        }
    }

    private void verifyWordBelongsToSpecifiedDictionary(Word word, Long dictionaryId) {
        if (!word.getDictionaryId().equals(dictionaryId)) {
            throw new ForbiddenAccessException(String.format(
                    ExceptionStatusesEnum.FORBIDDEN_ACCESS.getStatus(),
                    Word.class.getSimpleName(),
                    word.getId().toString()));
        }
    }

    private void displayLog(LogMessageEnum logMessage, Word word) {
        log.info(logMessage.getMessage(), word.getWord(), word.getDictionaryId(), IdentityUtil.getUserId());
    }

    private WordSaveResponse mapToResponse(
            Word word, VocabularyResponseMessageEnum messageEnum, LogMessageEnum logMessageEnum) {
        displayLog(logMessageEnum, word);

        WordDto wordDto = mapper.toDto(word);

        return new WordSaveResponse(String.format(messageEnum.getResponseMessage(), wordDto.getWord()), wordDto);
    }
}
